package cn.iocoder.yudao.module.gift.service.wool;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.wool.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.wool.WoolDO;
import cn.iocoder.yudao.module.gift.service.wool.bo.SignInWoolResultBO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 羊毛 Service 接口
 *
 * @author calvin
 */
public interface WoolService {

    /**
     * 创建羊毛
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWool(@Valid WoolSaveReqVO createReqVO);

    /**
     * 更新羊毛
     *
     * @param updateReqVO 更新信息
     */
    void updateWool(@Valid WoolSaveReqVO updateReqVO);

    /**
     * 删除羊毛
     *
     * @param id 编号
     */
    void deleteWool(Long id);

    /**
    * 批量删除羊毛
    *
    * @param ids 编号
    */
    void deleteWoolListByIds(List<Long> ids);

    /**
     * 获得羊毛
     *
     * @param id 编号
     * @return 羊毛
     */
    WoolDO getWool(Long id);

    /**
     * 获得羊毛分页
     *
     * @param pageReqVO 分页查询
     * @return 羊毛分页
     */
    PageResult<WoolDO> getWoolPage(WoolPageReqVO pageReqVO);

    /**
     * 获得会员待收取羊毛列表
     *
     * @param memberId 会员编号
     * @return 羊毛列表
     */
    List<WoolDO> getWaitReceiveWoolList(Long memberId);

    /**
     * 收取羊毛
     *
     * @param memberId 会员编号
     * @param id 羊毛编号
     */
    Integer receiveWool(Long memberId, Long id);

    /**
     * 获得会员今日获取羊毛总量
     *
     * @param memberId 会员编号
     * @return 今日获取羊毛总量
     */
    Integer getTodayReceivedWoolAmount(Long memberId);

    /**
     * 获得会员本月获取羊毛总量
     *
     * @param memberId 会员编号
     * @return 本月获取羊毛总量
     */
    Integer getThisMonthReceivedWoolAmount(Long memberId);

    /**
     * 获得会员历史获取羊毛总量
     *
     * @param memberId 会员编号
     * @return 历史获取羊毛总量
     */
    Integer getHistoryTotalReceivedWoolAmount(Long memberId);

    /**
     * 用户注册时，发放羊毛
     *
     * @param memberId 会员编号
     */
    void grantWoolByRegister(Long memberId);

    /**
     * 用户首次积分兑换成功时，发放邀请羊毛
     *
     * @param memberId 会员编号
     * @param orderId  积分兑换订单编号
     */
    void grantWoolByFirstPointExchange(Long memberId, Long orderId);

    /**
     * 用户签到时，生成待收取羊毛
     *
     * @param memberId 会员编号
     * @return 签到生成羊毛结果
     */
    SignInWoolResultBO createWoolBySignIn(Long memberId);

}
