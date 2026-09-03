package cn.iocoder.yudao.module.gift.service.trip;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.LocalSearchMetaheuristic;
import com.google.ortools.constraintsolver.RoutingDimension;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;
import com.google.ortools.constraintsolver.IntVar;
import com.google.ortools.constraintsolver.main;
import com.google.protobuf.Duration;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 单日单车 TSPTW 求解器。
 *
 * <p>节点 0 固定为住宿点（起点与终点相同）；其他节点按时间窗、服务时长和真实路线耗时求解。
 * 景点允许在时间窗冲突时被降级移除，餐饮节点保持必达。</p>
 */
final class TripOrToolsRouteOptimizer {

    private static final long SCENIC_DROP_PENALTY = 100_000L;

    private TripOrToolsRouteOptimizer() {
    }

    static Result solve(List<Node> nodes, long[][] travelMinutes, int dayStartMinutes, int dayEndMinutes,
                        Long budgetCapacity) {
        return solve(nodes, travelMinutes, dayStartMinutes, dayEndMinutes, budgetCapacity, Map.of());
    }

    static Result solve(List<Node> nodes, long[][] travelMinutes, int dayStartMinutes, int dayEndMinutes,
                        Long budgetCapacity, Map<SelectionGroup, Integer> selectionCounts) {
        validate(nodes, travelMinutes, dayStartMinutes, dayEndMinutes, budgetCapacity);
        Loader.loadNativeLibraries();
        RoutingIndexManager manager = new RoutingIndexManager(nodes.size(), 1, 0);
        RoutingModel routing = new RoutingModel(manager);
        try {
            int travelCallback = routing.registerTransitMatrix(travelMinutes);
            routing.setArcCostEvaluatorOfAllVehicles(travelCallback);
            int timeCallback = routing.registerTransitCallback((fromIndex, toIndex) -> {
                int fromNode = manager.indexToNode(fromIndex);
                int toNode = manager.indexToNode(toIndex);
                return travelMinutes[fromNode][toNode] + nodes.get(fromNode).serviceMinutes();
            });
            routing.addDimension(timeCallback, dayEndMinutes - dayStartMinutes, dayEndMinutes, false, "Time");
            RoutingDimension timeDimension = routing.getMutableDimension("Time");
            timeDimension.cumulVar(routing.start(0)).setRange(dayStartMinutes, dayStartMinutes);
            timeDimension.cumulVar(routing.end(0)).setRange(dayStartMinutes, dayEndMinutes);
            Map<SelectionGroup, List<IntVar>> selectionVars = new EnumMap<>(SelectionGroup.class);
            Map<String, List<IntVar>> restaurantVarsByCandidate = new HashMap<>();
            for (int nodeIndex = 1; nodeIndex < nodes.size(); nodeIndex++) {
                Node node = nodes.get(nodeIndex);
                long routingIndex = manager.nodeToIndex(nodeIndex);
                timeDimension.cumulVar(routingIndex).setRange(node.openMinutes(),
                        node.closeMinutes() - node.serviceMinutes());
                if (node.optional()) {
                    long penalty = node.selectionGroup() == SelectionGroup.SCENIC
                            ? SCENIC_DROP_PENALTY + node.utilityScore() * 100L
                            : Math.max(1, node.utilityScore()) * 100L;
                    routing.addDisjunction(new long[]{routingIndex}, penalty);
                }
                if (node.selectionGroup() != SelectionGroup.NONE) {
                    IntVar activeVar = routing.activeVar(routingIndex);
                    selectionVars.computeIfAbsent(node.selectionGroup(), ignored -> new ArrayList<>()).add(activeVar);
                    if ((node.selectionGroup() == SelectionGroup.LUNCH || node.selectionGroup() == SelectionGroup.DINNER)
                            && !node.candidateKey().isBlank()) {
                        restaurantVarsByCandidate.computeIfAbsent(node.candidateKey(), ignored -> new ArrayList<>()).add(activeVar);
                    }
                }
            }
            for (Map.Entry<SelectionGroup, Integer> entry : selectionCounts.entrySet()) {
                List<IntVar> vars = selectionVars.get(entry.getKey());
                if (entry.getValue() == null || entry.getValue() < 0 || vars == null || vars.size() < entry.getValue()) {
                    return null;
                }
                routing.solver().addConstraint(routing.solver().makeSumEquality(vars.toArray(IntVar[]::new), entry.getValue()));
            }
            restaurantVarsByCandidate.values().stream().filter(vars -> vars.size() > 1)
                    .forEach(vars -> routing.solver().addConstraint(routing.solver().makeSumLessOrEqual(vars.toArray(IntVar[]::new), 1)));
            if (budgetCapacity != null) {
                int budgetCallback = routing.registerUnaryTransitCallback(index -> nodes.get(manager.indexToNode(index)).cost());
                routing.addDimensionWithVehicleCapacity(budgetCallback, 0, new long[]{budgetCapacity}, true, "Budget");
            }
            Assignment assignment = routing.solveWithParameters(searchParameters());
            return assignment == null ? null : toResult(nodes, travelMinutes, manager, routing, timeDimension, assignment);
        } finally {
            routing.delete();
            manager.delete();
        }
    }

