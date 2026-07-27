package cn.iocoder.yudao.module.member.api.point;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.api.config.dto.PriceTransDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.enums.ApiConstants;
import feign.FeignIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.dromara.core.trans.anno.AutoTrans;
import org.dromara.trans.service.AutoTransable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.constraints.Min;

import java.util.Arrays;
import java.util.List;

@FeignClient(name = ApiConstants.NAME) // TODO 芋艿：fallbackFactory =
@Tag(name = "RPC 服务 - 用户积分")
public interface MemberPointApi {

    String PREFIX = ApiConstants.PREFIX + "/point";

    @PostMapping(PREFIX + "/add")
    @Operation(summary = "增加用户积分")
    @Parameters({
            @Parameter(name = "userId", description = "会员编号", required = true, example = "1024"),
            @Parameter(name = "point", description = "积分", required = true, example = "100"),
            @Parameter(name = "bizType", description = "业务类型", required = true, example = "1"),
            @Parameter(name = "bizId", description = "业务编号", required = true, example = "1"),
            @Parameter(name = "remark", description = "备注", example = "收取羊毛")
    })
    CommonResult<Boolean> addPoint(@RequestParam("userId") Long userId,
                                   @RequestParam("point") @Min(value = 1L, message = "积分必须是正数") Integer point,
                                   @RequestParam("bizType") Integer bizType,
                                   @RequestParam("bizId") String bizId,
                                   @RequestParam(value = "remark", required = false) String remark);

    default CommonResult<Boolean> addPoint(Long userId, Integer point, Integer bizType, String bizId) {
        return addPoint(userId, point, bizType, bizId, null);
    }

    @PostMapping(PREFIX + "/reducePoint")
    @Operation(summary = "减少用户积分")
    @Parameters({
            @Parameter(name = "userId", description = "会员编号", required = true, example = "1024"),
            @Parameter(name = "point", description = "积分", required = true, example = "100"),
            @Parameter(name = "bizType", description = "业务类型", required = true, example = "1"),
            @Parameter(name = "bizId", description = "业务编号", required = true, example = "1"),
            @Parameter(name = "remark", description = "备注", example = "撤销发放")
    })
    CommonResult<Boolean> reducePoint(@RequestParam("userId") Long userId,
                                      @RequestParam("point") @Min(value = 1L, message = "积分必须是正数") Integer point,
                                      @RequestParam("bizType") Integer bizType,
                                      @RequestParam("bizId") String bizId,
                                      @RequestParam(value = "remark", required = false) String remark);

    default CommonResult<Boolean> reducePoint(Long userId, Integer point, Integer bizType, String bizId) {
        return reducePoint(userId, point, bizType, bizId, null);
    }
}
