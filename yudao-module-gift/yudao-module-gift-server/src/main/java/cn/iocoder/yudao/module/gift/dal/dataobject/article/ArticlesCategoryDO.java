package cn.iocoder.yudao.module.gift.dal.dataobject.article;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 文章分类 DO
 *
 * @author 羔享科技
 */
@TableName("gift_article_category")
@KeySequence("gift_article_category_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticlesCategoryDO extends BaseDO {

    /**
     * 父分类编号 - 根分类
     */
    public static final Long PARENT_ID_ROOT = 0L;
    /**
     * 限定分类层级
     */
    public static final int CATEGORY_LEVEL = 2;

    /**
     * 分类编号
     */
    @TableId
    private Long id;
    /**
     * 父分类编号
     */
    private Long parentId;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 分类图
     */
    private String picUrl;
    /**
     * 分类排序
     */
    private Integer sort;
    /**
     * 开启状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
