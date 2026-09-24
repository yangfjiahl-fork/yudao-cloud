package cn.iocoder.yudao.module.gift.controller.admin.slider.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.gift.enums.DictTypeConstants;
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

@Schema(description = "管理后台 - 轮播 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SliderRespVO implements VO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4841")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "轮播位置", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = "HOME_TOP", example = "HOME_TOP")
    @ExcelProperty(value = "轮播位置", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SLIDER_POSITION)
    private String positionCode;

    @Schema(description = "城市ID（可选）", example = "16158")
    @ExcelProperty("城市ID")
    @Trans(type = TransType.AUTO_TRANS, key = AreaApi.PREFIX,
            fields = {"cityName", "provinceName"}, refs = {"cityName", "provinceName"})
    private Long cityId;

    @Schema(description = "城市名称", example = "杭州市")
    private String cityName;

    @Schema(description = "省份名称", example = "浙江省")
    private String provinceName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
