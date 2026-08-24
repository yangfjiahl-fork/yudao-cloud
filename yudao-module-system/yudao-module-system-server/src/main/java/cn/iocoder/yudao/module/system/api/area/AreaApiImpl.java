package cn.iocoder.yudao.module.system.api.area;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.system.service.area.AreaService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class AreaApiImpl implements AreaApi {

    @Resource
    private AreaService areaService;

    @Override
    public CommonResult<Long> getAreaId(String provinceName, String cityName, String districtName) {
        return success(areaService.getAreaId(provinceName, cityName, districtName));
    }

    @Override
    public CommonResult<String> getAreaName(Long areaId) {
        Area area = areaId != null ? AreaUtils.getArea(areaId.intValue()) : null;
        return success(area != null ? area.getName() : null);
    }

}
