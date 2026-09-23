package cn.iocoder.yudao.module.gift.dal.dataobject.itineraryday;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("gift_itinerary_day")
@KeySequence("gift_itinerary_day_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class ItineraryDayDO extends BaseDO {

    @TableId
    private Long id;
    private Long itineraryId;
    private Integer day;
    private Integer cityId;
    private Integer districtId;
    private String title;
    private String description;
    private Integer sort;

}
