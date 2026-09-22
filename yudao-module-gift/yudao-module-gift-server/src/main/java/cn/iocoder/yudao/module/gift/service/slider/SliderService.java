package cn.iocoder.yudao.module.gift.service.slider;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.slider.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.slider.SliderDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 轮播 Service 接口
 *
 * @author 羔享科技
 */
public interface SliderService {

    /**
     * 创建轮播
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSlider(@Valid SliderSaveReqVO createReqVO);

    /**
     * 更新轮播
     *
     * @param updateReqVO 更新信息
     */
    void updateSlider(@Valid SliderSaveReqVO updateReqVO);

    /**
     * 删除轮播
     *
     * @param id 编号
     */
    void deleteSlider(Long id);

    /**
    * 批量删除轮播
    *
    * @param ids 编号
    */
    void deleteSliderListByIds(List<Long> ids);

    /**
     * 获得轮播
     *
     * @param id 编号
     * @return 轮播
     */
    SliderDO getSlider(Long id);

    /**
     * 获得轮播分页
     *
     * @param pageReqVO 分页查询
     * @return 轮播分页
     */
    PageResult<SliderDO> getSliderPage(SliderPageReqVO pageReqVO);

}