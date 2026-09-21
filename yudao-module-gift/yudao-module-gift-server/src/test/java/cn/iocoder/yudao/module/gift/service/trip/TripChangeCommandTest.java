package cn.iocoder.yudao.module.gift.service.trip;

import cn.iocoder.yudao.module.gift.service.trip.bo.TripChangeCommand;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TripChangeCommandTest {

    @Test
    void shouldCreateSharedMoveCommandContract() {
        TripChangeCommand command = new TripChangeCommand(TripChangeCommand.Operation.MOVE_ITEM, 3,
                "item-1", 2, "AFTERNOON", 1, Map.of("reason", "用户拖动"));

        assertEquals(TripChangeCommand.Operation.MOVE_ITEM, command.operation());
        assertEquals(3, command.baseVersion());
        assertEquals("item-1", command.itemId());
        assertEquals(2, command.day());
        assertEquals("AFTERNOON", command.timePeriod());
    }

    @Test
    void shouldRejectMissingBaseVersion() {
        assertThrows(IllegalArgumentException.class, () -> new TripChangeCommand(
                TripChangeCommand.Operation.REPLAN_DAY, 0, null, 2, null, null, null));
    }

}
