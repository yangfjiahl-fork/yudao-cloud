package cn.iocoder.yudao.module.gift.service.itinerarycategory;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.itinerarycategory.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerarycategory.ItineraryCategoryDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.itinerarycategory.ItineraryCategoryMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 行程类别 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class ItineraryCategoryServiceImpl implements ItineraryCategoryService {

    @Resource
    private ItineraryCategoryMapper itineraryCategoryMapper;

    @Override
    public Long createItineraryCategory(ItineraryCategorySaveReqVO createReqVO) {
        // 插入
        ItineraryCategoryDO itineraryCategory = BeanUtils.toBean(createReqVO, ItineraryCategoryDO.class);
        itineraryCategoryMapper.insert(itineraryCategory);

        // 返回
        return itineraryCategory.getId();
    }

    @Override
    public void updateItineraryCategory(ItineraryCategorySaveReqVO updateReqVO) {
        // 校验存在
        validateItineraryCategoryExists(updateReqVO.getId());
        // 更新
        ItineraryCategoryDO updateObj = BeanUtils.toBean(updateReqVO, ItineraryCategoryDO.class);
        itineraryCategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteItineraryCategory(Long id) {
        // 校验存在
        validateItineraryCategoryExists(id);
        // 删除
        itineraryCategoryMapper.deleteById(id);
    }

    @Override
        public void deleteItineraryCategoryListByIds(List<Long> ids) {
        // 删除
        itineraryCategoryMapper.deleteByIds(ids);
        }


    private void validateItineraryCategoryExists(Long id) {
        if (itineraryCategoryMapper.selectById(id) == null) {
            throw exception(ITINERARY_CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public ItineraryCategoryDO getItineraryCategory(Long id) {
        return itineraryCategoryMapper.selectById(id);
    }

    @Override
    public PageResult<ItineraryCategoryDO> getItineraryCategoryPage(ItineraryCategoryPageReqVO pageReqVO) {
        return itineraryCategoryMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ItineraryCategoryDO> getItineraryCategoryList() {
        return itineraryCategoryMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
