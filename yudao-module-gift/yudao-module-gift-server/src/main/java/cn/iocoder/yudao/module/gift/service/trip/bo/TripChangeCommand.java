package cn.iocoder.yudao.module.gift.service.trip.bo;

import java.util.Map;

/** App 手动编辑与 Agent 自然语言编辑共用的行程变更命令。 */
public record TripChangeCommand(Operation operation, int baseVersion, String itemId, Integer day,
                                String timePeriod, Integer sort, Map<String, Object> values) {

    public TripChangeCommand {
        if (operation == null) {
            throw new IllegalArgumentException("行程变更操作不能为空");
        }
        if (baseVersion <= 0) {
            throw new IllegalArgumentException("baseVersion 必须大于 0");
        }
        values = values == null ? Map.of() : Map.copyOf(values);
    }

    public enum Operation {
        ADD_ITEM,
        REMOVE_ITEM,
        REPLACE_ITEM,
        MOVE_ITEM,
        UPDATE_ITEM,
        LOCK_ITEM,
        UNLOCK_ITEM,
        REPLAN_DAY,
        REPLAN_TRIP
    }

}
