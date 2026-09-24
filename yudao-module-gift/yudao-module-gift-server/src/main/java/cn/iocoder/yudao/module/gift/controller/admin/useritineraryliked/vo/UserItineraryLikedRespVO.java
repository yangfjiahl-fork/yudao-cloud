package cn.iocoder.yudao.module.gift.controller.admin.useritineraryliked.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 收藏行程 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserItineraryLikedRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "23699")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "行程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12377")
    @ExcelProperty("行程ID")
    private Long itineraryId;

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13449")
    @ExcelProperty("会员ID")
    private Long memberId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}