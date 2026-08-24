package cn.iocoder.yudao.module.gift.dal.dataobject.trip;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 行程中可被检索工具和前端卡片共同引用的关键实体。 */
@TableName("gift_trip_entity")
@KeySequence("gift_trip_entity_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class TripEntityDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long tripId;
    /** CITY、POI、HOTEL、RESTAURANT、TRANSPORT_HUB。 */
    private String entityType;
    private String name;
    /** USER_INPUT、gaode、aliyun 或 mock_hotel。 */
    private String provider;
    private String externalId;
    private Long parentEntityId;
    private BigDecimal longitude;
    private BigDecimal latitude;
    /** 地址、图片地址等非易变展示元数据。 */
    private String metadataJson;
    /** 0=待解析，1=已解析，2=不可用。 */
    private Integer status;

}
