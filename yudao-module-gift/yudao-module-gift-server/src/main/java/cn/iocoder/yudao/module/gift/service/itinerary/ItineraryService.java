package cn.iocoder.yudao.module.gift.service.itinerary;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.itinerary.vo.*;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryCityPageReqVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryPageReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerary.ItineraryDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 行程 Service 接口
 *
 * @author 羔享科技
 */
public interface ItineraryService {

    /**
     * 创建行程
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createItinerary(@Valid ItinerarySaveReqVO createReqVO);

    /**
     * 更新行程
     *
     * @param updateReqVO 更新信息
     */
    void updateItinerary(@Valid ItinerarySaveReqVO updateReqVO);

    /**
     * 删除行程
     *
     * @param id 编号
     */
    void deleteItinerary(Long id);

    /**
    * 批量删除行程
    *
    * @param ids 编号
    */
    void deleteItineraryListByIds(List<Long> ids);

    /**
     * 获得行程
     *
     * @param id 编号
     * @return 行程
     */
    ItineraryDO getItinerary(Long id);

    /**
     * 获得行程分页
     *
     * @param pageReqVO 分页查询
     * @return 行程分页
     */
    PageResult<ItineraryDO> getItineraryPage(ItineraryPageReqVO pageReqVO);

    /**
     * 获得 C 端通用行程分页
     *
     * @param pageReqVO 分页查询
     * @return 通用行程分页
     */
    PageResult<ItineraryDO> getItineraryPage(AppItineraryPageReqVO pageReqVO);

    /**
     * 按城市获得 C 端通用行程分页
     *
     * @param memberId 会员编号
     * @param pageReqVO 分页查询
     * @return 通用行程分页
     */
    PageResult<ItineraryDO> getItineraryCityPage(Long memberId, AppItineraryCityPageReqVO pageReqVO);

}
