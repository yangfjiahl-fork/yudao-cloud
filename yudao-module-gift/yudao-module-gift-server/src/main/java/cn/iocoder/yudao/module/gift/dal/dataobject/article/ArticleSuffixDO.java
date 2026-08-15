package cn.iocoder.yudao.module.gift.dal.dataobject.article;

import lombok.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 文章后缀 DO
 *
 * @author 羔享科技
 */
@TableName("gift_article_suffix")
@KeySequence("gift_article_suffix_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSuffixDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 签名标题
     */
    private String title;
    /**
     * 签名内容
     */
    private String content;


}
