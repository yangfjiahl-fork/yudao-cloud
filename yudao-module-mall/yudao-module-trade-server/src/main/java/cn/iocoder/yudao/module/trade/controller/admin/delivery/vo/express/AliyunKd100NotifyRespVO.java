package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.express;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 阿里云快递 100 动态通知响应 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliyunKd100NotifyRespVO {

    private Boolean result;
    private String returnCode;
    private String message;

    public static AliyunKd100NotifyRespVO success() {
        return new AliyunKd100NotifyRespVO(true, "200", "提交成功");
    }

}
