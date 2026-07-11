package cn.iocoder.yudao.module.member.service.auth;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.DesensitizedUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.*;
import cn.iocoder.yudao.module.member.convert.auth.AuthConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import cn.iocoder.yudao.module.system.api.logger.LoginLogApi;
import cn.iocoder.yudao.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import cn.iocoder.yudao.module.system.api.sms.SmsCodeApi;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.system.api.social.SocialUserApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserRespDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxPhoneNumberInfoRespDTO;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;
import cn.iocoder.yudao.module.system.enums.oauth2.OAuth2ClientConstants;
import cn.iocoder.yudao.module.system.enums.sms.SmsSceneEnum;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getTerminal;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.*;

/**
 * 会员的认证 Service 接口
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class MemberAuthServiceImpl implements MemberAuthService {

    @Resource
    private MemberUserService userService;
    @Resource
    private SmsCodeApi smsCodeApi;
    @Resource
    private LoginLogApi loginLogApi;
    @Resource
    private SocialUserApi socialUserApi;
    @Resource
    private SocialClientApi socialClientApi;
    @Resource
    private OAuth2TokenCommonApi oauth2TokenApi;

    @Override
    public AppAuthLoginRespVO login(AppAuthLoginReqVO reqVO) {
        log.info("[login][会员密码登录，手机号({})]", DesensitizedUtil.mobilePhone(reqVO.getMobile()));
        // 使用手机 + 密码，进行登录。
        MemberUserDO user = login0(reqVO.getMobile(), reqVO.getPassword());

        // 如果 socialType 非空，说明需要绑定社交用户
        String openid = null;
        if (reqVO.getSocialType() != null) {
            log.info("[login][会员({}) 绑定社交账号，社交类型({})]", user.getId(), reqVO.getSocialType());
            openid = socialUserApi.bindSocialUser(new SocialUserBindReqDTO(user.getId(), getUserType().getValue(),
                    reqVO.getSocialType(), reqVO.getSocialCode(), reqVO.getSocialState())).getCheckedData();
        }

        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(user, reqVO.getMobile(), LoginLogTypeEnum.LOGIN_MOBILE, openid);
    }

    @Override
    @Transactional
    public AppAuthLoginRespVO smsLogin(AppAuthSmsLoginReqVO reqVO) {
        log.info("[smsLogin][会员短信登录，手机号({})]", DesensitizedUtil.mobilePhone(reqVO.getMobile()));
        // 校验验证码
        String userIp = getClientIP();
        smsCodeApi.useSmsCode(AuthConvert.INSTANCE.convert(reqVO, SmsSceneEnum.MEMBER_LOGIN.getScene(), userIp)).checkError();
        log.info("[smsLogin][会员短信验证码校验通过，手机号({})]", DesensitizedUtil.mobilePhone(reqVO.getMobile()));

        // 获得获得注册用户
        MemberUserDO user = userService.createUserIfAbsent(reqVO.getMobile(), userIp, getTerminal());
        Assert.notNull(user, "获取用户失败，结果为空");

        // 校验是否禁用
        if (CommonStatusEnum.isDisable(user.getStatus())) {
            log.warn("[smsLogin][会员({}) 已被禁用，拒绝登录]", user.getId());
            createLoginLog(user.getId(), reqVO.getMobile(), LoginLogTypeEnum.LOGIN_SMS, LoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        }

        // 如果 socialType 非空，说明需要绑定社交用户
        String openid = null;
        if (reqVO.getSocialType() != null) {
            log.info("[smsLogin][会员({}) 绑定社交账号，社交类型({})]", user.getId(), reqVO.getSocialType());
            openid = socialUserApi.bindSocialUser(new SocialUserBindReqDTO(user.getId(), getUserType().getValue(),
                    reqVO.getSocialType(), reqVO.getSocialCode(), reqVO.getSocialState())).getCheckedData();
        }

        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(user, reqVO.getMobile(), LoginLogTypeEnum.LOGIN_SMS, openid);
    }

    @Override
    @Transactional
    public AppAuthLoginRespVO socialLogin(AppAuthSocialLoginReqVO reqVO) {
        log.info("[socialLogin][会员社交登录，社交类型({})]", reqVO.getType());
        // 使用 code 授权码，进行登录。然后，获得到绑定的用户编号
        SocialUserRespDTO socialUser = socialUserApi.getSocialUserByCode(UserTypeEnum.MEMBER.getValue(), reqVO.getType(),
                reqVO.getCode(), reqVO.getState()).getCheckedData();
        if (socialUser == null) {
            log.warn("[socialLogin][未找到社交用户，社交类型({})]", reqVO.getType());
            throw exception(AUTH_SOCIAL_USER_NOT_FOUND);
        }

        // 情况一：已绑定，直接读取用户信息
        MemberUserDO user;
        if (socialUser.getUserId() != null) {
            log.info("[socialLogin][社交账号已绑定会员({})]", socialUser.getUserId());
            user = userService.getUser(socialUser.getUserId());
            // 情况二：未绑定，注册用户 + 绑定用户
        } else {
            user = userService.createUser(socialUser.getNickname(), socialUser.getAvatar(), getClientIP(), getTerminal());
            log.info("[socialLogin][创建会员({}) 并绑定社交账号，社交类型({})]", user.getId(), reqVO.getType());
            socialUserApi.bindSocialUser(new SocialUserBindReqDTO(user.getId(), getUserType().getValue(),
                    reqVO.getType(), reqVO.getCode(), reqVO.getState())).checkError();
        }
        if (user == null) {
            log.warn("[socialLogin][社交账号绑定的会员不存在，会员编号({})]", socialUser.getUserId());
            throw exception(USER_NOT_EXISTS);
        }

        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(user, user.getMobile(), LoginLogTypeEnum.LOGIN_SOCIAL, socialUser.getOpenid());
    }

    @Override
    public AppAuthLoginRespVO weixinMiniAppLogin(AppAuthWeixinMiniAppLoginReqVO reqVO) {
        log.info("[weixinMiniAppLogin][会员微信小程序登录]");
        // 获得对应的手机号信息
        SocialWxPhoneNumberInfoRespDTO phoneNumberInfo = socialClientApi.getWxMaPhoneNumberInfo(
                UserTypeEnum.MEMBER.getValue(), reqVO.getPhoneCode()).getCheckedData();
        Assert.notNull(phoneNumberInfo, "获得手机信息失败，结果为空");

        // 获得获得注册用户
        MemberUserDO user = userService.createUserIfAbsent(phoneNumberInfo.getPurePhoneNumber(),
                getClientIP(), TerminalEnum.WECHAT_MINI_PROGRAM.getTerminal());
        Assert.notNull(user, "获取用户失败，结果为空");
        log.info("[weixinMiniAppLogin][获取会员({})，手机号({})]", user.getId(),
                DesensitizedUtil.mobilePhone(phoneNumberInfo.getPurePhoneNumber()));

        // 绑定社交用户
        log.info("[weixinMiniAppLogin][会员({}) 绑定微信小程序账号]", user.getId());
        String openid = socialUserApi.bindSocialUser(new SocialUserBindReqDTO(user.getId(), getUserType().getValue(),
                SocialTypeEnum.WECHAT_MINI_PROGRAM.getType(), reqVO.getLoginCode(), reqVO.getState())).getCheckedData();

        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(user, user.getMobile(), LoginLogTypeEnum.LOGIN_SOCIAL, openid);
    }

    private AppAuthLoginRespVO createTokenAfterLoginSuccess(MemberUserDO user, String mobile,
                                                            LoginLogTypeEnum logType, String openid) {
        // 插入登陆日志
        createLoginLog(user.getId(), mobile, logType, LoginResultEnum.SUCCESS);
        // 创建 Token 令牌
        OAuth2AccessTokenRespDTO accessTokenRespDTO = oauth2TokenApi.createAccessToken(new OAuth2AccessTokenCreateReqDTO()
                .setUserId(user.getId()).setUserType(getUserType().getValue())
                .setClientId(OAuth2ClientConstants.CLIENT_ID_DEFAULT)).getCheckedData();
        log.info("[createTokenAfterLoginSuccess][会员({}) 登录成功，登录类型({})，已创建访问令牌]", user.getId(), logType.getType());
        // 构建返回结果
        return AuthConvert.INSTANCE.convert(accessTokenRespDTO, openid);
    }

    @Override
    public String getSocialAuthorizeUrl(Integer type, String redirectUri) {
        log.info("[getSocialAuthorizeUrl][获取会员社交授权地址，社交类型({})]", type);
        return socialClientApi.getAuthorizeUrl(type, UserTypeEnum.MEMBER.getValue(), redirectUri).getCheckedData();
    }

    private MemberUserDO login0(String mobile, String password) {
        final LoginLogTypeEnum logTypeEnum = LoginLogTypeEnum.LOGIN_MOBILE;
        // 校验账号是否存在
        MemberUserDO user = userService.getUserByMobile(mobile);
        if (user == null) {
            log.warn("[login0][会员不存在，手机号({})]", DesensitizedUtil.mobilePhone(mobile));
            createLoginLog(null, mobile, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        if (!userService.isPasswordMatch(password, user.getPassword())) {
            log.warn("[login0][会员({}) 密码校验失败]", user.getId());
            createLoginLog(user.getId(), mobile, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        // 校验是否禁用
        if (CommonStatusEnum.isDisable(user.getStatus())) {
            log.warn("[login0][会员({}) 已被禁用，拒绝登录]", user.getId());
            createLoginLog(user.getId(), mobile, logTypeEnum, LoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        }
        return user;
    }

    private void createLoginLog(Long userId, String mobile, LoginLogTypeEnum logType, LoginResultEnum loginResult) {
        // 插入登录日志
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(logType.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(getUserType().getValue());
        reqDTO.setUsername(mobile);
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(getClientIP());
        reqDTO.setResult(loginResult.getResult());
        loginLogApi.createLoginLog(reqDTO).checkError();
        // 更新最后登录时间
        if (userId != null && Objects.equals(LoginResultEnum.SUCCESS.getResult(), loginResult.getResult())) {
            userService.updateUserLogin(userId, getClientIP());
        }
    }

    @Override
    public void logout(String token) {
        log.info("[logout][会员发起登出]");
        // 删除访问令牌
        OAuth2AccessTokenRespDTO accessTokenRespDTO = oauth2TokenApi.removeAccessToken(token).getCheckedData();
        if (accessTokenRespDTO == null) {
            log.warn("[logout][访问令牌不存在或已失效]");
            return;
        }
        // 删除成功，则记录登出日志
        log.info("[logout][会员({}) 登出成功]", accessTokenRespDTO.getUserId());
        createLogoutLog(accessTokenRespDTO.getUserId());
    }

    @Override
    public void sendSmsCode(Long userId, AppAuthSmsSendReqVO reqVO) {
        log.info("[sendSmsCode][发送会员短信验证码，会员编号({})，场景({})，手机号({})]", userId, reqVO.getScene(),
                DesensitizedUtil.mobilePhone(reqVO.getMobile()));
        // 情况 1：如果是修改手机场景，需要校验新手机号是否已经注册，说明不能使用该手机了
        if (Objects.equals(reqVO.getScene(), SmsSceneEnum.MEMBER_UPDATE_MOBILE.getScene())) {
            MemberUserDO user = userService.getUserByMobile(reqVO.getMobile());
            if (user != null && !Objects.equals(user.getId(), userId)) {
                log.warn("[sendSmsCode][手机号已被其他会员({}) 使用，拒绝发送修改手机号验证码]", user.getId());
                throw exception(AUTH_MOBILE_USED);
            }
        }
        // 情况 2：如果是重置密码场景，需要校验手机号是存在的
        if (Objects.equals(reqVO.getScene(), SmsSceneEnum.MEMBER_RESET_PASSWORD.getScene())) {
            MemberUserDO user = userService.getUserByMobile(reqVO.getMobile());
            if (user == null) {
                log.warn("[sendSmsCode][会员不存在，拒绝发送重置密码验证码，手机号({})]", DesensitizedUtil.mobilePhone(reqVO.getMobile()));
                throw exception(USER_MOBILE_NOT_EXISTS);
            }
        }
        // 情况 3：如果是修改密码场景，需要查询手机号，无需前端传递
        if (Objects.equals(reqVO.getScene(), SmsSceneEnum.MEMBER_UPDATE_PASSWORD.getScene())) {
            MemberUserDO user = userService.getUser(userId);
            // TODO 芋艿：后续 member user 手机非强绑定，这块需要做下调整；
            reqVO.setMobile(user.getMobile());
            log.info("[sendSmsCode][修改密码场景使用会员({}) 绑定的手机号({})]", userId,
                    DesensitizedUtil.mobilePhone(reqVO.getMobile()));
        }

        // 执行发送
        smsCodeApi.sendSmsCode(AuthConvert.INSTANCE.convert(reqVO).setCreateIp(getClientIP())).checkError();
        log.info("[sendSmsCode][会员短信验证码发送请求已提交，会员编号({})，场景({})]", userId, reqVO.getScene());
    }

    @Override
    public void validateSmsCode(Long userId, AppAuthSmsValidateReqVO reqVO) {
        log.info("[validateSmsCode][校验会员短信验证码，会员编号({})，场景({})，手机号({})]", userId, reqVO.getScene(),
                DesensitizedUtil.mobilePhone(reqVO.getMobile()));
        smsCodeApi.validateSmsCode(AuthConvert.INSTANCE.convert(reqVO));
        log.info("[validateSmsCode][会员短信验证码校验通过，会员编号({})，场景({})]", userId, reqVO.getScene());
    }

    @Override
    public AppAuthLoginRespVO refreshToken(String refreshToken) {
        log.info("[refreshToken][刷新会员访问令牌]");
        OAuth2AccessTokenRespDTO accessTokenDO = oauth2TokenApi.refreshAccessToken(refreshToken,
                OAuth2ClientConstants.CLIENT_ID_DEFAULT).getCheckedData();
        log.info("[refreshToken][会员({}) 访问令牌刷新成功]", accessTokenDO.getUserId());
        return AuthConvert.INSTANCE.convert(accessTokenDO, null);
    }

    private void createLogoutLog(Long userId) {
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(LoginLogTypeEnum.LOGOUT_SELF.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(getUserType().getValue());
        reqDTO.setUsername(getMobile(userId));
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(getClientIP());
        reqDTO.setResult(LoginResultEnum.SUCCESS.getResult());
        loginLogApi.createLoginLog(reqDTO).checkError();
    }

    private String getMobile(Long userId) {
        if (userId == null) {
            return null;
        }
        MemberUserDO user = userService.getUser(userId);
        return user != null ? user.getMobile() : null;
    }

    private UserTypeEnum getUserType() {
        return UserTypeEnum.MEMBER;
    }

}
