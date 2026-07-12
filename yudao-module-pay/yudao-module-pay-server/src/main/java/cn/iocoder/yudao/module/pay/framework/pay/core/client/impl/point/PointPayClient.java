package cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.point;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.member.api.point.MemberPointApi;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.NonePayClientConfig;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.PAY_ORDER_EXTENSION_NOT_FOUND;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.REFUND_NOT_FOUND;

/**
 * 积分支付的 PayClient 实现类。
 *
 * 当前换算规则：1 积分 = 1 分。
 */
@Slf4j
public class PointPayClient extends AbstractPayClient<NonePayClientConfig> {

    public static final String USER_ID_KEY = "userId";
    public static final String USER_TYPE_KEY = "userType";

    private static final String POINT_PAY_CHANNEL_ORDER_NO_PREFIX = "POINT-P-";
    private static final String POINT_REFUND_CHANNEL_NO_PREFIX = "POINT-R-";

    private MemberPointApi memberPointApi;
    private PayOrderService orderService;
    private PayRefundService refundService;

    public PointPayClient(Long channelId, NonePayClientConfig config) {
        super(channelId, PayChannelEnum.POINT.getCode(), config);
    }

    @Override
    protected void doInit() {
        if (memberPointApi == null) {
            memberPointApi = SpringUtil.getBean(MemberPointApi.class);
        }
    }

