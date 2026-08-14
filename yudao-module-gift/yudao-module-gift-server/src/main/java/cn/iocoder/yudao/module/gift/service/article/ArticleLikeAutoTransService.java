package cn.iocoder.yudao.module.gift.service.article;

import cn.iocoder.yudao.module.gift.service.article.dto.ArticleLikeTransDTO;
import org.dromara.core.trans.anno.AutoTrans;
import org.dromara.trans.service.AutoTransable;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 文章点赞状态自动翻译服务
 *
 * @author 羔享科技
 */
@Service
@AutoTrans(namespace = ArticleLikeAutoTransService.NAMESPACE, fields = "liked")
public class ArticleLikeAutoTransService implements AutoTransable<ArticleLikeTransDTO> {

    public static final String NAMESPACE = "gift/article/like";

    @Resource
    private ArticleService articleService;

    @Override
    public List<ArticleLikeTransDTO> selectByIds(List<?> ids) {
        Long memberId = getLoginUserId();
        if (memberId == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> articleIds = convertSet(ids, id -> Long.valueOf(String.valueOf(id)));
        Set<Long> likedArticleIds = articleService.getLikedArticleIds(memberId, articleIds);
        return articleIds.stream().map(articleId -> new ArticleLikeTransDTO()
                .setArticleId(articleId)
                .setLiked(likedArticleIds.contains(articleId)))
                .toList();
    }

    @Override
    public ArticleLikeTransDTO selectById(Object id) {
        List<ArticleLikeTransDTO> articleLikes = selectByIds(List.of(id));
        return articleLikes.isEmpty() ? null : articleLikes.get(0);
    }

}
