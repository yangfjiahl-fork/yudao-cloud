package cn.iocoder.yudao.module.gift.service.itineraryitem;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.itineraryitem.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryitem.ItineraryItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.itineraryitem.ItineraryItemMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 文章 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class ItineraryItemServiceImpl implements ItineraryItemService {

    @Resource
    private ItineraryItemMapper itineraryItemMapper;

    @Override
    public Long createItineraryItem(ItineraryItemSaveReqVO createReqVO) {
        // 插入
        ItineraryItemDO itineraryItem = BeanUtils.toBean(createReqVO, ItineraryItemDO.class);
        itineraryItemMapper.insert(itineraryItem);

        // 返回
        return itineraryItem.getId();
    }

    @Override
    public void updateItineraryItem(ItineraryItemSaveReqVO updateReqVO) {
        // 校验存在
        validateItineraryItemExists(updateReqVO.getId());
        // 更新
        ItineraryItemDO updateObj = BeanUtils.toBean(updateReqVO, ItineraryItemDO.class);
        itineraryItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteItineraryItem(Long id) {
        // 校验存在
        validateItineraryItemExists(id);
        // 删除
        itineraryItemMapper.deleteById(id);
    }

    @Override
        public void deleteItineraryItemListByIds(List<Long> ids) {
        // 删除
        itineraryItemMapper.deleteByIds(ids);
        }


    private void validateItineraryItemExists(Long id) {
        if (itineraryItemMapper.selectById(id) == null) {
            throw exception(ITINERARY_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public ItineraryItemDO getItineraryItem(Long id) {
        return itineraryItemMapper.selectById(id);
    }

    @Override
    public PageResult<ItineraryItemDO> getItineraryItemPage(ItineraryItemPageReqVO pageReqVO) {
        return itineraryItemMapper.selectPage(pageReqVO);
    }

}