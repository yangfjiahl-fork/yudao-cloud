package cn.iocoder.yudao.module.gift.controller.admin.wool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 羊毛 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WoolRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13660")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "业务场景", requiredMode = Schema.RequiredMode.REQUIRED, example = "REGISTER")
    @ExcelProperty(value = "业务场景", converter = DictConvert.class)
    @DictFormat("member_point_biz_type") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer bizType;

    @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27723")
    @ExcelProperty("业务编号")
    private String bizId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("数量")
    private Integer amount;

    @Schema(description = "备注", example = "新用户注册赠送羊毛")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "INIT")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("gift_wool_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "会员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "289")
    @ExcelProperty("会员编号")
    private Long memberId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
