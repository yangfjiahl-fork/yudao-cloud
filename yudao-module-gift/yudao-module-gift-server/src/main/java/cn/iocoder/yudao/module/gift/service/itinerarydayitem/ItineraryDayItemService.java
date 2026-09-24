package cn.iocoder.yudao.module.gift.service.itinerarydayitem;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.itinerarydayitem.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerarydayitem.ItineraryDayItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 文章 Service 接口
 *
 * @author 羔享科技
 */
public interface ItineraryDayItemService {

    /**
     * 创建文章
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createItineraryDayItem(@Valid ItineraryDayItemSaveReqVO createReqVO);

    /**
     * 更新文章
     *
     * @param updateReqVO 更新信息
     */
    void updateItineraryDayItem(@Valid ItineraryDayItemSaveReqVO updateReqVO);

    /**
     * 删除文章
     *
     * @param id 编号
     */
    void deleteItineraryDayItem(Long id);

    /**
    * 批量删除文章
    *
    * @param ids 编号
    */
    void deleteItineraryDayItemListByIds(List<Long> ids);

    /**
     * 获得文章
     *
     * @param id 编号
     * @return 文章
     */
    ItineraryDayItemDO getItineraryDayItem(Long id);

    /**
     * 获得文章分页
     *
     * @param pageReqVO 分页查询
     * @return 文章分页
     */
    PageResult<ItineraryDayItemDO> getItineraryDayItemPage(ItineraryDayItemPageReqVO pageReqVO);

}