package cn.iocoder.yudao.module.gift.service.itineraryday;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.itineraryday.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryday.ItineraryDayDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 通用行程每日安排 Service 接口
 *
 * @author 羔享科技
 */
public interface ItineraryDayService {

    /**
     * 创建通用行程每日安排
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createItineraryDay(@Valid ItineraryDaySaveReqVO createReqVO);

    /**
     * 更新通用行程每日安排
     *
     * @param updateReqVO 更新信息
     */
    void updateItineraryDay(@Valid ItineraryDaySaveReqVO updateReqVO);

    /**
     * 删除通用行程每日安排
     *
     * @param id 编号
     */
    void deleteItineraryDay(Long id);

    /**
    * 批量删除通用行程每日安排
    *
    * @param ids 编号
    */
    void deleteItineraryDayListByIds(List<Long> ids);

    /**
     * 获得通用行程每日安排
     *
     * @param id 编号
     * @return 通用行程每日安排
     */
    ItineraryDayDO getItineraryDay(Long id);

    /**
     * 获得通用行程每日安排分页
     *
     * @param pageReqVO 分页查询
     * @return 通用行程每日安排分页
     */
    PageResult<ItineraryDayDO> getItineraryDayPage(ItineraryDayPageReqVO pageReqVO);

}