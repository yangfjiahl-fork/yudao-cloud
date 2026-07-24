package cn.iocoder.yudao.module.gift.service.video;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.video.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.video.VideoDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.video.VideoMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 视频 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class VideoServiceImpl implements VideoService {

    @Resource
    private VideoMapper videoMapper;

    @Override
    public Long createVideo(VideoSaveReqVO createReqVO) {
        // 插入
        VideoDO video = BeanUtils.toBean(createReqVO, VideoDO.class);
        videoMapper.insert(video);

        // 返回
        return video.getId();
    }

    @Override
    public void updateVideo(VideoSaveReqVO updateReqVO) {
        // 校验存在
        validateVideoExists(updateReqVO.getId());
        // 更新
        VideoDO updateObj = BeanUtils.toBean(updateReqVO, VideoDO.class);
        videoMapper.updateById(updateObj);
    }

    @Override
    public void deleteVideo(Long id) {
        // 校验存在
        validateVideoExists(id);
        // 删除
        videoMapper.deleteById(id);
    }

    @Override
        public void deleteVideoListByIds(List<Long> ids) {
        // 删除
        videoMapper.deleteByIds(ids);
        }


    private void validateVideoExists(Long id) {
        if (videoMapper.selectById(id) == null) {
            throw exception(VIDEO_NOT_EXISTS);
        }
    }

    @Override
    public VideoDO getVideo(Long id) {
        return videoMapper.selectById(id);
    }

    @Override
    public PageResult<VideoDO> getVideoPage(VideoPageReqVO pageReqVO) {
        return videoMapper.selectPage(pageReqVO);
    }

}