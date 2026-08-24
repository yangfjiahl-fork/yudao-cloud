package cn.iocoder.yudao.module.ai.api.travel;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.api.travel.dto.AiTravelScenicSpotRespDTO;
import cn.iocoder.yudao.module.ai.api.travel.dto.AiTravelWeatherRespDTO;
import cn.iocoder.yudao.module.ai.framework.ai.core.scenic.ScenicSpotQueryClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.scenic.ScenicSpotQueryClientFacade;
import cn.iocoder.yudao.module.ai.framework.ai.core.weather.WeatherClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.weather.WeatherClientFacade;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** AI 模块内将供应商返回归一化后提供给业务模块。 */
@Service
public class AiTravelQueryApiImpl implements AiTravelQueryApi {

    @Resource
    private WeatherClientFacade weatherClientFacade;
    @Resource
    private ScenicSpotQueryClientFacade scenicSpotQueryClientFacade;
    @Resource
    private ConfigApi configApi;

    @Override
    public AiTravelWeatherRespDTO getCurrentWeather(String city) {
        WeatherClient.CurrentWeather weather = weatherClientFacade.getCurrentWeather(city);
        return new AiTravelWeatherRespDTO().setProvider(getProvider(WeatherClientFacade.QUERY_FROM_CONFIG_KEY))
                .setCity(weather.city()).setTemperature(weather.temperature()).setCondition(weather.condition())
                .setHumidity(weather.humidity()).setWindDirection(weather.windDirection())
                .setWindPower(weather.windPower()).setQueryTime(weather.queryTime());
    }

    @Override
    public List<AiTravelScenicSpotRespDTO> queryScenicSpots(String city, String keyword, int limit) {
        ScenicSpotQueryClient.Response response = scenicSpotQueryClientFacade.query(new ScenicSpotQueryClient.Request()
                .setType(ScenicSpotQueryClient.QueryType.SCENIC_SPOT).setRegion(city).setKeyword(keyword).setPage(1));
        if (!Boolean.TRUE.equals(response.getSuccess())) {
            throw new IllegalStateException(StrUtil.blankToDefault(response.getMessage(), "景点查询失败"));
        }
        String provider = getProvider(ScenicSpotQueryClientFacade.QUERY_FROM_CONFIG_KEY);
        List<AiTravelScenicSpotRespDTO> result = new ArrayList<>();
        JsonNode list = response.getData() == null ? null : response.getData().path("list");
        if (list == null || !list.isArray()) {
            return result;
        }
        for (JsonNode item : list) {
            if (result.size() >= Math.max(1, limit)) {
                break;
            }
            String name = text(item, "name", "scenicName", "title");
            if (StrUtil.isBlank(name)) {
                continue;
            }
            String location = text(item, "location");
            String[] coordinate = StrUtil.splitToArray(location, ',');
            result.add(new AiTravelScenicSpotRespDTO().setProvider(provider)
                    .setExternalId(text(item, "id", "scenicId", "code"))
                    .setName(name).setAddress(text(item, "address", "addr"))
                    .setLongitude(coordinate.length > 0 ? coordinate[0] : text(item, "longitude", "lng"))
                    .setLatitude(coordinate.length > 1 ? coordinate[1] : text(item, "latitude", "lat"))
                    .setImageUrl(firstImage(item)));
        }
        return result;
    }

    private String getProvider(String configKey) {
        String value = configApi.getConfigValueByKey(configKey).getCheckedData();
        return "gaode".equalsIgnoreCase(StrUtil.trim(value)) ? "gaode" : "aliyun";
    }

    private static String firstImage(JsonNode item) {
        String imageUrl = text(item, "imageUrl", "image", "imgUrl", "cover");
        if (StrUtil.isNotBlank(imageUrl)) {
            return imageUrl;
        }
        JsonNode photos = item.path("photos");
        if (photos.isArray() && !photos.isEmpty()) {
            return text(photos.get(0), "url", "imageUrl");
        }
        return "";
    }

    private static String text(JsonNode item, String... names) {
        for (String name : names) {
            String value = item.path(name).asText();
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }

}
