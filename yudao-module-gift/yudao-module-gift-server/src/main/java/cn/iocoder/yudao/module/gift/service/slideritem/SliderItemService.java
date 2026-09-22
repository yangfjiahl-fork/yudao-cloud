package cn.iocoder.yudao.module.gift.service.slideritem;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.slideritem.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.slideritem.SliderItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 轮播图 Service 接口
 *
 * @author 羔享科技
 */
public interface SliderItemService {

    /**
     * 创建轮播图
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSliderItem(@Valid SliderItemSaveReqVO createReqVO);

    /**
     * 更新轮播图
     *
     * @param updateReqVO 更新信息
     */
    void updateSliderItem(@Valid SliderItemSaveReqVO updateReqVO);

    /**
     * 删除轮播图
     *
     * @param id 编号
     */
    void deleteSliderItem(Long id);

    /**
    * 批量删除轮播图
    *
    * @param ids 编号
    */
    void deleteSliderItemListByIds(List<Long> ids);

    /**
     * 获得轮播图
     *
     * @param id 编号
     * @return 轮播图
     */
    SliderItemDO getSliderItem(Long id);

    /**
     * 获得轮播图分页
     *
     * @param pageReqVO 分页查询
     * @return 轮播图分页
     */
    PageResult<SliderItemDO> getSliderItemPage(SliderItemPageReqVO pageReqVO);

}