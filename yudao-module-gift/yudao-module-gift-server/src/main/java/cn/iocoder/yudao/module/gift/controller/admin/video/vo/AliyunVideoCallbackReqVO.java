package cn.iocoder.yudao.module.gift.controller.admin.video.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
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
    public static final String EVENT_TYPE_DELETE_MEDIA_COMPLETE = "DeleteMediaComplete";

    /**
     * 事件处理状态
     */
    @JsonAlias("Status")
    private String status;
    /**
     * 阿里云 VOD 媒体 ID。视频处理事件使用 VideoId，删除事件使用 MediaId。
     */
    @JsonAlias({"VideoId", "MediaId"})
    private String videoId;
    /**
     * 事件类型
     */
    @JsonAlias("EventType")
    private String eventType;
    /**
     * 转码流信息
     */
    @JsonAlias("StreamInfos")
    private List<StreamInfo> streamInfos;
    /**
     * 视频封面地址
     */
    @JsonAlias("CoverUrl")
    private String coverUrl;
    /**
     * 媒体类型，例如 video。
     */
    @JsonAlias("MediaType")
    private String mediaType;

    public String getCleanedCoverUrl() {
        if (StringUtils.isBlank(coverUrl)) {
            return "";
        }
        int index = coverUrl.indexOf('?');
        return index <= 0 ? coverUrl : coverUrl.substring(0, index);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StreamInfo {

        /**
         * 转码流状态
         */
        @JsonAlias("Status")
        private String status;
        /**
         * 清晰度，例如 LD、SD、HD
         */
        @JsonAlias("Definition")
        private String definition;
        /**
         * 文件大小，单位：字节
         */
        @JsonAlias("Size")
        private Long size;
        /**
         * 视频时长，单位：秒
         */
        @JsonAlias("Duration")
        private Double duration;
        /**
         * 播放地址
         */
        @JsonAlias("FileUrl")
        private String fileUrl;
        /**
         * 视频宽度
         */
        @JsonAlias("Width")
        private Integer width;
        /**
         * 视频高度
         */
        @JsonAlias("Height")
        private Integer height;

        public String getCleanedFileUrl() {
            if (StringUtils.isBlank(fileUrl)) {
                return "";
            }
            int index = fileUrl.indexOf('?');
            return index <= 0 ? fileUrl : fileUrl.substring(0, index);
        }
    }

}
