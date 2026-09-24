package cn.iocoder.yudao.module.gift.service.userfeedback;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.gift.controller.admin.userfeedback.vo.UserFeedbackPageReqVO;
import cn.iocoder.yudao.module.gift.controller.admin.userfeedback.vo.UserFeedbackSaveReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.userfeedback.UserFeedbackDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 用户反馈 Service 接口
 *
 * @author 羔享科技
 */
public interface UserFeedbackService {

    /**
     * 创建用户反馈
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUserFeedback(@Valid UserFeedbackSaveReqVO createReqVO);

    /**
     * 更新用户反馈
     *
     * @param updateReqVO 更新信息
     */
    void updateUserFeedback(@Valid UserFeedbackSaveReqVO updateReqVO);

    /**
     * 处理用户反馈
     *
     * @param id 用户反馈ID
     * @param processRemark 处理说明
     */
    void processUserFeedback(Long id, String processRemark);

    /**
     * 删除用户反馈
     *
     * @param id 编号
     */
    void deleteUserFeedback(Long id);

    /**
     * 批量删除用户反馈
     *
     * @param ids 编号
     */
    void deleteUserFeedbackListByIds(List<Long> ids);

    /**
     * 获得用户反馈
     *
     * @param id 编号
     * @return 用户反馈
     */
    UserFeedbackDO getUserFeedback(Long id);

    /**
     * 获得用户反馈分页
     *
     * @param pageReqVO 分页查询
     * @return 用户反馈分页
     */
    PageResult<UserFeedbackDO> getUserFeedbackPage(UserFeedbackPageReqVO pageReqVO);

}
