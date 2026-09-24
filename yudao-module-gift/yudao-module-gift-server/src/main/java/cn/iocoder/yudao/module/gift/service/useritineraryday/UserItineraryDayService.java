package cn.iocoder.yudao.module.gift.service.useritineraryday;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.useritineraryday.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDayDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 用户行程每日安排 Service 接口
 *
 * @author 羔享科技
 */
public interface UserItineraryDayService {

    /**
     * 创建用户行程每日安排
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUserItineraryDay(@Valid UserItineraryDaySaveReqVO createReqVO);

    /**
     * 更新用户行程每日安排
     *
     * @param updateReqVO 更新信息
     */
    void updateUserItineraryDay(@Valid UserItineraryDaySaveReqVO updateReqVO);

    /**
     * 删除用户行程每日安排
     *
     * @param id 编号
     */
    void deleteUserItineraryDay(Long id);

    /**
    * 批量删除用户行程每日安排
    *
    * @param ids 编号
    */
    void deleteUserItineraryDayListByIds(List<Long> ids);

    /**
     * 获得用户行程每日安排
     *
     * @param id 编号
     * @return 用户行程每日安排
     */
    UserItineraryDayDO getUserItineraryDay(Long id);

    /**
     * 获得用户行程每日安排分页
     *
     * @param pageReqVO 分页查询
     * @return 用户行程每日安排分页
     */
    PageResult<UserItineraryDayDO> getUserItineraryDayPage(UserItineraryDayPageReqVO pageReqVO);

}