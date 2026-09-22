package cn.iocoder.yudao.module.gift.service.itineraryitem;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.itineraryitem.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryitem.ItineraryItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 文章 Service 接口
 *
 * @author 羔享科技
 */
public interface ItineraryItemService {

    /**
     * 创建文章
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createItineraryItem(@Valid ItineraryItemSaveReqVO createReqVO);

    /**
     * 更新文章
     *
     * @param updateReqVO 更新信息
     */
    void updateItineraryItem(@Valid ItineraryItemSaveReqVO updateReqVO);

    /**
     * 删除文章
     *
     * @param id 编号
     */
    void deleteItineraryItem(Long id);

    /**
    * 批量删除文章
    *
    * @param ids 编号
    */
    void deleteItineraryItemListByIds(List<Long> ids);

    /**
     * 获得文章
     *
     * @param id 编号
     * @return 文章
     */
    ItineraryItemDO getItineraryItem(Long id);

    /**
     * 获得文章分页
     *
     * @param pageReqVO 分页查询
     * @return 文章分页
     */
    PageResult<ItineraryItemDO> getItineraryItemPage(ItineraryItemPageReqVO pageReqVO);

}