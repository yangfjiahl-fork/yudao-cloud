package cn.iocoder.yudao.module.member.api.signin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.member.api.signin.dto.MemberSignInRecordRespDTO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import cn.iocoder.yudao.module.member.service.signin.MemberSignInRecordService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 会员签到记录 API 实现类
 *
 * @author calvin
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class MemberSignInRecordApiImpl implements MemberSignInRecordApi {

    @Resource
    private MemberSignInRecordService memberSignInRecordService;

    @Override
    public CommonResult<MemberSignInRecordRespDTO> createSignRecordForWool(Long userId) {
        MemberSignInRecordDO record = memberSignInRecordService.createSignRecordForWool(userId);
        return success(BeanUtils.toBean(record, MemberSignInRecordRespDTO.class));
    }

}
