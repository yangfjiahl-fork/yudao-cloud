package cn.iocoder.yudao.module.ai.controller.admin.chat.vo.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 聊天对话创建【我的】 Request VO")
@Data
public class AiChatConversationCreateMyReqVO {

    @Schema(description = "聊天角色编号", example = "666")
    private Long roleId;

    @Schema(description = "知识库编号", example = "1204")
    private Long knowledgeId;

    @Schema(description = "省级地区编号", example = "330000")
    private Long provinceId;

    @Schema(description = "市级地区编号", example = "330100")
    private Long cityId;

    @Schema(description = "区县级地区编号", example = "330106")
    private Long districtId;

}
