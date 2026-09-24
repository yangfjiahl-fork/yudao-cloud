package cn.iocoder.yudao.module.gift.service.useritineraryliked;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.useritineraryliked.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritineraryliked.UserItineraryLikedDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 收藏行程 Service 接口
 *
 * @author 羔享科技
 */
public interface UserItineraryLikedService {

    /**
     * 创建收藏行程
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUserItineraryLiked(@Valid UserItineraryLikedSaveReqVO createReqVO);

    /**
     * 更新收藏行程
     *
     * @param updateReqVO 更新信息
     */
    void updateUserItineraryLiked(@Valid UserItineraryLikedSaveReqVO updateReqVO);

    /**
     * 删除收藏行程
     *
     * @param id 编号
     */
    void deleteUserItineraryLiked(Long id);

    /**
    * 批量删除收藏行程
    *
    * @param ids 编号
    */
    void deleteUserItineraryLikedListByIds(List<Long> ids);

    /**
     * 获得收藏行程
     *
     * @param id 编号
     * @return 收藏行程
     */
    UserItineraryLikedDO getUserItineraryLiked(Long id);

    /**
     * 获得收藏行程分页
     *
     * @param pageReqVO 分页查询
     * @return 收藏行程分页
     */
    PageResult<UserItineraryLikedDO> getUserItineraryLikedPage(UserItineraryLikedPageReqVO pageReqVO);

}