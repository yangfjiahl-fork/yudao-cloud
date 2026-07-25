package cn.iocoder.yudao.module.system.service.area;

import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 地区 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AreaServiceImpl implements AreaService {

    @Override
    public Long getAreaId(String provinceName, String cityName, String districtName) {
        if (StringUtils.isAnyBlank(provinceName, cityName)) {
            Area area = AreaUtils.getArea(districtName);
            return area != null ? area.getId().longValue() : null;
        }

        Area province = getChildArea(AreaUtils.getArea(Area.ID_CHINA), provinceName);
        Area city = getChildArea(province, cityName);
        Area district = getChildArea(city, districtName);
        return district != null ? district.getId().longValue() : null;
    }

    private Area getChildArea(Area parent, String name) {
        if (parent == null || parent.getChildren() == null) {
            return null;
        }
        return parent.getChildren().stream().filter(area -> area.getName().equals(name)).findFirst().orElse(null);
    }

}