    private static RoutingSearchParameters searchParameters() {
        return main.defaultRoutingSearchParameters().toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                .setTimeLimit(Duration.newBuilder().setNanos(250_000_000).build())
                .build();
    }

    private static Result toResult(List<Node> nodes, long[][] travelMinutes, RoutingIndexManager manager,
                                   RoutingModel routing, RoutingDimension timeDimension, Assignment assignment) {
        List<Visit> visits = new ArrayList<>();
        long currentIndex = routing.start(0);
        int previousNode = manager.indexToNode(currentIndex);
        while (!routing.isEnd(currentIndex)) {
            currentIndex = assignment.value(routing.nextVar(currentIndex));
            if (routing.isEnd(currentIndex)) {
                break;
            }
            int currentNode = manager.indexToNode(currentIndex);
            Node node = nodes.get(currentNode);
            int startMinutes = Math.toIntExact(assignment.value(timeDimension.cumulVar(currentIndex)));
            visits.add(new Visit(node.id(), startMinutes, startMinutes + node.serviceMinutes(),
                    Math.toIntExact(travelMinutes[previousNode][currentNode])));
            previousNode = currentNode;
        }
        Set<String> droppedNodeIds = new LinkedHashSet<>();
        for (int nodeIndex = 1; nodeIndex < nodes.size(); nodeIndex++) {
            long index = manager.nodeToIndex(nodeIndex);
            if (assignment.value(routing.nextVar(index)) == index) {
                droppedNodeIds.add(nodes.get(nodeIndex).id());
            }
        }
        int endMinutes = Math.toIntExact(assignment.value(timeDimension.cumulVar(routing.end(0))));
        return new Result(visits, droppedNodeIds, endMinutes);
    }

    private static void validate(List<Node> nodes, long[][] travelMinutes, int dayStartMinutes, int dayEndMinutes,
                                 Long budgetCapacity) {
        if (nodes == null || nodes.size() < 2 || travelMinutes == null || travelMinutes.length != nodes.size()) {
            throw new IllegalArgumentException("行程求解节点或路线矩阵无效");
        }
        if (dayStartMinutes < 0 || dayEndMinutes > 24 * 60 || dayStartMinutes >= dayEndMinutes) {
            throw new IllegalArgumentException("行程求解日间时间窗无效");
        }
        if (budgetCapacity != null && budgetCapacity < 0) {
            throw new IllegalArgumentException("行程求解预算容量无效");
        }
        for (int index = 0; index < nodes.size(); index++) {
            if (travelMinutes[index] == null || travelMinutes[index].length != nodes.size()) {
                throw new IllegalArgumentException("行程求解路线矩阵维度无效");
            }
            Node node = nodes.get(index);
            if (node.serviceMinutes() < 0 || node.openMinutes() < dayStartMinutes || node.closeMinutes() > dayEndMinutes
                    || node.openMinutes() + node.serviceMinutes() > node.closeMinutes() || node.cost() < 0) {
                throw new IllegalArgumentException("行程求解节点时间窗或费用无效");
            }
        }
    }

    enum SelectionGroup {
        NONE,
        SCENIC,
        LUNCH,
        DINNER
    }

    record Node(String id, int serviceMinutes, int openMinutes, int closeMinutes, long cost, int utilityScore,
                boolean optional, SelectionGroup selectionGroup, String candidateKey) {

        Node(String id, int serviceMinutes, int openMinutes, int closeMinutes, long cost, int utilityScore,
             boolean optional) {
            this(id, serviceMinutes, openMinutes, closeMinutes, cost, utilityScore, optional, SelectionGroup.NONE, "");
        }
    }

    record Visit(String nodeId, int startMinutes, int endMinutes, int travelMinutesFromPrevious) {
    }

    record Result(List<Visit> visits, Set<String> droppedNodeIds, int endMinutes) {
    }

}
