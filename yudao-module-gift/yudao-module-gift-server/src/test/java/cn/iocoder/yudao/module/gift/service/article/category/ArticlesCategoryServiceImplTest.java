package cn.iocoder.yudao.module.gift.service.article.category;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleCategorySaveReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesCategoryDO;
import cn.iocoder.yudao.module.gift.dal.mysql.article.ArticlesCategoryMapper;
import cn.iocoder.yudao.module.gift.service.article.ArticlesCategoryServiceImpl;
import cn.iocoder.yudao.module.gift.service.article.ArticleService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.ARTICLE_CATEGORY_EXISTS_CHILDREN;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.ARTICLE_CATEGORY_HAVE_BIND_ARTICLES;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.ARTICLE_CATEGORY_PARENT_NOT_FIRST_LEVEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ArticlesCategoryServiceImpl} 的单元测试
 *
 * @author 羔享科技
 */
public class ArticlesCategoryServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ArticlesCategoryServiceImpl articleCategoryService;
    @Mock
    private ArticlesCategoryMapper articlesCategoryMapper;
    @Mock
    private ArticleService articleService;

    @Test
    public void testCreateArticleCategory() {
        ArticleCategorySaveReqVO reqVO = buildSaveReqVO();

        articleCategoryService.createArticleCategory(reqVO);

        ArgumentCaptor<ArticlesCategoryDO> captor = ArgumentCaptor.forClass(ArticlesCategoryDO.class);
        verify(articlesCategoryMapper).insert(captor.capture());
        assertEquals(reqVO.getParentId(), captor.getValue().getParentId());
        assertEquals(reqVO.getName(), captor.getValue().getName());
    }

    @Test
    public void testCreateArticleCategory_parentIsSecondLevel() {
        ArticleCategorySaveReqVO reqVO = buildSaveReqVO();
        reqVO.setParentId(2L);
        when(articlesCategoryMapper.selectById(2L)).thenReturn(ArticlesCategoryDO.builder().id(2L).parentId(1L).build());

        assertServiceException(() -> articleCategoryService.createArticleCategory(reqVO),
                ARTICLE_CATEGORY_PARENT_NOT_FIRST_LEVEL);
        verify(articlesCategoryMapper, never()).insert(org.mockito.ArgumentMatchers.any(ArticlesCategoryDO.class));
    }

    @Test
    public void testDeleteArticleCategory_hasChildren() {
        when(articlesCategoryMapper.selectById(1L)).thenReturn(ArticlesCategoryDO.builder().id(1L).build());
        when(articlesCategoryMapper.selectCountByParentId(1L)).thenReturn(1L);

        assertServiceException(() -> articleCategoryService.deleteArticleCategory(1L), ARTICLE_CATEGORY_EXISTS_CHILDREN);
    }

    @Test
    public void testDeleteArticleCategory_hasArticles() {
        when(articlesCategoryMapper.selectById(1L)).thenReturn(ArticlesCategoryDO.builder().id(1L).build());
        when(articlesCategoryMapper.selectCountByParentId(1L)).thenReturn(0L);
        when(articleService.getArticleCountByCategoryId(1L)).thenReturn(1L);

        assertServiceException(() -> articleCategoryService.deleteArticleCategory(1L), ARTICLE_CATEGORY_HAVE_BIND_ARTICLES);
    }

    private ArticleCategorySaveReqVO buildSaveReqVO() {
        ArticleCategorySaveReqVO reqVO = new ArticleCategorySaveReqVO();
        reqVO.setParentId(0L);
        reqVO.setName("饰品搭配");
        reqVO.setPicUrl("https://example.com/category.png");
        reqVO.setSort(1);
        reqVO.setStatus(0);
        return reqVO;
    }

}
