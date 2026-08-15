package cn.iocoder.yudao.module.gift.service.article;

import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticleSuffixDO;
import cn.iocoder.yudao.module.gift.dal.mysql.article.ArticleSuffixMapper;
import cn.iocoder.yudao.module.gift.service.article.dto.ArticleSuffixTransDTO;
import jakarta.annotation.Resource;
import org.dromara.core.trans.anno.AutoTrans;
import org.dromara.trans.service.AutoTransable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 文章后缀自动翻译服务
 *
 * @author 羔享科技
 */
@Service
@AutoTrans(namespace = ArticleSuffixAutoTransService.NAMESPACE, fields = "title")
public class ArticleSuffixAutoTransService implements AutoTransable<ArticleSuffixTransDTO> {

    public static final String NAMESPACE = "gift/article/suffix";

    @Resource
    private ArticleSuffixMapper articleSuffixMapper;

    @Override
    public List<ArticleSuffixTransDTO> selectByIds(List<?> ids) {
        Set<Long> suffixIds = convertSet(ids, id -> Long.valueOf(String.valueOf(id)));
        if (suffixIds.isEmpty()) {
            return List.of();
        }
        return articleSuffixMapper.selectByIds(suffixIds).stream()
                .map(this::convert)
                .toList();
    }

    @Override
    public ArticleSuffixTransDTO selectById(Object id) {
        ArticleSuffixDO suffix = articleSuffixMapper.selectById(Long.valueOf(String.valueOf(id)));
        return suffix == null ? null : convert(suffix);
    }

    private ArticleSuffixTransDTO convert(ArticleSuffixDO suffix) {
        return new ArticleSuffixTransDTO().setSuffixId(suffix.getId()).setTitle(suffix.getTitle());
    }

}
