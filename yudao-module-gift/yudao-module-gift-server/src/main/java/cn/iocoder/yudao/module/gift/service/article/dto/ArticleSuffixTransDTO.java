package cn.iocoder.yudao.module.gift.service.article.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dromara.core.trans.vo.VO;

/**
 * 文章后缀自动翻译 DTO
 *
 * @author 羔享科技
 */
@Data
@Accessors(chain = true)
public class ArticleSuffixTransDTO implements VO {

    private Long suffixId;
    private String title;

    @Override
    public Object getPkey() {
        return suffixId;
    }

}
