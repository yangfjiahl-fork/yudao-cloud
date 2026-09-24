package cn.iocoder.yudao.module.gift.service.itinerary;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItineraryOrToolsRouteOptimizerTest {

    @Test
    void solve_shouldHonorTimeWindowsAndDropOnlyOptionalScenicNode() {
        List<ItineraryOrToolsRouteOptimizer.Node> nodes = List.of(
                new ItineraryOrToolsRouteOptimizer.Node("hotel", 0, 540, 1200, 0, 0, false),
                new ItineraryOrToolsRouteOptimizer.Node("morning", 60, 540, 660, 0, 100, false),
                new ItineraryOrToolsRouteOptimizer.Node("lunch", 60, 690, 810, 80, 0, false),
                new ItineraryOrToolsRouteOptimizer.Node("too-late-scenic", 60, 540, 660, 0, 1, true),
                new ItineraryOrToolsRouteOptimizer.Node("afternoon", 60, 840, 1080, 0, 90, false)
        );
        long[][] matrix = {
                {0, 10, 10, 90, 10},
                {10, 0, 10, 90, 10},
                {10, 10, 0, 90, 10},
                {90, 90, 90, 0, 90},
                {10, 10, 10, 90, 0}
        };

        ItineraryOrToolsRouteOptimizer.Result result = ItineraryOrToolsRouteOptimizer.solve(nodes, matrix, 540, 1200, 500L);

        assertNotNull(result);
        assertEquals(List.of("morning", "lunch", "afternoon"), result.visits().stream()
                .map(ItineraryOrToolsRouteOptimizer.Visit::nodeId).toList());
        assertEquals(550, result.visits().get(0).startMinutes());
        assertEquals(690, result.visits().get(1).startMinutes());
        assertTrue(result.droppedNodeIds().contains("too-late-scenic"));
    }

    @Test
    void solve_shouldSelectRequiredNumbersForEachCandidateGroup() {
        List<ItineraryOrToolsRouteOptimizer.Node> nodes = List.of(
                new ItineraryOrToolsRouteOptimizer.Node("hotel", 0, 540, 1200, 0, 0, false),
                new ItineraryOrToolsRouteOptimizer.Node("scenic-1", 120, 540, 1200, 0, 100, false,
                        ItineraryOrToolsRouteOptimizer.SelectionGroup.SCENIC, "scenic-1"),
                new ItineraryOrToolsRouteOptimizer.Node("scenic-2", 120, 540, 1200, 0, 90, true,
                        ItineraryOrToolsRouteOptimizer.SelectionGroup.SCENIC, "scenic-2"),
                new ItineraryOrToolsRouteOptimizer.Node("lunch", 60, 690, 810, 80, 40, true,
                        ItineraryOrToolsRouteOptimizer.SelectionGroup.LUNCH, "food-1"),
                new ItineraryOrToolsRouteOptimizer.Node("dinner", 60, 1050, 1170, 100, 40, true,
                        ItineraryOrToolsRouteOptimizer.SelectionGroup.DINNER, "food-2"));
        long[][] matrix = {
                {0, 10, 10, 10, 10}, {10, 0, 10, 10, 10}, {10, 10, 0, 10, 10},
                {10, 10, 10, 0, 10}, {10, 10, 10, 10, 0}
        };

        ItineraryOrToolsRouteOptimizer.Result result = ItineraryOrToolsRouteOptimizer.solve(nodes, matrix, 540, 1200, 500L,
                Map.of(ItineraryOrToolsRouteOptimizer.SelectionGroup.SCENIC, 2,
                        ItineraryOrToolsRouteOptimizer.SelectionGroup.LUNCH, 1,
                        ItineraryOrToolsRouteOptimizer.SelectionGroup.DINNER, 1));

        assertNotNull(result);
        assertEquals(4, result.visits().size());
    }

    @Test
    void solve_shouldHandleDailyCandidateCounts() {
        List<ItineraryOrToolsRouteOptimizer.Node> nodes = new java.util.ArrayList<>();
        nodes.add(new ItineraryOrToolsRouteOptimizer.Node("hotel", 0, 540, 1200, 0, 0, false));
        for (int index = 0; index < 6; index++) {
            nodes.add(new ItineraryOrToolsRouteOptimizer.Node("scenic-" + index, 180, 540, 1200, 0, 10, index != 0,
                    ItineraryOrToolsRouteOptimizer.SelectionGroup.SCENIC, "scenic-" + index));
        }
        for (int index = 0; index < 4; index++) {
            nodes.add(new ItineraryOrToolsRouteOptimizer.Node("lunch-" + index, 60, 690, 810, 80, 10, true,
                    ItineraryOrToolsRouteOptimizer.SelectionGroup.LUNCH, "food-" + index));
            nodes.add(new ItineraryOrToolsRouteOptimizer.Node("dinner-" + index, 60, 1050, 1170, 80, 10, true,
                    ItineraryOrToolsRouteOptimizer.SelectionGroup.DINNER, "food-" + index));
        }
        long[][] matrix = new long[nodes.size()][nodes.size()];
        for (int from = 0; from < nodes.size(); from++) {
            for (int to = 0; to < nodes.size(); to++) {
                matrix[from][to] = from == to ? 0 : 10;
            }
        }

        ItineraryOrToolsRouteOptimizer.Result result = ItineraryOrToolsRouteOptimizer.solve(nodes, matrix, 540, 1200, null,
                Map.of(ItineraryOrToolsRouteOptimizer.SelectionGroup.SCENIC, 2,
                        ItineraryOrToolsRouteOptimizer.SelectionGroup.LUNCH, 1,
                        ItineraryOrToolsRouteOptimizer.SelectionGroup.DINNER, 1));

        assertNotNull(result);
        assertEquals(4, result.visits().size());
    }

}
