package cn.iocoder.yudao.module.gift.controller.admin.slideritem.vo;

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

    @Schema(description = "跳转页面", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("跳转页面")
    private String jumpPage;

    @Schema(description = "跳转页面ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "32220")
    @ExcelProperty("跳转页面ID")
    private Long jumpPageId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}