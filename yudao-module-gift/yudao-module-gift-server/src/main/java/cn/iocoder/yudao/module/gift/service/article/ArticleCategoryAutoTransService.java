package cn.iocoder.yudao.module.gift.service.article;

import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesCategoryDO;
import cn.iocoder.yudao.module.gift.dal.mysql.article.ArticlesCategoryMapper;
import cn.iocoder.yudao.module.gift.service.article.dto.ArticleCategoryTransDTO;
import jakarta.annotation.Resource;
import org.dromara.core.trans.anno.AutoTrans;
import org.dromara.trans.service.AutoTransable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 文章分类自动翻译服务
 *
 * @author 羔享科技
 */
@Service
@AutoTrans(namespace = ArticleCategoryAutoTransService.NAMESPACE, fields = "name")
public class ArticleCategoryAutoTransService implements AutoTransable<ArticleCategoryTransDTO> {

    public static final String NAMESPACE = "gift/article/category";

    @Resource
    private ArticlesCategoryMapper articlesCategoryMapper;

    @Override
    public List<ArticleCategoryTransDTO> selectByIds(List<?> ids) {
        Set<Long> categoryIds = convertSet(ids, id -> Long.valueOf(String.valueOf(id)));
        if (categoryIds.isEmpty()) {
            return List.of();
        }
        return articlesCategoryMapper.selectByIds(categoryIds).stream()
                .map(this::convert)
                .toList();
    }

    @Override
    public ArticleCategoryTransDTO selectById(Object id) {
        ArticlesCategoryDO category = articlesCategoryMapper.selectById(Long.valueOf(String.valueOf(id)));
        return category == null ? null : convert(category);
    }

    private ArticleCategoryTransDTO convert(ArticlesCategoryDO category) {
        return new ArticleCategoryTransDTO().setCategoryId(category.getId()).setName(category.getName());
    }

}
