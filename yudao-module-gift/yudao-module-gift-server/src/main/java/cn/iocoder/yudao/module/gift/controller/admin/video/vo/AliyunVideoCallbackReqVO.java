package cn.iocoder.yudao.module.gift.controller.admin.video.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 阿里云 VOD 视频处理回调。
 *
 * @author 羔享科技
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AliyunVideoCallbackReqVO {

    public static final String EVENT_TYPE_TRANSCODE_COMPLETE = "TranscodeComplete";
    public static final String EVENT_TYPE_SNAPSHOT_COMPLETE = "SnapshotComplete";

    /**
     * 事件处理状态
     */
    private String status;
    /**
     * 阿里云 VOD VideoId
     */
    private String videoId;
    /**
     * 事件类型
     */
    private String eventType;
    /**
     * 转码流信息
     */
    private List<StreamInfo> streamInfos;
    /**
     * 视频封面地址
     */
    private String coverUrl;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StreamInfo {

        /**
         * 转码流状态
         */
        private String status;
        /**
         * 清晰度，例如 LD、SD、HD
         */
        private String definition;
        /**
         * 文件大小，单位：字节
         */
        private Long size;
        /**
         * 视频时长，单位：秒
         */
        private Double duration;
        /**
         * 播放地址
         */
        private String fileUrl;
        /**
         * 视频宽度
         */
        private Integer width;
        /**
         * 视频高度
         */
        private Integer height;

    }

}
