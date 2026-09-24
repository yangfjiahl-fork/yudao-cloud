package cn.iocoder.yudao.module.gift.dal.dataobject.userfeedback;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.gift.enums.UserFeedbackStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用户反馈 DO
 *
 * @author 羔享科技
 */
@TableName("gift_user_feedback")
@KeySequence("gift_user_feedback_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedbackDO extends BaseDO {

    /**
     * 用户反馈ID
     */
    @TableId
    private Long id;
    /**
     * 会员ID
     */
    private Long memberId;
    /**
     * 问题分类：0未知，10地名名称，20地点图片，30地点介绍，40营业时间，50地理位置，60电话，99其他建议
     */
    private Integer category;
    /**
     * 处理状态
     *
     * 枚举 {@link UserFeedbackStatusEnum}
     */
    private Integer status;
    /**
     * 反馈问题与建议
     */
    private String content;
    /**
     * POI供应商地点ID
     */
    private String poiId;
    /**
     * POI名称快照
     */
    private String poiName;
    /**
     * POI数据供应商
     */
    private String poiProvider;

}
