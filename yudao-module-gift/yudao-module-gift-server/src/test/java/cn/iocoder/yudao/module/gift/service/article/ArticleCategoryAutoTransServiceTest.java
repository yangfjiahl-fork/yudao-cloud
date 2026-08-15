package cn.iocoder.yudao.module.gift.service.article;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesCategoryDO;
import cn.iocoder.yudao.module.gift.dal.mysql.article.ArticlesCategoryMapper;
import cn.iocoder.yudao.module.gift.service.article.dto.ArticleCategoryTransDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

/**
 * {@link ArticleCategoryAutoTransService} 的单元测试
 *
 * @author 羔享科技
 */
class ArticleCategoryAutoTransServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ArticleCategoryAutoTransService autoTransService;
    @Mock
    private ArticlesCategoryMapper articlesCategoryMapper;

    @Test
    void testSelectByIds() {
        when(articlesCategoryMapper.selectByIds(anyCollection())).thenReturn(List.of(
                ArticlesCategoryDO.builder().id(1L).name("饰品搭配").build(),
                ArticlesCategoryDO.builder().id(2L).name("护肤知识").build()));

        List<ArticleCategoryTransDTO> result = autoTransService.selectByIds(List.of(1L, 2L));

        assertEquals(2, result.size());
        assertEquals("饰品搭配", result.get(0).getName());
        assertEquals(2L, result.get(1).getCategoryId());
    }

}