    @Override
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        try {
            Long userId = getUserId(reqDTO);
            Integer userType = getUserType(reqDTO);
            Assert.notNull(userId, "积分支付会员编号不能为空");
            Assert.isTrue(Objects.equals(userType, UserTypeEnum.MEMBER.getValue()), "积分支付只支持会员用户");

            Integer point = convertPriceToPoint(reqDTO.getPrice());
            String bizId = getPayBizId(reqDTO);
            memberPointApi.reducePoint(userId, point, MemberPointBizTypeEnum.POINT_PAY.getType(),
                    bizId, "积分支付").checkError();
            return PayOrderRespDTO.successOf(POINT_PAY_CHANNEL_ORDER_NO_PREFIX + reqDTO.getOutTradeNo(),
                    String.valueOf(userId), LocalDateTime.now(), reqDTO.getOutTradeNo(), reqDTO);
        } catch (Throwable ex) {
            log.error("[doUnifiedOrder][reqDTO({}) 异常]", reqDTO, ex);
            Integer errorCode = INTERNAL_SERVER_ERROR.getCode();
            String errorMsg = INTERNAL_SERVER_ERROR.getMsg();
            if (ex instanceof ServiceException serviceException) {
                errorCode = serviceException.getCode();
                errorMsg = serviceException.getMessage();
            }
            return PayOrderRespDTO.closedOf(String.valueOf(errorCode), errorMsg,
                    reqDTO.getOutTradeNo(), "");
        }
    }

    @Override
    protected PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body, Map<String, String> headers) {
        throw new UnsupportedOperationException("积分支付无支付回调");
    }

    @Override
    protected PayOrderRespDTO doGetOrder(String outTradeNo) {
        PayOrderExtensionDO orderExtension = getOrderService().getOrderExtensionByNo(outTradeNo);
        if (orderExtension == null) {
            return PayOrderRespDTO.closedOf(String.valueOf(PAY_ORDER_EXTENSION_NOT_FOUND.getCode()),
                    PAY_ORDER_EXTENSION_NOT_FOUND.getMsg(), outTradeNo, "");
        }
        if (PayOrderStatusEnum.isClosed(orderExtension.getStatus())) {
            return PayOrderRespDTO.closedOf(orderExtension.getChannelErrorCode(),
                    orderExtension.getChannelErrorMsg(), outTradeNo, "");
        }
        if (PayOrderStatusEnum.isSuccess(orderExtension.getStatus())) {
            PayOrderDO order = getOrderService().getOrder(orderExtension.getOrderId());
            return PayOrderRespDTO.successOf(POINT_PAY_CHANNEL_ORDER_NO_PREFIX + outTradeNo,
                    order == null ? null : String.valueOf(order.getUserId()),
                    orderExtension.getUpdateTime(), outTradeNo, orderExtension);
        }
        log.error("[doGetOrder] 支付单 {} 的状态不正确", outTradeNo);
        throw new IllegalStateException(String.format("支付单[%s] 状态不正确", outTradeNo));
    }

    @Override
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        try {
            PayOrderExtensionDO orderExtension = getOrderService().getOrderExtensionByNo(reqDTO.getOutTradeNo());
            Assert.notNull(orderExtension, "支付拓展单不能为空");
            PayOrderDO order = getOrderService().getOrder(orderExtension.getOrderId());
            Assert.notNull(order, "支付单不能为空");
            Assert.notNull(order.getUserId(), "支付单会员编号不能为空");

            Integer point = convertPriceToPoint(reqDTO.getRefundPrice());
            memberPointApi.addPoint(order.getUserId(), point, MemberPointBizTypeEnum.POINT_PAY_REFUND.getType(),
                    "point-pay-refund:" + reqDTO.getOutRefundNo(), "积分支付退款").checkError();
            return PayRefundRespDTO.successOf(POINT_REFUND_CHANNEL_NO_PREFIX + reqDTO.getOutRefundNo(),
                    LocalDateTime.now(), reqDTO.getOutRefundNo(), reqDTO);
        } catch (Throwable ex) {
            log.error("[doUnifiedRefund][reqDTO({}) 异常]", reqDTO, ex);
            Integer errorCode = INTERNAL_SERVER_ERROR.getCode();
            String errorMsg = INTERNAL_SERVER_ERROR.getMsg();
            if (ex instanceof ServiceException serviceException) {
                errorCode = serviceException.getCode();
                errorMsg = serviceException.getMessage();
            }
            return PayRefundRespDTO.failureOf(String.valueOf(errorCode), errorMsg,
                    reqDTO.getOutRefundNo(), "");
        }
    }

    @Override
    protected PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body, Map<String, String> headers) {
        throw new UnsupportedOperationException("积分支付无退款回调");
    }

    @Override
    protected PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo) {
        PayRefundDO payRefund = getRefundService().getRefundByNo(outRefundNo);
        if (payRefund == null) {
            return PayRefundRespDTO.failureOf(String.valueOf(REFUND_NOT_FOUND.getCode()), REFUND_NOT_FOUND.getMsg(),
                    outRefundNo, "");
        }
        if (PayRefundStatusEnum.isFailure(payRefund.getStatus())) {
            return PayRefundRespDTO.failureOf(payRefund.getChannelErrorCode(), payRefund.getChannelErrorMsg(),
                    outRefundNo, "");
        }
        if (PayRefundStatusEnum.isSuccess(payRefund.getStatus())) {
            return PayRefundRespDTO.successOf(POINT_REFUND_CHANNEL_NO_PREFIX + outRefundNo,
                    payRefund.getUpdateTime(), outRefundNo, payRefund);
        }
        return PayRefundRespDTO.waitingOf(POINT_REFUND_CHANNEL_NO_PREFIX + outRefundNo, outRefundNo, payRefund);
    }

    @Override
    protected PayTransferRespDTO doUnifiedTransfer(PayTransferUnifiedReqDTO reqDTO) {
        throw new UnsupportedOperationException("积分支付不支持转账");
    }

    @Override
    protected PayTransferRespDTO doParseTransferNotify(Map<String, String> params, String body, Map<String, String> headers) {
        throw new UnsupportedOperationException("积分支付无转账回调");
    }

    @Override
    protected PayTransferRespDTO doGetTransfer(String outTradeNo) {
        throw new UnsupportedOperationException("积分支付不支持查询转账");
    }

    private Long getUserId(PayOrderUnifiedReqDTO reqDTO) {
        if (reqDTO.getUserId() != null) {
            return reqDTO.getUserId();
        }
        return MapUtil.getLong(reqDTO.getChannelExtras(), USER_ID_KEY);
    }

    private Integer getUserType(PayOrderUnifiedReqDTO reqDTO) {
        if (reqDTO.getUserType() != null) {
            return reqDTO.getUserType();
        }
        return MapUtil.getInt(reqDTO.getChannelExtras(), USER_TYPE_KEY);
    }

    private Integer convertPriceToPoint(Integer price) {
        Assert.notNull(price, "支付金额不能为空");
        Assert.isTrue(price > 0, "支付金额必须大于 0");
        return price;
    }

    private String getPayBizId(PayOrderUnifiedReqDTO reqDTO) {
        if (reqDTO.getOrderId() != null) {
            return "point-pay:" + reqDTO.getOrderId();
        }
        return "point-pay:" + reqDTO.getOutTradeNo();
    }

    private PayOrderService getOrderService() {
        if (orderService == null) {
            orderService = SpringUtil.getBean(PayOrderService.class);
        }
        return orderService;
    }

    private PayRefundService getRefundService() {
        if (refundService == null) {
            refundService = SpringUtil.getBean(PayRefundService.class);
        }
        return refundService;
    }

}
