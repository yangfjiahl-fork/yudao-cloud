package cn.iocoder.yudao.module.gift.service.itinerarydayitem;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.itinerarydayitem.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerarydayitem.ItineraryDayItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 通用行程节点 Service 接口
 *
 * @author 羔享科技
 */
public interface ItineraryDayItemService {

    /**
     * 创建通用行程节点
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createItineraryDayItem(@Valid ItineraryDayItemSaveReqVO createReqVO);

    /**
     * 更新通用行程节点
     *
     * @param updateReqVO 更新信息
     */
    void updateItineraryDayItem(@Valid ItineraryDayItemSaveReqVO updateReqVO);

    /**
     * 删除通用行程节点
     *
     * @param id 编号
     */
    void deleteItineraryDayItem(Long id);

    /**
    * 批量删除通用行程节点
    *
    * @param ids 编号
    */
    void deleteItineraryDayItemListByIds(List<Long> ids);

    /**
     * 获得通用行程节点
     *
     * @param id 编号
     * @return 通用行程节点
     */
    ItineraryDayItemDO getItineraryDayItem(Long id);

    /**
     * 获得通用行程节点分页
     *
     * @param pageReqVO 分页查询
     * @return 通用行程节点分页
     */
    PageResult<ItineraryDayItemDO> getItineraryDayItemPage(ItineraryDayItemPageReqVO pageReqVO);

}