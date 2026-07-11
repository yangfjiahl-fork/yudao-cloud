package cn.iocoder.yudao.module.system.service.sms;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.iocoder.yudao.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import cn.iocoder.yudao.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import cn.iocoder.yudao.module.system.api.sms.dto.code.SmsCodeValidateReqDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsCodeDO;
import cn.iocoder.yudao.module.system.dal.mysql.sms.SmsCodeMapper;
import cn.iocoder.yudao.module.system.enums.sms.SmsSceneEnum;
import cn.iocoder.yudao.module.system.framework.sms.config.SmsCodeProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.randomInt;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.isToday;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 短信验证码 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class SmsCodeServiceImpl implements SmsCodeService {

    @Resource
    private SmsCodeProperties smsCodeProperties;

    @Resource
    private SmsCodeMapper smsCodeMapper;

    @Resource
    private SmsSendService smsSendService;

    @Override
    public void sendSmsCode(SmsCodeSendReqDTO reqDTO) {
        log.info("[sendSmsCode][创建短信验证码，手机号({})，场景({})，测试模拟发送({})]",
                DesensitizedUtil.mobilePhone(reqDTO.getMobile()), reqDTO.getScene(), smsCodeProperties.isTestMockSend());
        SmsSceneEnum sceneEnum = SmsSceneEnum.getCodeByScene(reqDTO.getScene());
        Assert.notNull(sceneEnum, "验证码场景({}) 查找不到配置", reqDTO.getScene());
        // 创建验证码
        String code = createSmsCode(reqDTO.getMobile(), reqDTO.getScene(), reqDTO.getCreateIp());
        // 非测试模拟发送模式时，发送验证码
        if (!smsCodeProperties.isTestMockSend()) {
            smsSendService.sendSingleSms(reqDTO.getMobile(), null, null,
                    sceneEnum.getTemplateCode(), MapUtil.of("code", code));
            log.info("[sendSmsCode][短信验证码发送请求已提交，手机号({})，场景({})]",
                    DesensitizedUtil.mobilePhone(reqDTO.getMobile()), reqDTO.getScene());
        } else {
            log.info("[sendSmsCode][测试模拟发送已开启，跳过短信下发，手机号({})，场景({})]",
                    DesensitizedUtil.mobilePhone(reqDTO.getMobile()), reqDTO.getScene());
        }
    }

    private String createSmsCode(String mobile, Integer scene, String ip) {
        // 校验是否可以发送验证码，不用筛选场景
        SmsCodeDO lastSmsCode = smsCodeMapper.selectLastByMobile(mobile, null, null);
        if (lastSmsCode != null) {
            if (LocalDateTimeUtil.between(lastSmsCode.getCreateTime(), LocalDateTime.now()).toMillis()
                    < smsCodeProperties.getSendFrequency().toMillis()) { // 发送过于频繁
                log.warn("[createSmsCode][短信验证码发送过于频繁，手机号({})，场景({})]",
                        DesensitizedUtil.mobilePhone(mobile), scene);
                throw exception(SMS_CODE_SEND_TOO_FAST);
            }
            if (isToday(lastSmsCode.getCreateTime()) && // 必须是今天，才能计算超过当天的上限
                    lastSmsCode.getTodayIndex() >= smsCodeProperties.getSendMaximumQuantityPerDay()) { // 超过当天发送的上限。
                log.warn("[createSmsCode][短信验证码超过每日发送上限，手机号({})，场景({})，今日发送次数({})]",
                        DesensitizedUtil.mobilePhone(mobile), scene, lastSmsCode.getTodayIndex());
                throw exception(SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY);
            }
            // TODO 芋艿：提升，每个 IP 每天可发送数量
            // TODO 芋艿：提升，每个 IP 每小时可发送数量
        }

        // 创建验证码记录
        String code = smsCodeProperties.isTestMockSend()
                ? mobile.substring(mobile.length() - 4) + String.format("%02d", LocalDateTime.now().getHour())
                : String.format("%0" + smsCodeProperties.getEndCode().toString().length() + "d",
                randomInt(smsCodeProperties.getBeginCode(), smsCodeProperties.getEndCode() + 1));
        SmsCodeDO newSmsCode = SmsCodeDO.builder().mobile(mobile).code(code).scene(scene)
                .todayIndex(lastSmsCode != null && isToday(lastSmsCode.getCreateTime()) ? lastSmsCode.getTodayIndex() + 1 : 1)
                .createIp(ip).used(false).build();
        smsCodeMapper.insert(newSmsCode);
        log.info("[createSmsCode][短信验证码记录已创建，编号({})，手机号({})，场景({})，今日发送次数({})，验证码({})]",
                newSmsCode.getId(), DesensitizedUtil.mobilePhone(mobile), scene, newSmsCode.getTodayIndex(), code);
        return code;
    }

    @Override
    public void useSmsCode(SmsCodeUseReqDTO reqDTO) {
        log.info("[useSmsCode][使用短信验证码，手机号({})，场景({})]",
                DesensitizedUtil.mobilePhone(reqDTO.getMobile()), reqDTO.getScene());
        // 检测验证码是否有效
        SmsCodeDO lastSmsCode = validateSmsCode0(reqDTO.getMobile(), reqDTO.getCode(), reqDTO.getScene());
        // 使用验证码
        smsCodeMapper.updateById(SmsCodeDO.builder().id(lastSmsCode.getId())
                .used(true).usedTime(LocalDateTime.now()).usedIp(reqDTO.getUsedIp()).build());
        log.info("[useSmsCode][短信验证码已使用，编号({})，手机号({})，场景({})]",
                lastSmsCode.getId(), DesensitizedUtil.mobilePhone(reqDTO.getMobile()), reqDTO.getScene());
    }

    @Override
    public void validateSmsCode(SmsCodeValidateReqDTO reqDTO) {
        log.info("[validateSmsCode][校验短信验证码，手机号({})，场景({})]",
                DesensitizedUtil.mobilePhone(reqDTO.getMobile()), reqDTO.getScene());
        SmsCodeDO smsCode = validateSmsCode0(reqDTO.getMobile(), reqDTO.getCode(), reqDTO.getScene());
        log.info("[validateSmsCode][短信验证码校验通过，编号({})，手机号({})，场景({})]",
                smsCode.getId(), DesensitizedUtil.mobilePhone(reqDTO.getMobile()), reqDTO.getScene());
    }

    private SmsCodeDO validateSmsCode0(String mobile, String code, Integer scene) {
        // 校验验证码
        SmsCodeDO lastSmsCode = smsCodeMapper.selectLastByMobile(mobile, code, scene);
        // 若验证码不存在，抛出异常
        if (lastSmsCode == null) {
            log.warn("[validateSmsCode0][短信验证码不存在，手机号({})，场景({})]", DesensitizedUtil.mobilePhone(mobile), scene);
            throw exception(SMS_CODE_NOT_FOUND);
        }
        // 超过时间
        if (LocalDateTimeUtil.between(lastSmsCode.getCreateTime(), LocalDateTime.now()).toMillis()
                >= smsCodeProperties.getExpireTimes().toMillis()) { // 验证码已过期
            log.warn("[validateSmsCode0][短信验证码已过期，编号({})，手机号({})，场景({})]",
                    lastSmsCode.getId(), DesensitizedUtil.mobilePhone(mobile), scene);
            throw exception(SMS_CODE_EXPIRED);
        }
        // 判断验证码是否已被使用
        if (Boolean.TRUE.equals(lastSmsCode.getUsed())) {
            log.warn("[validateSmsCode0][短信验证码已使用，编号({})，手机号({})，场景({})]",
                    lastSmsCode.getId(), DesensitizedUtil.mobilePhone(mobile), scene);
            throw exception(SMS_CODE_USED);
        }
        return lastSmsCode;
    }

}
