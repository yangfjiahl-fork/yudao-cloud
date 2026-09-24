package cn.iocoder.yudao.module.gift.service.useritineraryday;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.useritineraryday.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryDayMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 用户行程每日安排 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class UserItineraryDayServiceImpl implements UserItineraryDayService {

    @Resource
    private UserItineraryDayMapper userItineraryDayMapper;

    @Override
    public Long createUserItineraryDay(UserItineraryDaySaveReqVO createReqVO) {
        // 插入
        UserItineraryDayDO userItineraryDay = BeanUtils.toBean(createReqVO, UserItineraryDayDO.class);
        userItineraryDayMapper.insert(userItineraryDay);

        // 返回
        return userItineraryDay.getId();
    }

    @Override
    public void updateUserItineraryDay(UserItineraryDaySaveReqVO updateReqVO) {
        // 校验存在
        validateUserItineraryDayExists(updateReqVO.getId());
        // 更新
        UserItineraryDayDO updateObj = BeanUtils.toBean(updateReqVO, UserItineraryDayDO.class);
        userItineraryDayMapper.updateById(updateObj);
    }

    @Override
    public void deleteUserItineraryDay(Long id) {
        // 校验存在
        validateUserItineraryDayExists(id);
        // 删除
        userItineraryDayMapper.deleteById(id);
    }

    @Override
        public void deleteUserItineraryDayListByIds(List<Long> ids) {
        // 删除
        userItineraryDayMapper.deleteByIds(ids);
        }


    private void validateUserItineraryDayExists(Long id) {
        if (userItineraryDayMapper.selectById(id) == null) {
            throw exception(USER_ITINERARY_DAY_NOT_EXISTS);
        }
    }

    @Override
    public UserItineraryDayDO getUserItineraryDay(Long id) {
        return userItineraryDayMapper.selectById(id);
    }

    @Override
    public PageResult<UserItineraryDayDO> getUserItineraryDayPage(UserItineraryDayPageReqVO pageReqVO) {
        return userItineraryDayMapper.selectPage(pageReqVO);
    }

}