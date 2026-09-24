package cn.iocoder.yudao.module.system.api.area;

import cn.iocoder.yudao.module.system.api.area.dto.AreaRespDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AreaApiImplTest {

    private final AreaApiImpl areaApi = new AreaApiImpl();

    @Test
    void shouldReturnAreaHierarchyNames() {
        AreaRespDTO area = areaApi.getArea(330100L).getCheckedData();

        assertEquals(330100L, area.getId());
        assertEquals("杭州市", area.getName());
        assertEquals("杭州市", area.getCityName());
        assertEquals("浙江省", area.getProvinceName());
    }

    @Test
    void shouldSupportBatchAutoTranslationLookup() {
        List<AreaRespDTO> areas = areaApi.getAreaList(List.of(330100L, 999999L)).getCheckedData();

        assertEquals(1, areas.size());
        assertEquals("杭州市", areas.get(0).getCityName());
        assertEquals("浙江省", areas.get(0).getProvinceName());
    }

}
