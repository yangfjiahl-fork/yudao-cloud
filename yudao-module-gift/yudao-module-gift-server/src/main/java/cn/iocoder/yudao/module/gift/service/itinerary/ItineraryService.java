package cn.iocoder.yudao.module.gift.service.itinerary;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.itinerary.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerary.ItineraryDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 线路 Service 接口
 *
 * @author 羔享科技
 */
public interface ItineraryService {

    /**
     * 创建线路
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createItinerary(@Valid ItinerarySaveReqVO createReqVO);

    /**
     * 更新线路
     *
     * @param updateReqVO 更新信息
     */
    void updateItinerary(@Valid ItinerarySaveReqVO updateReqVO);

    /**
     * 删除线路
     *
     * @param id 编号
     */
    void deleteItinerary(Long id);

    /**
    * 批量删除线路
    *
    * @param ids 编号
    */
    void deleteItineraryListByIds(List<Long> ids);

    /**
     * 获得线路
     *
     * @param id 编号
     * @return 线路
     */
    ItineraryDO getItinerary(Long id);

    /**
     * 获得线路分页
     *
     * @param pageReqVO 分页查询
     * @return 线路分页
     */
    PageResult<ItineraryDO> getItineraryPage(ItineraryPageReqVO pageReqVO);

}