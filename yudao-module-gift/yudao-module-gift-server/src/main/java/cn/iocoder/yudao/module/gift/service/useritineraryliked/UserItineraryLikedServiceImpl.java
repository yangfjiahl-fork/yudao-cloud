package cn.iocoder.yudao.module.gift.service.useritineraryliked;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.useritineraryliked.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryliked.UserItineraryLikedDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.useritineraryliked.UserItineraryLikedMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 收藏行程 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class UserItineraryLikedServiceImpl implements UserItineraryLikedService {

    @Resource
    private UserItineraryLikedMapper userItineraryLikedMapper;

    @Override
    public Long createUserItineraryLiked(UserItineraryLikedSaveReqVO createReqVO) {
        // 插入
        UserItineraryLikedDO userItineraryLiked = BeanUtils.toBean(createReqVO, UserItineraryLikedDO.class);
        userItineraryLikedMapper.insert(userItineraryLiked);

        // 返回
        return userItineraryLiked.getId();
    }

    @Override
    public void updateUserItineraryLiked(UserItineraryLikedSaveReqVO updateReqVO) {
        // 校验存在
        validateUserItineraryLikedExists(updateReqVO.getId());
        // 更新
        UserItineraryLikedDO updateObj = BeanUtils.toBean(updateReqVO, UserItineraryLikedDO.class);
        userItineraryLikedMapper.updateById(updateObj);
    }

    @Override
    public void deleteUserItineraryLiked(Long id) {
        // 校验存在
        validateUserItineraryLikedExists(id);
        // 删除
        userItineraryLikedMapper.deleteById(id);
    }

    @Override
        public void deleteUserItineraryLikedListByIds(List<Long> ids) {
        // 删除
        userItineraryLikedMapper.deleteByIds(ids);
        }


    private void validateUserItineraryLikedExists(Long id) {
        if (userItineraryLikedMapper.selectById(id) == null) {
            throw exception(USER_ITINERARY_LIKED_NOT_EXISTS);
        }
    }

    @Override
    public UserItineraryLikedDO getUserItineraryLiked(Long id) {
        return userItineraryLikedMapper.selectById(id);
    }

    @Override
    public PageResult<UserItineraryLikedDO> getUserItineraryLikedPage(UserItineraryLikedPageReqVO pageReqVO) {
        return userItineraryLikedMapper.selectPage(pageReqVO);
    }

}