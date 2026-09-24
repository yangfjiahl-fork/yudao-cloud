package cn.iocoder.yudao.module.system.api.area.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.dromara.core.trans.vo.VO;

@Schema(description = "RPC 服务 - 地区 Response DTO")
@Data
@Accessors(chain = true)
public class AreaRespDTO implements VO {

    @Schema(description = "地区编号", example = "330100")
    private Long id;

    @Schema(description = "地区名称", example = "杭州市")
    private String name;

    @Schema(description = "省份名称", example = "浙江省")
    private String provinceName;

    @Schema(description = "城市名称", example = "杭州市")
    private String cityName;

}
