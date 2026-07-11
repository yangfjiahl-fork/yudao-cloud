package cn.iocoder.yudao.module.gift.service.wool.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 签到生成羊毛结果 BO
 *
 * @author calvin
 */
@Data
@Accessors(chain = true)
public class SignInWoolResultBO {

    /**
     * 签到记录编号
     */
    private Long signInRecordId;
    /**
     * 羊毛编号
     */
    private Long woolId;
    /**
     * 签到第几天
     */
    private Integer day;
    /**
     * 羊毛数量
     */
    private Integer amount;
    /**
     * 经验
     */
    private Integer experience;
    /**
     * 签到时间
     */
    private LocalDateTime createTime;

}
