package cn.iocoder.yudao.module.gift.dal.dataobject.video;

import cn.iocoder.yudao.module.gift.enums.VideoQualityEnum;
import cn.iocoder.yudao.module.gift.enums.VideoStatusEnum;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 视频 DO
 *
 * @author 羔享科技
 */
@TableName("gift_video")
@KeySequence("gift_video_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 阿里云 VOD VideoId
     */
    private String vodVideoId;
    /**
     * 视频标题
     */
    private String title;
    /**
     * 状态
     *
     * 枚举 {@link VideoStatusEnum 对应的类}
     */
    private Integer status;
    /**
     * 视频封面图
     */
    private String coverUrl;
    /**
     * 基础播放地址
     */
    private String playUrl;
    /**
     * 视频时长毫秒
     */
    private Integer duration;
    /**
     * 视频宽度
     */
    private Integer width;
    /**
     * 视频高度
     */
    private Integer height;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 清晰度
     *
     * 枚举 {@link VideoQualityEnum 对应的类}
     */
    private Integer quality;


}
