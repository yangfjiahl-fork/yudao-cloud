package cn.iocoder.yudao.module.gift.service.article;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticleLikeDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesDO;
import cn.iocoder.yudao.module.gift.dal.mysql.article.ArticleLikeMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.article.ArticlesMapper;
import cn.iocoder.yudao.module.gift.dal.redis.article.ArticleLikeRedisDAO;
import cn.iocoder.yudao.module.gift.enums.ArticleStatusEnum;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ArticlesServiceImpl} 的单元测试
 *
 * @author 羔享科技
 */
public class ArticlesServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ArticlesServiceImpl articleService;
    @Mock
    private ArticlesMapper articlesMapper;
    @Mock
    private ArticleLikeMapper articleLikeMapper;
    @Mock
    private ArticleLikeRedisDAO articleLikeRedisDAO;

    @Test
    public void testLikeArticle() {
        when(articlesMapper.selectById(1L)).thenReturn(ArticlesDO.builder()
                .id(1L).status(ArticleStatusEnum.ONLINE.getType()).build());
        when(articleLikeMapper.selectByArticleIdAndMemberId(1L, 2L)).thenReturn(null);

        articleService.likeArticle(2L, 1L);

        ArgumentCaptor<ArticleLikeDO> captor = ArgumentCaptor.forClass(ArticleLikeDO.class);
        verify(articleLikeMapper).insert(captor.capture());
        assertEquals(1L, captor.getValue().getArticleId());
        assertEquals(2L, captor.getValue().getMemberId());
        verify(articlesMapper).incrementLikeCount(1L);
        verify(articleLikeRedisDAO).updateLiked(2L, 1L, true);
    }

    @Test
    public void testLikeArticle_alreadyLiked() {
        when(articlesMapper.selectById(1L)).thenReturn(ArticlesDO.builder()
                .id(1L).status(ArticleStatusEnum.ONLINE.getType()).build());
        when(articleLikeMapper.selectByArticleIdAndMemberId(1L, 2L)).thenReturn(buildArticleLike(null, false));

        articleService.likeArticle(2L, 1L);

        verify(articleLikeMapper, never()).insert(any(ArticleLikeDO.class));
        verify(articlesMapper, never()).incrementLikeCount(anyLong());
        verify(articleLikeRedisDAO).updateLiked(2L, 1L, true);
    }

    @Test
    public void testLikeArticle_restoreDeletedLike() {
        when(articlesMapper.selectById(1L)).thenReturn(ArticlesDO.builder()
                .id(1L).status(ArticleStatusEnum.ONLINE.getType()).build());
        when(articleLikeMapper.selectByArticleIdAndMemberId(1L, 2L))
                .thenReturn(buildArticleLike(3L, true));

        articleService.likeArticle(2L, 1L);

        verify(articleLikeMapper).restoreById(3L, "2");
        verify(articleLikeMapper, never()).insert(any(ArticleLikeDO.class));
        verify(articlesMapper).incrementLikeCount(1L);
        verify(articleLikeRedisDAO).updateLiked(2L, 1L, true);
    }

    @Test
    public void testUnlikeArticle() {
        when(articleLikeMapper.deleteByArticleIdAndMemberId(1L, 2L)).thenReturn(1);

        articleService.unlikeArticle(2L, 1L);

        verify(articlesMapper).decrementLikeCount(1L);
        verify(articleLikeRedisDAO).updateLiked(2L, 1L, false);
    }

    @Test
    public void testUnlikeArticle_alreadyUnliked() {
        when(articleLikeMapper.deleteByArticleIdAndMemberId(1L, 2L)).thenReturn(0);

        articleService.unlikeArticle(2L, 1L);

        verify(articlesMapper, never()).decrementLikeCount(anyLong());
        verify(articleLikeRedisDAO).updateLiked(2L, 1L, false);
    }

    @Test
    public void testGetLikedArticleIds() {
        when(articleLikeRedisDAO.getLikedMap(2L, List.of(1L, 2L, 3L)))
                .thenReturn(Map.of(1L, true, 2L, false));
        when(articleLikeMapper.selectListByMemberIdAndArticleIds(2L, List.of(3L)))
                .thenReturn(List.of(new ArticleLikeDO().setArticleId(3L)));

        Set<Long> result = articleService.getLikedArticleIds(2L, List.of(1L, 2L, 3L));

        assertEquals(Set.of(1L, 3L), result);
        verify(articleLikeRedisDAO).updateLiked(eq(2L), eq(List.of(3L)), eq(Set.of(3L)));
    }

    private ArticleLikeDO buildArticleLike(Long id, boolean deleted) {
        ArticleLikeDO articleLike = new ArticleLikeDO();
        articleLike.setId(id);
        articleLike.setDeleted(deleted);
        return articleLike;
    }

}
