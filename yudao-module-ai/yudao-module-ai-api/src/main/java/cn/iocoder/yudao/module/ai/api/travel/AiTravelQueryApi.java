package cn.iocoder.yudao.module.ai.api.travel;

import cn.iocoder.yudao.module.ai.api.travel.dto.AiTravelScenicSpotRespDTO;
import cn.iocoder.yudao.module.ai.api.travel.dto.AiTravelWeatherRespDTO;

import java.util.List;

/**
 * 供业务模块使用的受控旅行数据查询入口。
 *
 * 具体供应商选择仍由 AI 模块读取系统配置完成，调用方不接触供应商密钥或协议。
 */
public interface AiTravelQueryApi {

    AiTravelWeatherRespDTO getCurrentWeather(String city);

    List<AiTravelScenicSpotRespDTO> queryScenicSpots(String city, String keyword, int limit);

}
