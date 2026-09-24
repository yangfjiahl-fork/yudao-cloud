package cn.iocoder.yudao.module.gift.service.itinerarydayitem;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.itinerarydayitem.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerarydayitem.ItineraryDayItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.itinerarydayitem.ItineraryDayItemMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 通用行程节点 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class ItineraryDayItemServiceImpl implements ItineraryDayItemService {

    @Resource
    private ItineraryDayItemMapper itineraryDayItemMapper;

    @Override
    public Long createItineraryDayItem(ItineraryDayItemSaveReqVO createReqVO) {
        // 插入
        ItineraryDayItemDO itineraryDayItem = BeanUtils.toBean(createReqVO, ItineraryDayItemDO.class);
        itineraryDayItemMapper.insert(itineraryDayItem);

        // 返回
        return itineraryDayItem.getId();
    }

    @Override
    public void updateItineraryDayItem(ItineraryDayItemSaveReqVO updateReqVO) {
        // 校验存在
        validateItineraryDayItemExists(updateReqVO.getId());
        // 更新
        ItineraryDayItemDO updateObj = BeanUtils.toBean(updateReqVO, ItineraryDayItemDO.class);
        itineraryDayItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteItineraryDayItem(Long id) {
        // 校验存在
        validateItineraryDayItemExists(id);
        // 删除
        itineraryDayItemMapper.deleteById(id);
    }

    @Override
        public void deleteItineraryDayItemListByIds(List<Long> ids) {
        // 删除
        itineraryDayItemMapper.deleteByIds(ids);
        }


    private void validateItineraryDayItemExists(Long id) {
        if (itineraryDayItemMapper.selectById(id) == null) {
            throw exception(ITINERARY_DAY_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public ItineraryDayItemDO getItineraryDayItem(Long id) {
        return itineraryDayItemMapper.selectById(id);
    }

    @Override
    public PageResult<ItineraryDayItemDO> getItineraryDayItemPage(ItineraryDayItemPageReqVO pageReqVO) {
        return itineraryDayItemMapper.selectPage(pageReqVO);
    }

}
