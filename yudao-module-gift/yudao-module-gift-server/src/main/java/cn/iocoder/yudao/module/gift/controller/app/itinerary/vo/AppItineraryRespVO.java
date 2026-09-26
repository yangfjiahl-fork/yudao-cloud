package cn.iocoder.yudao.module.gift.controller.app.itinerary.vo;

import cn.iocoder.yudao.module.gift.controller.common.vo.ItineraryPageRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Schema(description = "用户 APP - 通用行程详情 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppItineraryRespVO extends ItineraryPageRespVO {

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "图片地址")
    private String picUrls;

    @Schema(description = "图片尺寸")
    private String picSizes;

    @Schema(description = "第二城市ID", example = "320100")
    private Integer nextCityId;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
