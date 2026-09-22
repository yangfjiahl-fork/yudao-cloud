package cn.iocoder.yudao.module.gift.service.useritinerary;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.useritinerary.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.useritinerary.UserItineraryMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 用户行程 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class UserItineraryServiceImpl implements UserItineraryService {

    @Resource
    private UserItineraryMapper userItineraryMapper;

    @Override
    public Long createUserItinerary(UserItinerarySaveReqVO createReqVO) {
        // 插入
        UserItineraryDO userItinerary = BeanUtils.toBean(createReqVO, UserItineraryDO.class);
        userItineraryMapper.insert(userItinerary);

        // 返回
        return userItinerary.getId();
    }

    @Override
    public void updateUserItinerary(UserItinerarySaveReqVO updateReqVO) {
        // 校验存在
        validateUserItineraryExists(updateReqVO.getId());
        // 更新
        UserItineraryDO updateObj = BeanUtils.toBean(updateReqVO, UserItineraryDO.class);
        userItineraryMapper.updateById(updateObj);
    }

    @Override
    public void deleteUserItinerary(Long id) {
        // 校验存在
        validateUserItineraryExists(id);
        // 删除
        userItineraryMapper.deleteById(id);
    }

    @Override
        public void deleteUserItineraryListByIds(List<Long> ids) {
        // 删除
        userItineraryMapper.deleteByIds(ids);
        }


    private void validateUserItineraryExists(Long id) {
        if (userItineraryMapper.selectById(id) == null) {
            throw exception(USER_ITINERARY_NOT_EXISTS);
        }
    }

    @Override
    public UserItineraryDO getUserItinerary(Long id) {
        return userItineraryMapper.selectById(id);
    }

    @Override
    public PageResult<UserItineraryDO> getUserItineraryPage(UserItineraryPageReqVO pageReqVO) {
        return userItineraryMapper.selectPage(pageReqVO);
    }

}