package cn.iocoder.yudao.module.gift.dal.dataobject.itineraryconversation;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("gift_user_itinerary_conversation")
@KeySequence("gift_user_itinerary_conversation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class ItineraryConversationDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long memberId;
    private String title;
    private Boolean pinned;
    private Long provinceId;
    private Long cityId;
    private Long districtId;
    private Long currentUserItineraryId;
    private String intakeAgentSessionId;
    private String planAgentSessionId;
    private String stateJson;
    private String missingRequiredJson;
    private Integer status;

}
