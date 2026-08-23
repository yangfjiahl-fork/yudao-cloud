package cn.iocoder.yudao.module.ai.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.dal.dataobject.chat.AiChatConversationDO;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Chat Prompt 工具类
 */
public class AiChatPromptUtils {

    private AiChatPromptUtils() {
    }

    public static String appendRegionSystemMessage(String systemMessage, AiChatConversationDO conversation) {
        List<String> regions = new ArrayList<>(3);
        addRegion(regions, "省", conversation.getProvinceId());
        addRegion(regions, "市", conversation.getCityId());
        addRegion(regions, "区县", conversation.getDistrictId());
        if (CollUtil.isEmpty(regions)) {
            return systemMessage;
        }
        return StrUtil.format("{}\n\n当前会话默认地区：{}。当用户没有明确指定地区时，优先以此为准；"
                        + "用户明确指定其他地区时，以用户当轮描述为准。",
                StrUtil.blankToDefault(systemMessage, "你是一个严谨的助手。"), StrUtil.join("；", regions));
    }

    private static void addRegion(List<String> regions, String level, Long areaId) {
        if (areaId == null) {
            return;
        }
        regions.add(level + "编号：" + areaId);
    }

}
