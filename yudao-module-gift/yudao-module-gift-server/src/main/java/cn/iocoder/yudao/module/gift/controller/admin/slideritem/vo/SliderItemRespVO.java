package cn.iocoder.yudao.module.gift.controller.admin.slideritem.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.gift.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 轮播图 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SliderItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "26589")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "轮播ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17759")
    @ExcelProperty("轮播ID")
    private Long sliderId;

    @Schema(description = "轮播位置", example = "HOME_TOP")
    @ExcelProperty(value = "轮播位置", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SLIDER_POSITION)
    private String positionCode;

    @Schema(description = "城市ID", example = "330100")
    @ExcelProperty("城市ID")
    private Long cityId;

    @Schema(description = "省份名称", example = "浙江省")
    @ExcelProperty("省份名称")
    private String provinceName;

    @Schema(description = "城市名称", example = "杭州市")
    @ExcelProperty("城市名称")
    private String cityName;

    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @ExcelProperty("图片地址")
    private String imageUrl;

    @Schema(description = "图片宽度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("图片宽度")
    private Integer imageWidth;

    @Schema(description = "图片高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("图片高度")
    private Integer imageHeight;

    @Schema(description = "顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("顺序")
    private Integer sort;

    @Schema(description = "跳转页面", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = "SHARE", example = "SHARE")
    @ExcelProperty(value = "跳转页面", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SLIDER_ITEM_JUMP_PAGE)
    private String jumpPage;

    @Schema(description = "跳转页面ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "32220")
    @ExcelProperty("跳转页面ID")
    private Long jumpPageId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
