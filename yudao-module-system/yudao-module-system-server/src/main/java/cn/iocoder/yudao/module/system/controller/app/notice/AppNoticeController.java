package cn.iocoder.yudao.module.system.controller.app.notice;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.controller.app.notice.vo.AppNoticeRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notice.NoticeDO;
import cn.iocoder.yudao.module.system.service.notice.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 通知公告")
@RestController
@RequestMapping("/system/notice")
@Validated
public class AppNoticeController {

    @Resource
    private NoticeService noticeService;

    @GetMapping("/page")
    @Operation(summary = "获得启用的通知公告分页")
    public CommonResult<PageResult<AppNoticeRespVO>> getNoticePage(@Valid PageParam pageParam) {
        PageResult<NoticeDO> pageResult = noticeService.getEnableNoticePage(pageParam);
        return success(BeanUtils.toBean(pageResult, AppNoticeRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得启用的通知公告详情")
    @Parameter(name = "id", description = "公告编号", required = true, example = "1024")
    public CommonResult<AppNoticeRespVO> getNotice(@RequestParam("id") Long id) {
        NoticeDO notice = noticeService.getEnableNotice(id);
        return success(BeanUtils.toBean(notice, AppNoticeRespVO.class));
    }

}
