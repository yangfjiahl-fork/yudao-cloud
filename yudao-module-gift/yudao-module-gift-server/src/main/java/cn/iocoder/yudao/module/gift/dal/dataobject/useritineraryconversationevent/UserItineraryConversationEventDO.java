package cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryconversationevent;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("gift_user_itinerary_conversation_event")
@KeySequence("gift_user_itinerary_conversation_event_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserItineraryConversationEventDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long conversationId;
    private String runId;
    private Long replyEventId;
    private Long userItineraryId;
    private String eventType;
    private String role;
    private String stage;
    private String content;
    private String payloadJson;
    private Integer status;

}
