package cn.iocoder.yudao.module.gift.controller.admin.slider.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 轮播 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SliderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4841")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "轮播位置", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("轮播位置")
    private String positionCode;

    @Schema(description = "城市ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "16158")
    @ExcelProperty("城市ID")
    private Long cityId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}