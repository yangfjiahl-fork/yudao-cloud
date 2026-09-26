package cn.iocoder.yudao.module.gift.controller.common.vo;

import cn.iocoder.yudao.module.gift.dal.dataobject.itinerarycategory.ItineraryCategoryDO;
import cn.iocoder.yudao.module.system.api.area.AreaApi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;
import org.dromara.core.trans.vo.VO;

@Schema(description = "行程分页项 Response VO")
@Data
public class ItineraryPageRespVO implements VO {

    @Schema(description = "行程编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "城市编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "310100")
    @Trans(type = TransType.AUTO_TRANS, key = AreaApi.PREFIX,
            fields = {"cityName", "provinceName"}, refs = {"cityName", "provinceName"})
    private Integer cityId;

    @Schema(description = "城市名称", example = "上海市")
    private String cityName;

    @Schema(description = "省份名称", example = "上海市")
    private String provinceName;

    @Schema(description = "行程类别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @Trans(type = TransType.SIMPLE, target = ItineraryCategoryDO.class,
            fields = "title", ref = "categoryName")
    private Long categoryId;

    @Schema(description = "行程类别名称", example = "亲子游")
    private String categoryName;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "副标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String subTitle;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "封面图")
    private String coverUrl;

    @Schema(description = "封面宽度")
    private Integer coverWidth;

    @Schema(description = "封面高度")
    private Integer coverHeight;

    @Schema(description = "浏览数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long viewCnt;

    @Schema(description = "点赞数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long likeCnt;

}
