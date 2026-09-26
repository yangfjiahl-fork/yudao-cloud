package cn.iocoder.yudao.module.gift.controller.admin.useritineraryliked.vo;

import cn.iocoder.yudao.module.gift.dal.dataobject.useritinerary.UserItineraryDO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.dromara.core.trans.anno.Trans;
import org.dromara.core.trans.constant.TransType;
import org.dromara.core.trans.vo.VO;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 收藏行程 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserItineraryLikedRespVO implements VO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "23699")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "行程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12377")
    @ExcelProperty("行程ID")
    @Trans(type = TransType.SIMPLE, target = UserItineraryDO.class,
            fields = "title", ref = "itineraryName")
    private Long itineraryId;

    @Schema(description = "行程名称", example = "云南 6 天 5 晚亲子游")
    private String itineraryName;

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13449")
    @ExcelProperty("会员ID")
    @Trans(type = TransType.AUTO_TRANS, key = MemberUserApi.PREFIX,
            fields = "nickname", ref = "memberName")
    private Long memberId;

    @Schema(description = "会员名称", example = "小王同学")
    private String memberName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
