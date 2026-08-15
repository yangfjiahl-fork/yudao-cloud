package cn.iocoder.yudao.module.gift.service.article;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticleSuffixDO;
import cn.iocoder.yudao.module.gift.dal.mysql.article.ArticleSuffixMapper;
import cn.iocoder.yudao.module.gift.service.article.dto.ArticleSuffixTransDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

/**
 * {@link ArticleSuffixAutoTransService} 的单元测试
 *
 * @author 羔享科技
 */
class ArticleSuffixAutoTransServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ArticleSuffixAutoTransService autoTransService;
    @Mock
    private ArticleSuffixMapper articleSuffixMapper;

    @Test
    void testSelectByIds() {
        when(articleSuffixMapper.selectByIds(anyCollection())).thenReturn(List.of(
                ArticleSuffixDO.builder().id(1L).title("今日穿搭").build()));

        List<ArticleSuffixTransDTO> result = autoTransService.selectByIds(List.of(1L));

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getSuffixId());
        assertEquals("今日穿搭", result.get(0).getTitle());
    }

}
