package cn.iocoder.yudao.module.gift.service.trip;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TripOrToolsRouteOptimizerTest {

    @Test
    void solve_shouldHonorTimeWindowsAndDropOnlyOptionalScenicNode() {
        List<TripOrToolsRouteOptimizer.Node> nodes = List.of(
                new TripOrToolsRouteOptimizer.Node("hotel", 0, 540, 1200, 0, 0, false),
                new TripOrToolsRouteOptimizer.Node("morning", 60, 540, 660, 0, 100, false),
                new TripOrToolsRouteOptimizer.Node("lunch", 60, 690, 810, 80, 0, false),
                new TripOrToolsRouteOptimizer.Node("too-late-scenic", 60, 540, 660, 0, 1, true),
                new TripOrToolsRouteOptimizer.Node("afternoon", 60, 840, 1080, 0, 90, false)
        );
        long[][] matrix = {
                {0, 10, 10, 90, 10},
                {10, 0, 10, 90, 10},
                {10, 10, 0, 90, 10},
                {90, 90, 90, 0, 90},
                {10, 10, 10, 90, 0}
        };

        TripOrToolsRouteOptimizer.Result result = TripOrToolsRouteOptimizer.solve(nodes, matrix, 540, 1200, 500L);

        assertNotNull(result);
        assertEquals(List.of("morning", "lunch", "afternoon"), result.visits().stream()
                .map(TripOrToolsRouteOptimizer.Visit::nodeId).toList());
        assertEquals(550, result.visits().get(0).startMinutes());
        assertEquals(690, result.visits().get(1).startMinutes());
        assertTrue(result.droppedNodeIds().contains("too-late-scenic"));
    }

    @Test
    void solve_shouldSelectRequiredNumbersForEachCandidateGroup() {
        List<TripOrToolsRouteOptimizer.Node> nodes = List.of(
                new TripOrToolsRouteOptimizer.Node("hotel", 0, 540, 1200, 0, 0, false),
                new TripOrToolsRouteOptimizer.Node("scenic-1", 120, 540, 1200, 0, 100, false,
                        TripOrToolsRouteOptimizer.SelectionGroup.SCENIC, "scenic-1"),
                new TripOrToolsRouteOptimizer.Node("scenic-2", 120, 540, 1200, 0, 90, true,
                        TripOrToolsRouteOptimizer.SelectionGroup.SCENIC, "scenic-2"),
                new TripOrToolsRouteOptimizer.Node("lunch", 60, 690, 810, 80, 40, true,
                        TripOrToolsRouteOptimizer.SelectionGroup.LUNCH, "food-1"),
                new TripOrToolsRouteOptimizer.Node("dinner", 60, 1050, 1170, 100, 40, true,
                        TripOrToolsRouteOptimizer.SelectionGroup.DINNER, "food-2"));
        long[][] matrix = {
                {0, 10, 10, 10, 10}, {10, 0, 10, 10, 10}, {10, 10, 0, 10, 10},
                {10, 10, 10, 0, 10}, {10, 10, 10, 10, 0}
        };

        TripOrToolsRouteOptimizer.Result result = TripOrToolsRouteOptimizer.solve(nodes, matrix, 540, 1200, 500L,
                Map.of(TripOrToolsRouteOptimizer.SelectionGroup.SCENIC, 2,
                        TripOrToolsRouteOptimizer.SelectionGroup.LUNCH, 1,
                        TripOrToolsRouteOptimizer.SelectionGroup.DINNER, 1));

        assertNotNull(result);
        assertEquals(4, result.visits().size());
    }

    @Test
    void solve_shouldHandleDailyCandidateCounts() {
        List<TripOrToolsRouteOptimizer.Node> nodes = new java.util.ArrayList<>();
        nodes.add(new TripOrToolsRouteOptimizer.Node("hotel", 0, 540, 1200, 0, 0, false));
        for (int index = 0; index < 6; index++) {
            nodes.add(new TripOrToolsRouteOptimizer.Node("scenic-" + index, 180, 540, 1200, 0, 10, index != 0,
                    TripOrToolsRouteOptimizer.SelectionGroup.SCENIC, "scenic-" + index));
        }
        for (int index = 0; index < 4; index++) {
            nodes.add(new TripOrToolsRouteOptimizer.Node("lunch-" + index, 60, 690, 810, 80, 10, true,
                    TripOrToolsRouteOptimizer.SelectionGroup.LUNCH, "food-" + index));
            nodes.add(new TripOrToolsRouteOptimizer.Node("dinner-" + index, 60, 1050, 1170, 80, 10, true,
                    TripOrToolsRouteOptimizer.SelectionGroup.DINNER, "food-" + index));
        }
        long[][] matrix = new long[nodes.size()][nodes.size()];
        for (int from = 0; from < nodes.size(); from++) {
            for (int to = 0; to < nodes.size(); to++) {
                matrix[from][to] = from == to ? 0 : 10;
            }
        }

        TripOrToolsRouteOptimizer.Result result = TripOrToolsRouteOptimizer.solve(nodes, matrix, 540, 1200, null,
                Map.of(TripOrToolsRouteOptimizer.SelectionGroup.SCENIC, 2,
                        TripOrToolsRouteOptimizer.SelectionGroup.LUNCH, 1,
                        TripOrToolsRouteOptimizer.SelectionGroup.DINNER, 1));

        assertNotNull(result);
        assertEquals(4, result.visits().size());
    }

}
