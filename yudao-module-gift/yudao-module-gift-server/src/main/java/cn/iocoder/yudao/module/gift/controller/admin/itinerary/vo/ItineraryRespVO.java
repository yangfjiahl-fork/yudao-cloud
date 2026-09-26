package cn.iocoder.yudao.module.gift.controller.admin.itinerary.vo;

import cn.iocoder.yudao.module.gift.dal.dataobject.itinerarycategory.ItineraryCategoryDO;
import cn.iocoder.yudao.module.system.api.area.AreaApi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;
import org.dromara.core.trans.vo.VO;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 行程 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ItineraryRespVO implements VO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10691")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "城市ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7848")
    @ExcelProperty("城市ID")
    @Trans(type = TransType.AUTO_TRANS, key = AreaApi.PREFIX,
            fields = {"cityName", "provinceName"}, refs = {"cityName", "provinceName"})
    private Integer cityId;

    @Schema(description = "城市名称", example = "杭州市")
    private String cityName;

    @Schema(description = "省份名称", example = "浙江省")
    private String provinceName;

    @Schema(description = "类别ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1668")
    @ExcelProperty("类别ID")
    @Trans(type = TransType.SIMPLE, target = ItineraryCategoryDO.class,
            fields = "title", ref = "categoryName")
    private Long categoryId;

    @Schema(description = "行程类别名称", example = "亲子游")
    private String categoryName;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("标题")
    private String title;

    @Schema(description = "副标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("副标题")
    private String subTitle;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "随便")
    @ExcelProperty("描述")
    private String description;

    @Schema(description = "图标")
    @ExcelProperty("图标")
    private String icon;

    @Schema(description = "多图片", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("多图片")
    private String picUrls;

    @Schema(description = "图片尺寸")
    @ExcelProperty("图片尺寸")
    private String picSizes;

    @Schema(description = "标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("标签")
    private String tags;

    @Schema(description = "封面图", example = "https://www.iocoder.cn")
    @ExcelProperty("封面图")
    private String coverUrl;

    @Schema(description = "封面宽度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("封面宽度")
    private Integer coverWidth;

    @Schema(description = "封面高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("封面高度")
    private Integer coverHeight;

    @Schema(description = "第二城市ID", example = "23770")
    @ExcelProperty("第二城市ID")
    private Integer nextCityId;

    @Schema(description = "浏览数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("浏览数")
    private Long viewCnt;

    @Schema(description = "点赞数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("点赞数")
    private Long likeCnt;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排序")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
