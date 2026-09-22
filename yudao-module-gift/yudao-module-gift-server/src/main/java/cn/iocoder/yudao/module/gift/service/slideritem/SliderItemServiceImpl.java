package cn.iocoder.yudao.module.gift.service.slideritem;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.slideritem.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.slideritem.SliderItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.slideritem.SliderItemMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 轮播图 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class SliderItemServiceImpl implements SliderItemService {

    @Resource
    private SliderItemMapper sliderItemMapper;

    @Override
    public Long createSliderItem(SliderItemSaveReqVO createReqVO) {
        // 插入
        SliderItemDO sliderItem = BeanUtils.toBean(createReqVO, SliderItemDO.class);
        sliderItemMapper.insert(sliderItem);

        // 返回
        return sliderItem.getId();
    }

    @Override
    public void updateSliderItem(SliderItemSaveReqVO updateReqVO) {
        // 校验存在
        validateSliderItemExists(updateReqVO.getId());
        // 更新
        SliderItemDO updateObj = BeanUtils.toBean(updateReqVO, SliderItemDO.class);
        sliderItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteSliderItem(Long id) {
        // 校验存在
        validateSliderItemExists(id);
        // 删除
        sliderItemMapper.deleteById(id);
    }

    @Override
        public void deleteSliderItemListByIds(List<Long> ids) {
        // 删除
        sliderItemMapper.deleteByIds(ids);
        }


    private void validateSliderItemExists(Long id) {
        if (sliderItemMapper.selectById(id) == null) {
            throw exception(SLIDER_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public SliderItemDO getSliderItem(Long id) {
        return sliderItemMapper.selectById(id);
    }

    @Override
    public PageResult<SliderItemDO> getSliderItemPage(SliderItemPageReqVO pageReqVO) {
        return sliderItemMapper.selectPage(pageReqVO);
    }

}