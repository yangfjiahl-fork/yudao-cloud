package cn.iocoder.yudao.module.gift.service.video;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.gift.controller.admin.video.vo.AliyunVideoCallbackReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.video.VideoDO;
import cn.iocoder.yudao.module.gift.dal.mysql.video.VideoMapper;
import cn.iocoder.yudao.module.gift.enums.VideoQualityEnum;
import cn.iocoder.yudao.module.gift.enums.VideoStatusEnum;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link VideoServiceImpl} 的单元测试
 *
 * @author 羔享科技
 */
public class VideoServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private VideoServiceImpl videoService;
    @Mock
    private VideoMapper videoMapper;

    @Test
    public void testAliyunVideoCallbackReqVO_parsePascalCase() {
        String body = """
                {"Status":"success","VideoId":"10405a82876771f18d7b5017f0f80102","EventType":"TranscodeComplete",
                "StreamInfos":[{"Status":"success","Definition":"SD","Size":440310,"Duration":6.13,
                "FileUrl":"https://example.com/video.mp4","Height":960,"Width":444}]}""";

        AliyunVideoCallbackReqVO callback = JsonUtils.parseObject(body, AliyunVideoCallbackReqVO.class);

        assertEquals("success", callback.getStatus());
        assertEquals("10405a82876771f18d7b5017f0f80102", callback.getVideoId());
        assertEquals(AliyunVideoCallbackReqVO.EVENT_TYPE_TRANSCODE_COMPLETE, callback.getEventType());
        AliyunVideoCallbackReqVO.StreamInfo streamInfo = callback.getStreamInfos().get(0);
        assertEquals("https://example.com/video.mp4", streamInfo.getFileUrl());
        assertEquals(6.13D, streamInfo.getDuration());
        assertEquals(444, streamInfo.getWidth());
        assertEquals(960, streamInfo.getHeight());
    }

    @Test
    public void testGetRandomVideoList() {
        List<VideoDO> videos = LongStream.rangeClosed(1, 25)
                .mapToObj(id -> VideoDO.builder().id(id).build()).toList();
        when(videoMapper.selectRecentOnlineList(50)).thenReturn(videos);

        List<VideoDO> result = videoService.getRandomVideoList(30);

        assertEquals(25, result.size());
        assertEquals(25, result.stream().map(VideoDO::getId).distinct().count());
        verify(videoMapper).selectRecentOnlineList(50);
    }

    @Test
    public void testReceiveAliyunVideoCallback_snapshotFirst() {
        AliyunVideoCallbackReqVO snapshotCallback = buildSnapshotCallback();
        when(videoMapper.selectByVodVideoId(snapshotCallback.getVideoId())).thenReturn(null);

        assertTrue(videoService.receiveAliyunVideoCallback(snapshotCallback));

        VideoDO inserted = captureInsertedVideo();
        assertEquals(snapshotCallback.getVideoId(), inserted.getVodVideoId());
        assertEquals(snapshotCallback.getCoverUrl(), inserted.getCoverUrl());
        assertEquals("", inserted.getPlayUrl());
        assertEquals(0, inserted.getDuration());
        assertEquals(VideoStatusEnum.OFFLINE.getType(), inserted.getStatus());
    }

    @Test
    public void testReceiveAliyunVideoCallback_transcodeFirst() {
        AliyunVideoCallbackReqVO transcodeCallback = buildTranscodeCallback();
        when(videoMapper.selectByVodVideoId(transcodeCallback.getVideoId())).thenReturn(null);

        assertTrue(videoService.receiveAliyunVideoCallback(transcodeCallback));

        VideoDO inserted = captureInsertedVideo();
        assertEquals("https://example.com/video.mp4", inserted.getPlayUrl());
        assertEquals(6130, inserted.getDuration());
        assertEquals(444, inserted.getWidth());
        assertEquals(960, inserted.getHeight());
        assertEquals(440310L, inserted.getFileSize());
        assertEquals(VideoQualityEnum.SD.getType(), inserted.getQuality());
        assertEquals("", inserted.getCoverUrl());
    }

    @Test
    public void testReceiveAliyunVideoCallback_transcodeAfterSnapshot() {
        AliyunVideoCallbackReqVO transcodeCallback = buildTranscodeCallback();
        when(videoMapper.selectByVodVideoId(transcodeCallback.getVideoId()))
                .thenReturn(VideoDO.builder().id(1L).vodVideoId(transcodeCallback.getVideoId())
                        .coverUrl("https://example.com/cover.jpg").build());

        assertTrue(videoService.receiveAliyunVideoCallback(transcodeCallback));

        ArgumentCaptor<VideoDO> captor = ArgumentCaptor.forClass(VideoDO.class);
        verify(videoMapper).updateById(captor.capture());
        VideoDO updated = captor.getValue();
        assertEquals(1L, updated.getId());
        assertEquals("https://example.com/video.mp4", updated.getPlayUrl());
        assertEquals(6130, updated.getDuration());
        assertNull(updated.getCoverUrl());
    }

    @Test
    public void testReceiveAliyunVideoCallback_snapshotAfterTranscode() {
        AliyunVideoCallbackReqVO snapshotCallback = buildSnapshotCallback();
        when(videoMapper.selectByVodVideoId(snapshotCallback.getVideoId()))
                .thenReturn(VideoDO.builder().id(1L).vodVideoId(snapshotCallback.getVideoId())
                        .playUrl("https://example.com/video.mp4").build());

        assertTrue(videoService.receiveAliyunVideoCallback(snapshotCallback));

        ArgumentCaptor<VideoDO> captor = ArgumentCaptor.forClass(VideoDO.class);
        verify(videoMapper).updateById(captor.capture());
        VideoDO updated = captor.getValue();
        assertEquals(1L, updated.getId());
        assertEquals(snapshotCallback.getCoverUrl(), updated.getCoverUrl());
        assertNull(updated.getPlayUrl());
        assertNull(updated.getDuration());
    }

    private VideoDO captureInsertedVideo() {
        ArgumentCaptor<VideoDO> captor = ArgumentCaptor.forClass(VideoDO.class);
        verify(videoMapper).insert(captor.capture());
        return captor.getValue();
    }

    private AliyunVideoCallbackReqVO buildSnapshotCallback() {
        AliyunVideoCallbackReqVO callback = new AliyunVideoCallbackReqVO();
        callback.setStatus("success");
        callback.setVideoId("video-id");
        callback.setEventType(AliyunVideoCallbackReqVO.EVENT_TYPE_SNAPSHOT_COMPLETE);
        callback.setCoverUrl("https://example.com/cover.jpg");
        return callback;
    }

    private AliyunVideoCallbackReqVO buildTranscodeCallback() {
        AliyunVideoCallbackReqVO.StreamInfo streamInfo = new AliyunVideoCallbackReqVO.StreamInfo();
        streamInfo.setStatus("success");
        streamInfo.setDefinition("SD");
        streamInfo.setFileUrl("https://example.com/video.mp4");
        streamInfo.setDuration(6.13D);
        streamInfo.setWidth(444);
        streamInfo.setHeight(960);
        streamInfo.setSize(440310L);

        AliyunVideoCallbackReqVO callback = new AliyunVideoCallbackReqVO();
        callback.setStatus("success");
        callback.setVideoId("video-id");
        callback.setEventType(AliyunVideoCallbackReqVO.EVENT_TYPE_TRANSCODE_COMPLETE);
        callback.setStreamInfos(List.of(streamInfo));
        return callback;
    }

}
