package cn.iocoder.yudao.module.gift.dal.redis.article;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ArticleLikeRedisDAO} 的单元测试
 *
 * @author 羔享科技
 */
class ArticleLikeRedisDAOTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ArticleLikeRedisDAO articleLikeRedisDAO;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Test
    void testUpdateLiked() {
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);

        articleLikeRedisDAO.updateLiked(2L, 1L, true);

        verify(hashOperations).put("gift:article:like:2", "1", "1");
        verify(stringRedisTemplate).expire("gift:article:like:2", 7, TimeUnit.DAYS);
    }

    @Test
    void testUpdateLikedBatch() {
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);

        articleLikeRedisDAO.updateLiked(2L, java.util.List.of(1L, 3L), java.util.List.of(3L));

        verify(hashOperations).putAll("gift:article:like:2", Map.of("1", "0", "3", "1"));
        verify(stringRedisTemplate).expire("gift:article:like:2", 7, TimeUnit.DAYS);
    }

}
