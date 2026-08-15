package cn.iocoder.yudao.module.gift.service.article.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dromara.core.trans.vo.VO;

/**
 * 文章分类自动翻译 DTO
 *
 * @author 羔享科技
 */
@Data
@Accessors(chain = true)
public class ArticleCategoryTransDTO implements VO {

    private Long categoryId;
    private String name;

    @Override
    public Object getPkey() {
        return categoryId;
    }

}
