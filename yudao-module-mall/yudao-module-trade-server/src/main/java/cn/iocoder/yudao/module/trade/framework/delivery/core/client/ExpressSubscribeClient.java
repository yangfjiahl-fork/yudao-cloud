package cn.iocoder.yudao.module.trade.framework.delivery.core.client;

import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackNotifyRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.client.dto.ExpressTrackQueryReqDTO;

/**
 * 支持物流订阅的快递客户端接口
 */
public interface ExpressSubscribeClient extends ExpressClient {

    /**
     * 订阅物流动态
     *
     * @param reqDTO 订阅参数
     */
    void subscribeExpressTrack(ExpressTrackQueryReqDTO reqDTO);

    /**
     * 解析物流动态通知
     *
     * @param payload 通知报文
     * @return 物流动态
     */
    ExpressTrackNotifyRespDTO parseExpressTrackNotify(String payload);

}
