package cn.iocoder.yudao.module.system.api.area;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = ApiConstants.NAME) // TODO 芋艿：fallbackFactory =
@Tag(name = "RPC 服务 - 地区")
public interface AreaApi {

    String PREFIX = ApiConstants.PREFIX + "/area";

    @GetMapping(PREFIX + "/get-id")
    @Operation(summary = "根据省市区名称获得地区编号")
    @Parameters({
            @Parameter(name = "provinceName", description = "省名称", example = "北京市", required = true),
            @Parameter(name = "cityName", description = "市名称", example = "北京市", required = true),
            @Parameter(name = "districtName", description = "区名称", example = "朝阳区", required = true)
    })
    CommonResult<Long> getAreaId(@RequestParam("provinceName") String provinceName,
                                 @RequestParam("cityName") String cityName,
                                 @RequestParam("districtName") String districtName);

    @GetMapping(PREFIX + "/get-name")
    @Operation(summary = "根据地区编号获得地区名称")
    CommonResult<String> getAreaName(@RequestParam("areaId") Long areaId);

}
