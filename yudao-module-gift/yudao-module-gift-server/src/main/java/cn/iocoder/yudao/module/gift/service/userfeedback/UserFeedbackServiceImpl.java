package cn.iocoder.yudao.module.gift.service.userfeedback;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.gift.controller.admin.userfeedback.vo.UserFeedbackPageReqVO;
import cn.iocoder.yudao.module.gift.controller.admin.userfeedback.vo.UserFeedbackSaveReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.userfeedback.UserFeedbackDO;
import cn.iocoder.yudao.module.gift.dal.mysql.userfeedback.UserFeedbackMapper;
import cn.iocoder.yudao.module.gift.enums.UserFeedbackStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.USER_FEEDBACK_NOT_EXISTS;

/**
 * 用户反馈 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class UserFeedbackServiceImpl implements UserFeedbackService {

    @Resource
    private UserFeedbackMapper userFeedbackMapper;

    @Override
    public Long createUserFeedback(UserFeedbackSaveReqVO createReqVO) {
        UserFeedbackDO userFeedback = BeanUtils.toBean(createReqVO, UserFeedbackDO.class);
        userFeedback.setStatus(UserFeedbackStatusEnum.UNPROCESSED.getStatus());
        userFeedbackMapper.insert(userFeedback);
        return userFeedback.getId();
    }

    @Override
    public void updateUserFeedback(UserFeedbackSaveReqVO updateReqVO) {
        validateUserFeedbackExists(updateReqVO.getId());
        UserFeedbackDO updateObj = BeanUtils.toBean(updateReqVO, UserFeedbackDO.class);
        userFeedbackMapper.updateById(updateObj);
    }

    @Override
    public void processUserFeedback(Long id, String processRemark) {
        validateUserFeedbackExists(id);
        userFeedbackMapper.updateById(UserFeedbackDO.builder()
                .id(id)
                .status(UserFeedbackStatusEnum.PROCESSED.getStatus())
                .processRemark(processRemark)
                .build());
    }

    @Override
    public void deleteUserFeedback(Long id) {
        validateUserFeedbackExists(id);
        userFeedbackMapper.deleteById(id);
    }

    @Override
    public void deleteUserFeedbackListByIds(List<Long> ids) {
        userFeedbackMapper.deleteByIds(ids);
    }

    private void validateUserFeedbackExists(Long id) {
        if (userFeedbackMapper.selectById(id) == null) {
            throw exception(USER_FEEDBACK_NOT_EXISTS);
        }
    }

    @Override
    public UserFeedbackDO getUserFeedback(Long id) {
        return userFeedbackMapper.selectById(id);
    }

    @Override
    public PageResult<UserFeedbackDO> getUserFeedbackPage(UserFeedbackPageReqVO pageReqVO) {
        return userFeedbackMapper.selectPage(pageReqVO);
    }

}
