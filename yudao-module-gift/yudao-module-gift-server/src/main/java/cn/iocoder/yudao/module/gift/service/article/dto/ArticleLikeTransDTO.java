package cn.iocoder.yudao.module.gift.service.article.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dromara.core.trans.vo.VO;

/**
 * 文章点赞状态翻译 DTO
 *
 * @author 羔享科技
 */
@Data
@Accessors(chain = true)
public class ArticleLikeTransDTO implements VO {

    /**
     * 对应文章编号；作为 Easy Trans 的转换主键。
     */
    private Long articleId;

    private Boolean liked;

    @Override
    public Object getPkey() {
        return articleId;
    }

}
