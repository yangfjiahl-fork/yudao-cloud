package cn.iocoder.yudao.module.member.api.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.api.config.dto.MemberConfigRespDTO;
import cn.iocoder.yudao.module.member.api.config.dto.PriceTransDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.enums.ApiConstants;
import feign.FeignIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.dromara.core.trans.anno.AutoTrans;
import org.dromara.trans.service.AutoTransable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@FeignClient(name = ApiConstants.NAME) // TODO 芋艿：fallbackFactory =
@Tag(name = "RPC 服务 - 用户配置")
@AutoTrans(
        namespace = MemberConfigApi.PREFIX,
        fields = {"woolPrice"}
)
public interface MemberConfigApi extends AutoTransable<PriceTransDTO> {

    String PREFIX = ApiConstants.PREFIX + "/config";

    @GetMapping(PREFIX + "/get")
    @Operation(summary = "获得用户配置")
    CommonResult<MemberConfigRespDTO> getConfig();

    @Override
    default List<PriceTransDTO> selectByIds(List<?> ids) {
        return batchGet((List<Integer>) ids);
    }

    @Override
    default PriceTransDTO selectById(Object primaryValue) {
        return batchGet(Arrays.asList((Integer) primaryValue)).get(0);
    }

    @FeignIgnore
    List<PriceTransDTO> batchGet(List<Integer> ids);
}
