package cn.iocoder.yudao.module.member.api.signin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.api.signin.dto.MemberSignInRecordRespDTO;
import cn.iocoder.yudao.module.member.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = ApiConstants.NAME) // TODO 芋艿：fallbackFactory =
@Tag(name = "RPC 服务 - 会员签到记录")
public interface MemberSignInRecordApi {

    String PREFIX = ApiConstants.PREFIX + "/sign-in-record";

    @PostMapping(PREFIX + "/create-for-wool")
    @Operation(summary = "为羊毛流程创建签到记录")
    CommonResult<MemberSignInRecordRespDTO> createSignRecordForWool(
            @Parameter(name = "userId", description = "会员编号", required = true, example = "1024")
            @RequestParam("userId") Long userId);

}
