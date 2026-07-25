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
    public CommonResult<List<AppVideoRespVO>> getRandomVideoList() {
        List<VideoDO> videos = videoService.getRandomVideoList();
        videoService.authUrl(videos);
        return success(BeanUtils.toBean(videos, AppVideoRespVO.class));
    }

}
