package cn.iocoder.yudao.module.gift.controller.app.video;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.gift.controller.app.video.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.video.VideoDO;
import cn.iocoder.yudao.module.gift.service.video.VideoService;

@Tag(name = "用户 APP - 视频")
@RestController
@RequestMapping("/gift/video")
@Validated
public class AppVideoController {

    @Resource
    private VideoService videoService;

    @GetMapping("/random-list")
    @Operation(summary = "随机获得视频列表")
    @Parameter(name = "pageSize", description = "返回数量，默认 30，最大 50", example = "30")
    public CommonResult<List<AppVideoRespVO>> getRandomVideoList(
            @RequestParam(value = "pageSize", defaultValue = "30") @Min(1) @Max(50) Integer pageSize) {
        List<VideoDO> videos = videoService.getRandomVideoList(pageSize);
        videoService.authUrl(videos);
        return success(BeanUtils.toBean(videos, AppVideoRespVO.class));
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "根据id获得视频列表")
    @Parameter(name = "ids", description = "多个id", example = "30")
    public CommonResult<List<AppVideoRespVO>> getByIds(@RequestParam("ids") Set<Long> ids) {
        List<VideoDO> videos = videoService.getByIds(ids);
        videoService.authUrl(videos);
        return success(BeanUtils.toBean(videos, AppVideoRespVO.class));
    }
}
