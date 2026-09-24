package cn.iocoder.yudao.module.gift.service.useritinerarydayitem;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.useritinerarydayitem.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryDayItemMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 用户行程节点 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class UserItineraryDayItemServiceImpl implements UserItineraryDayItemService {

    @Resource
    private UserItineraryDayItemMapper userItineraryDayItemMapper;

    @Override
    public Long createUserItineraryDayItem(UserItineraryDayItemSaveReqVO createReqVO) {
        // 插入
        UserItineraryDayItemDO userItineraryDayItem = BeanUtils.toBean(createReqVO, UserItineraryDayItemDO.class);
        userItineraryDayItemMapper.insert(userItineraryDayItem);

        // 返回
        return userItineraryDayItem.getId();
    }

    @Override
    public void updateUserItineraryDayItem(UserItineraryDayItemSaveReqVO updateReqVO) {
        // 校验存在
        validateUserItineraryDayItemExists(updateReqVO.getId());
        // 更新
        UserItineraryDayItemDO updateObj = BeanUtils.toBean(updateReqVO, UserItineraryDayItemDO.class);
        userItineraryDayItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteUserItineraryDayItem(Long id) {
        // 校验存在
        validateUserItineraryDayItemExists(id);
        // 删除
        userItineraryDayItemMapper.deleteById(id);
    }

    @Override
        public void deleteUserItineraryDayItemListByIds(List<Long> ids) {
        // 删除
        userItineraryDayItemMapper.deleteByIds(ids);
        }


    private void validateUserItineraryDayItemExists(Long id) {
        if (userItineraryDayItemMapper.selectById(id) == null) {
            throw exception(USER_ITINERARY_DAY_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public UserItineraryDayItemDO getUserItineraryDayItem(Long id) {
        return userItineraryDayItemMapper.selectById(id);
    }

    @Override
    public PageResult<UserItineraryDayItemDO> getUserItineraryDayItemPage(UserItineraryDayItemPageReqVO pageReqVO) {
        return userItineraryDayItemMapper.selectPage(pageReqVO);
    }

}