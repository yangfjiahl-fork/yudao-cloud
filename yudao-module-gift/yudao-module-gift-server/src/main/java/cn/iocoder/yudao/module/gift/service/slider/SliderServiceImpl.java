package cn.iocoder.yudao.module.gift.service.slider;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.slider.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.slider.SliderDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.slideritem.SliderItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.slider.SliderMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.slideritem.SliderItemMapper;
import cn.iocoder.yudao.module.gift.service.usercity.UserCityService;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 轮播 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class SliderServiceImpl implements SliderService {

    @Resource
    private SliderMapper sliderMapper;
    @Resource
    private SliderItemMapper sliderItemMapper;
    @Resource
    private UserCityService userCityService;

    @Override
    public Long createSlider(SliderSaveReqVO createReqVO) {
        // 插入
        SliderDO slider = BeanUtils.toBean(createReqVO, SliderDO.class);
        sliderMapper.insert(slider);

        // 返回
        return slider.getId();
    }

    @Override
    public void updateSlider(SliderSaveReqVO updateReqVO) {
        // 校验存在
        validateSliderExists(updateReqVO.getId());
        // 更新
        SliderDO updateObj = BeanUtils.toBean(updateReqVO, SliderDO.class);
        sliderMapper.updateById(updateObj);
    }

    @Override
    public void deleteSlider(Long id) {
        // 校验存在
        validateSliderExists(id);
        // 删除
        sliderMapper.deleteById(id);
    }

    @Override
        public void deleteSliderListByIds(List<Long> ids) {
        // 删除
        sliderMapper.deleteByIds(ids);
        }


    private void validateSliderExists(Long id) {
        if (sliderMapper.selectById(id) == null) {
            throw exception(SLIDER_NOT_EXISTS);
        }
    }

    @Override
    public SliderDO getSlider(Long id) {
        return sliderMapper.selectById(id);
    }

    @Override
    public List<SliderDO> getSliderList(Collection<Long> ids) {
        return sliderMapper.selectByIds(ids);
    }

    @Override
    public PageResult<SliderDO> getSliderPage(SliderPageReqVO pageReqVO) {
        return sliderMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SliderItemDO> getSliderItemList(Long memberId, String positionCode) {
        UserCityService.UserCity userCity = userCityService.getUserCity(memberId);
        if (userCity != null && userCity.cityId() != null) {
            List<SliderItemDO> citySliderItems = getSliderItemList(positionCode, userCity.cityId());
            if (CollUtil.isNotEmpty(citySliderItems)) {
                return citySliderItems;
            }
        }
        return getSliderItemList(positionCode, null);
    }

    private List<SliderItemDO> getSliderItemList(String positionCode, Long cityId) {
        SliderDO slider = sliderMapper.selectByPositionCodeAndCityId(positionCode, cityId);
        if (slider == null) {
            return Collections.emptyList();
        }
        return sliderItemMapper.selectListBySliderId(slider.getId());
    }

}
