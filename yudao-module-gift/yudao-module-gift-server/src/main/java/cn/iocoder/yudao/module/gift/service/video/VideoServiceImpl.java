package cn.iocoder.yudao.module.gift.service.video;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.iocoder.yudao.module.gift.convert.AliyunAuthUtil;
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
import cn.iocoder.yudao.module.gift.enums.VideoQualityEnum;
import cn.iocoder.yudao.module.gift.enums.VideoStatusEnum;

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

    private static final int RANDOM_VIDEO_CANDIDATE_SIZE = 50;

    @Value("${vod.privateKey}")
    private String privateKey;

    @Resource
    private VideoMapper videoMapper;

    @Override
    public void authUrl(List<VideoDO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        list.forEach(item -> {
            item.setCoverUrl(AliyunAuthUtil.generateAuthUrl(item.getCoverUrl(), privateKey, 60 * 60));
            item.setPlayUrl(AliyunAuthUtil.generateAuthUrl(item.getPlayUrl(), privateKey, 60 * 60));
        });
    }

    @Override
    public List<VideoDO> getRandomVideoList(Integer pageSize) {
        List<VideoDO> videos = new ArrayList<>(videoMapper.selectRecentOnlineList(RANDOM_VIDEO_CANDIDATE_SIZE));
        Collections.shuffle(videos);
        return new ArrayList<>(videos.subList(0, Math.min(videos.size(), pageSize)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveAliyunVideoCallback(AliyunVideoCallbackReqVO callbackReqVO) {
        if (callbackReqVO == null || StrUtil.isBlank(callbackReqVO.getVideoId())
                || !StrUtil.equalsIgnoreCase("success", callbackReqVO.getStatus())) {
            return false;
        }
        if (AliyunVideoCallbackReqVO.EVENT_TYPE_TRANSCODE_COMPLETE.equals(callbackReqVO.getEventType())) {
            return handleTranscodeComplete(callbackReqVO);
        }
        if (AliyunVideoCallbackReqVO.EVENT_TYPE_SNAPSHOT_COMPLETE.equals(callbackReqVO.getEventType())) {
            return handleSnapshotComplete(callbackReqVO);
        }
        if (AliyunVideoCallbackReqVO.EVENT_TYPE_DELETE_MEDIA_COMPLETE.equals(callbackReqVO.getEventType())
                && StrUtil.equalsIgnoreCase("video", callbackReqVO.getMediaType())) {
            return handleDeleteMediaComplete(callbackReqVO);
        }
        return false;
    }

    /**
     * 阿里云已删除媒体后，同步逻辑删除本地视频记录。
     */
    private boolean handleDeleteMediaComplete(AliyunVideoCallbackReqVO callbackReqVO) {
        VideoDO video = videoMapper.selectByVodVideoId(callbackReqVO.getVideoId());
        if (video == null) {
            return true;
        }
        videoMapper.deleteById(video.getId());
        return true;
    }

    private boolean handleTranscodeComplete(AliyunVideoCallbackReqVO callbackReqVO) {
        AliyunVideoCallbackReqVO.StreamInfo streamInfo = CollUtil.emptyIfNull(callbackReqVO.getStreamInfos()).stream()
                .filter(info -> StrUtil.equalsIgnoreCase("success", info.getStatus()))
                .findFirst().orElse(null);
        if (streamInfo == null) {
            return false;
        }

        VideoDO video = videoMapper.selectByVodVideoId(callbackReqVO.getVideoId());
        if (video == null) {
            videoMapper.insert(buildDefaultVideo(callbackReqVO.getVideoId(), streamInfo, null));
            return true;
        }
        VideoDO updateObj = new VideoDO();
        updateObj.setId(video.getId());
        fillTranscodeFields(updateObj, streamInfo);
        updateObj.setStatus(VideoStatusEnum.ONLINE.getType());
        videoMapper.updateById(updateObj);
        return true;
    }

    private boolean handleSnapshotComplete(AliyunVideoCallbackReqVO callbackReqVO) {
        if (StrUtil.isBlank(callbackReqVO.getCoverUrl())) {
            return false;
        }

        VideoDO video = videoMapper.selectByVodVideoId(callbackReqVO.getVideoId());
        if (video == null) {
            videoMapper.insert(buildDefaultVideo(callbackReqVO.getVideoId(), null, callbackReqVO.getCleanedCoverUrl()));
            return true;
        }
        VideoDO updateObj = new VideoDO();
        updateObj.setId(video.getId());
        updateObj.setCoverUrl(callbackReqVO.getCleanedCoverUrl());
        updateObj.setStatus(VideoStatusEnum.ONLINE.getType());
        videoMapper.updateById(updateObj);
        return true;
    }

    /**
     * 截图和转码回调会异步到达，因此首次入库时补全全部必填字段。
     */
    private VideoDO buildDefaultVideo(String vodVideoId, AliyunVideoCallbackReqVO.StreamInfo streamInfo, String coverUrl) {
        VideoDO video = VideoDO.builder()
                .vodVideoId(vodVideoId)
                .title(vodVideoId)
                .status(VideoStatusEnum.OFFLINE.getType())
                .coverUrl(StrUtil.nullToDefault(coverUrl, ""))
                .playUrl("")
                .duration(0)
                .width(0)
                .height(0)
                .fileSize(0L)
                .quality(VideoQualityEnum.SD.getType())
                .build();
        if (streamInfo != null) {
            fillTranscodeFields(video, streamInfo);
        }
        video.setCreator("admin");
        return video;
    }

    private void fillTranscodeFields(VideoDO video, AliyunVideoCallbackReqVO.StreamInfo streamInfo) {
        video.setPlayUrl(StrUtil.nullToDefault(streamInfo.getCleanedFileUrl(), ""));
        video.setDuration(streamInfo.getDuration() == null ? 0 : (int) Math.round(streamInfo.getDuration() * 1000));
        video.setWidth(streamInfo.getWidth() == null ? 0 : streamInfo.getWidth());
        video.setHeight(streamInfo.getHeight() == null ? 0 : streamInfo.getHeight());
        video.setFileSize(streamInfo.getSize() == null ? 0L : streamInfo.getSize());
        video.setQuality(getVideoQuality(streamInfo.getDefinition()));
    }

    private Integer getVideoQuality(String definition) {
        if (StrUtil.equalsIgnoreCase("LD", definition)) {
            return VideoQualityEnum.LD.getType();
        }
        if (StrUtil.equalsIgnoreCase("HD", definition)) {
            return VideoQualityEnum.HD.getType();
        }
        return VideoQualityEnum.SD.getType();
    }

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
