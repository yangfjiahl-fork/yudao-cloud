package cn.iocoder.yudao.module.ai.api.travel.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AiTravelScenicSpotRespDTO {

    private String provider;
    private String externalId;
    private String name;
    private String address;
    private String longitude;
    private String latitude;
    private String imageUrl;

}
