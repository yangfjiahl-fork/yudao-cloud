package cn.iocoder.yudao.module.gift.service.wool;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.gift.controller.admin.wool.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.wool.WoolDO;
import cn.iocoder.yudao.module.gift.dal.mysql.wool.WoolMapper;
import cn.iocoder.yudao.module.gift.enums.WoolStatusEnum;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.member.api.point.MemberPointApi;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 羊毛 Service 实现类
 *
 * @author calvin
 */
@Service
@Validated
@Slf4j
public class WoolServiceImpl implements WoolService {

    private static final String NEW_USER_WOOL_AMOUNT_CONFIG_KEY = "wool.amount.for.new.user";
    private static final String REGISTER_WOOL_REMARK = "新用户注册赠送羊毛";
    private static final String DEFAULT_WOOL_POINT_REMARK = "收取羊毛";

    @Resource
    private WoolMapper woolMapper;
    @Resource
    private ConfigApi configApi;
    @Resource
    private MemberPointApi memberPointApi;

    @Override
    public Long createWool(WoolSaveReqVO createReqVO) {
        // 插入
        WoolDO wool = BeanUtils.toBean(createReqVO, WoolDO.class);
        woolMapper.insert(wool);

        // 返回
        return wool.getId();
    }

    @Override
    public void updateWool(WoolSaveReqVO updateReqVO) {
        // 校验存在
        validateWoolExists(updateReqVO.getId());
        // 更新
        WoolDO updateObj = BeanUtils.toBean(updateReqVO, WoolDO.class);
        woolMapper.updateById(updateObj);
    }

    @Override
    public void deleteWool(Long id) {
        // 校验存在
        validateWoolExists(id);
        // 删除
        woolMapper.deleteById(id);
    }

    @Override
    public void deleteWoolListByIds(List<Long> ids) {
        // 删除
        woolMapper.deleteByIds(ids);
    }


    private void validateWoolExists(Long id) {
        if (woolMapper.selectById(id) == null) {
            throw exception(WOOL_NOT_EXISTS);
        }
    }

    @Override
    public WoolDO getWool(Long id) {
        return woolMapper.selectById(id);
    }

    @Override
    public PageResult<WoolDO> getWoolPage(WoolPageReqVO pageReqVO) {
        return woolMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WoolDO> getWaitReceiveWoolList(Long memberId) {
        return woolMapper.selectListByMemberIdAndStatus(memberId, WoolStatusEnum.INIT.getType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer receiveWool(Long memberId, Long id) {
        WoolDO wool = woolMapper.selectById(id);
        if (wool == null || !Objects.equals(wool.getMemberId(), memberId)) {
            throw exception(WOOL_NOT_EXISTS);
        }
        if (!Objects.equals(wool.getStatus(), WoolStatusEnum.INIT.getType())) {
            throw exception(WOOL_NOT_WAIT_RECEIVE);
        }

        int updateCount = woolMapper.updateStatusByIdAndMemberIdAndStatus(id, memberId,
                WoolStatusEnum.INIT.getType(), WoolStatusEnum.SUCCESS.getType());
        if (updateCount == 0) {
            throw exception(WOOL_NOT_WAIT_RECEIVE);
        }

        memberPointApi.addPoint(memberId, wool.getAmount(), wool.getBizType(),
                String.valueOf(wool.getId()),
                StrUtil.blankToDefault(wool.getRemark(), DEFAULT_WOOL_POINT_REMARK)).getCheckedData();
        log.info("[receiveWool][会员({}) 收取羊毛({}) 成功，增加积分({})]", memberId, id, wool.getAmount());
        return wool.getAmount();
    }

    @Override
    public Integer getTodayReceivedWoolAmount(Long memberId) {
        LocalDateTime today = LocalDateTimeUtils.getToday();
        return woolMapper.selectSumAmountByMemberIdAndStatusAndUpdateTimeBetween(memberId,
                WoolStatusEnum.SUCCESS.getType(), today, today.plusDays(1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantWoolByRegister(Long memberId) {
        MemberPointBizTypeEnum bizTypeEnum = MemberPointBizTypeEnum.REGISTER;
        String bizId = String.valueOf(memberId);
        WoolDO existsWool = woolMapper.selectByBizTypeAndBizId(bizTypeEnum.getType(), bizId);
        if (existsWool != null) {
            log.info("[grantWoolByRegister][会员({}) 已存在注册羊毛({})，跳过发放]", memberId, existsWool.getId());
            return;
        }

        WoolDO wool = WoolDO.builder()
                .bizType(bizTypeEnum.getType())
                .bizId(bizId)
                .amount(getNewUserWoolAmount())
                .remark(REGISTER_WOOL_REMARK)
                .status(WoolStatusEnum.INIT.getType())
                .memberId(memberId)
                .build();
        // 注册事件基于 @Async 异步执行，没有登录用户上下文，需要显式设置创建人
        wool.setCreator(String.valueOf(memberId));
        woolMapper.insert(wool);
        log.info("[grantWoolByRegister][会员({}) 注册羊毛({}) 发放成功，数量({})]",
                memberId, wool.getId(), wool.getAmount());
    }

    private int getNewUserWoolAmount() {
        String value = configApi.getConfigValueByKey(NEW_USER_WOOL_AMOUNT_CONFIG_KEY).getCheckedData();
        if (StrUtil.isBlank(value) || !NumberUtil.isInteger(value.trim())) {
            log.warn("[getNewUserWoolAmount][新用户羊毛数量配置无效，key({}) value({})]",
                    NEW_USER_WOOL_AMOUNT_CONFIG_KEY, value);
            throw exception(WOOL_NEW_USER_AMOUNT_CONFIG_INVALID);
        }
        int amount = Integer.parseInt(value.trim());
        if (amount <= 0) {
            log.warn("[getNewUserWoolAmount][新用户羊毛数量配置必须大于 0，key({}) value({})]",
                    NEW_USER_WOOL_AMOUNT_CONFIG_KEY, value);
            throw exception(WOOL_NEW_USER_AMOUNT_CONFIG_INVALID);
        }
        return amount;
    }

}
