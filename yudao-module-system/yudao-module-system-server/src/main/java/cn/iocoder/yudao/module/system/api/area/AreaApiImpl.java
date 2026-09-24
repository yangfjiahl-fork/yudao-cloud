package cn.iocoder.yudao.module.system.api.area;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.system.api.area.dto.AreaRespDTO;
import cn.iocoder.yudao.module.system.service.area.AreaService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

    @Override
    public CommonResult<AreaRespDTO> getArea(Long id) {
        return success(convertArea(id));
    }

    @Override
    public CommonResult<List<AreaRespDTO>> getAreaList(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return success(List.of());
        }
        return success(ids.stream()
                .map(this::convertArea)
                .filter(Objects::nonNull)
                .toList());
    }

    private AreaRespDTO convertArea(Long id) {
        if (id == null || id > Integer.MAX_VALUE || id < Integer.MIN_VALUE) {
            return null;
        }
        Area area = AreaUtils.getArea(id.intValue());
        if (area == null) {
            return null;
        }
        Area province = findAncestor(area, AreaTypeEnum.PROVINCE);
        Area city = findAncestor(area, AreaTypeEnum.CITY);
        return new AreaRespDTO()
                .setId(id)
                .setName(area.getName())
                .setProvinceName(province == null ? null : province.getName())
                .setCityName(city == null ? null : city.getName());
    }

    private static Area findAncestor(Area area, AreaTypeEnum type) {
        for (Area current = area; current != null; current = current.getParent()) {
            if (type.getType().equals(current.getType())) {
                return current;
            }
        }
        return null;
    }

}
