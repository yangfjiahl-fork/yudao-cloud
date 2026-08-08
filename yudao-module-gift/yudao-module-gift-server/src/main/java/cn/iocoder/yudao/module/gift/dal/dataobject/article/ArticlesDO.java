package cn.iocoder.yudao.module.gift.dal.dataobject.article;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 文章 DO
 *
 * @author 羔享科技
 */
@TableName(value = "gift_article", autoResultMap = true)
@KeySequence("gift_article_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticlesDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 文章作者会员编号
     */
    private Long memberId;
    /**
     * 文章作者
     */
    private String author;
    /**
     * 文章分类编号，发布时要求为末级分类
     */
    private Long categoryId;
    /**
     * 标题
     */
    private String title;
    /**
     * 摘要
     */
    private String summary;
    /**
     * 封面图地址
     */
    private String coverImage;
    /**
     * 封面宽度
     */
    private Integer coverWidth;
    /**
     * 封面高度
     */
    private Integer coverHeight;
    /**
     * 封面方向
     */
    private String coverOrientation;
    /**
     * 轮播图地址数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> sliderPicUrls;
    /**
     * 站内富文本正文
     */
    private String content;
    /**
     * 浏览次数
     */
    private Integer viewCount;
    /**
     * 点赞次数
     */
    private Integer likeCount;
    /**
     * 排序值，越大越靠前
     */
    private Integer sort;
    /**
     * 状态：0-草稿，1-已发布，2-已下架
     *
     * 枚举 {@link TODO gift_article_status 对应的类}
     */
    private Integer status;
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;


}
