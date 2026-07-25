package cn.iocoder.yudao.module.system.service.area;

/**
 * 地区 Service 接口
 *
 * @author 芋道源码
 */
public interface AreaService {

    /**
     * 根据省市区名称获得地区编号
     *
     * @param provinceName 省名称
     * @param cityName 市名称
     * @param districtName 区名称
     * @return 地区编号，未找到时返回 null
     */
    Long getAreaId(String provinceName, String cityName, String districtName);

}
