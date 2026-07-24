package cn.iocoder.yudao.module.gift.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 视频 Response VO")
@Data
@ExcelIgnoreUnannotated
public class VideoRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "16576")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "阿里云 VOD VideoId", requiredMode = Schema.RequiredMode.REQUIRED, example = "6925")
    @ExcelProperty("阿里云 VOD VideoId")
    private String vodVideoId;

    @Schema(description = "视频标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("视频标题")
    private String title;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("gift_video_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "视频封面图", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @ExcelProperty("视频封面图")
    private String coverUrl;

    @Schema(description = "基础播放地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @ExcelProperty("基础播放地址")
    private String playUrl;

    @Schema(description = "视频时长毫秒", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("视频时长毫秒")
    private Integer duration;

    @Schema(description = "视频宽度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("视频宽度")
    private Integer width;

    @Schema(description = "视频高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("视频高度")
    private Integer height;

    @Schema(description = "文件大小", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("文件大小")
    private Long fileSize;

    @Schema(description = "清晰度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "清晰度", converter = DictConvert.class)
    @DictFormat("gift_video_quality") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer quality;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新者")
    @ExcelProperty("更新者")
    private String updater;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

}