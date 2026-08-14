package cn.iocoder.yudao.module.gift.dal.dataobject.article;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;

/**
 * 文章点赞记录
 *
 * @author 羔享科技
 */
@TableName("gift_article_like")
@Data
public class ArticleLikeDO extends TenantBaseDO {

    /** 编号 */
    @TableId
    private Long id;
    /** 文章编号 */
    private Long articleId;
    /** 会员编号 */
    private Long memberId;
}
