package cn.iocoder.yudao.module.gift.service.useritinerarydayitem;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.useritinerarydayitem.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 用户行程节点 Service 接口
 *
 * @author 羔享科技
 */
public interface UserItineraryDayItemService {

    /**
     * 创建用户行程节点
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUserItineraryDayItem(@Valid UserItineraryDayItemSaveReqVO createReqVO);

    /**
     * 更新用户行程节点
     *
     * @param updateReqVO 更新信息
     */
    void updateUserItineraryDayItem(@Valid UserItineraryDayItemSaveReqVO updateReqVO);

    /**
     * 删除用户行程节点
     *
     * @param id 编号
     */
    void deleteUserItineraryDayItem(Long id);

    /**
    * 批量删除用户行程节点
    *
    * @param ids 编号
    */
    void deleteUserItineraryDayItemListByIds(List<Long> ids);

    /**
     * 获得用户行程节点
     *
     * @param id 编号
     * @return 用户行程节点
     */
    UserItineraryDayItemDO getUserItineraryDayItem(Long id);

    /**
     * 获得用户行程节点分页
     *
     * @param pageReqVO 分页查询
     * @return 用户行程节点分页
     */
    PageResult<UserItineraryDayItemDO> getUserItineraryDayItemPage(UserItineraryDayItemPageReqVO pageReqVO);

}