package cn.iocoder.yudao.module.gift.service.useritinerary;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.useritinerary.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 用户行程 Service 接口
 *
 * @author 羔享科技
 */
public interface UserItineraryService {

    /**
     * 创建用户行程
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUserItinerary(@Valid UserItinerarySaveReqVO createReqVO);

    /**
     * 更新用户行程
     *
     * @param updateReqVO 更新信息
     */
    void updateUserItinerary(@Valid UserItinerarySaveReqVO updateReqVO);

    /**
     * 删除用户行程
     *
     * @param id 编号
     */
    void deleteUserItinerary(Long id);

    /**
     * 删除指定会员的用户行程
     *
     * @param memberId 会员编号
     * @param id 编号
     */
    void deleteUserItinerary(Long memberId, Long id);

    /**
    * 批量删除用户行程
    *
    * @param ids 编号
    */
    void deleteUserItineraryListByIds(List<Long> ids);

    /**
     * 获得用户行程
     *
     * @param id 编号
     * @return 用户行程
     */
    UserItineraryDO getUserItinerary(Long id);

    /**
     * 获得指定会员的用户行程
     *
     * @param memberId 会员编号
     * @param id 编号
     * @return 用户行程
     */
    UserItineraryDO getUserItinerary(Long memberId, Long id);

    /**
     * 获得用户行程分页
     *
     * @param pageReqVO 分页查询
     * @return 用户行程分页
     */
    PageResult<UserItineraryDO> getUserItineraryPage(UserItineraryPageReqVO pageReqVO);

    /**
     * 获得用户行程导出分页
     *
     * @param pageReqVO 分页查询
     * @return 完整字段的用户行程分页
     */
    PageResult<UserItineraryDO> getUserItineraryExportPage(UserItineraryPageReqVO pageReqVO);

    /**
     * 获得指定会员的用户行程分页
     *
     * @param memberId 会员编号
     * @param pageReqVO 分页查询
     * @return 用户行程分页
     */
    PageResult<UserItineraryDO> getUserItineraryPage(Long memberId, PageParam pageReqVO);

}
