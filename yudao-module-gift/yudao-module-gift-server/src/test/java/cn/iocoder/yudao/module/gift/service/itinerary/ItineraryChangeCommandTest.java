package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.iocoder.yudao.module.gift.service.itinerary.bo.ItineraryChangeCommand;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItineraryChangeCommandTest {

    @Test
    void shouldCreateSharedMoveCommandContract() {
        ItineraryChangeCommand command = new ItineraryChangeCommand(ItineraryChangeCommand.Operation.MOVE_ITEM,
                "item-1", 2, "AFTERNOON", 1, Map.of("reason", "用户拖动"));

        assertEquals(ItineraryChangeCommand.Operation.MOVE_ITEM, command.operation());
        assertEquals("item-1", command.itemId());
        assertEquals(2, command.day());
        assertEquals("AFTERNOON", command.timePeriod());
    }

}
