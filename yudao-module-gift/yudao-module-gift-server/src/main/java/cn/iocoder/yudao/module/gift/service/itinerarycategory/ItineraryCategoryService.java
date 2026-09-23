package cn.iocoder.yudao.module.gift.service.itinerarycategory;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.itinerarycategory.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerarycategory.ItineraryCategoryDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 线路类别 Service 接口
 *
 * @author 羔享科技
 */
public interface ItineraryCategoryService {

    /**
     * 创建线路类别
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createItineraryCategory(@Valid ItineraryCategorySaveReqVO createReqVO);

    /**
     * 更新线路类别
     *
     * @param updateReqVO 更新信息
     */
    void updateItineraryCategory(@Valid ItineraryCategorySaveReqVO updateReqVO);

    /**
     * 删除线路类别
     *
     * @param id 编号
     */
    void deleteItineraryCategory(Long id);

    /**
    * 批量删除线路类别
    *
    * @param ids 编号
    */
    void deleteItineraryCategoryListByIds(List<Long> ids);

    /**
     * 获得线路类别
     *
     * @param id 编号
     * @return 线路类别
     */
    ItineraryCategoryDO getItineraryCategory(Long id);

    /**
     * 获得线路类别分页
     *
     * @param pageReqVO 分页查询
     * @return 线路类别分页
     */
    PageResult<ItineraryCategoryDO> getItineraryCategoryPage(ItineraryCategoryPageReqVO pageReqVO);

    /**
     * 获得 C 端行程类别列表
     *
     * @return 行程类别列表
     */
    List<ItineraryCategoryDO> getItineraryCategoryList();

}
