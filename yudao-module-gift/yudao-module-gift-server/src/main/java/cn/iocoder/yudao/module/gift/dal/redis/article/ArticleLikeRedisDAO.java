package cn.iocoder.yudao.module.gift.dal.redis.article;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.gift.dal.redis.RedisKeyConstants.ARTICLE_LIKE;

/**
 * 文章点赞状态 Redis DAO
 *
 * @author 羔享科技
 */
@Repository
public class ArticleLikeRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void updateLiked(Long memberId, Long articleId, boolean liked) {
        stringRedisTemplate.opsForHash().put(getKey(memberId), String.valueOf(articleId), liked ? "1" : "0");
    }

    public void updateLiked(Long memberId, Collection<Long> articleIds, Collection<Long> likedArticleIds) {
        Map<String, String> values = new HashMap<>();
        for (Long articleId : articleIds) {
            values.put(String.valueOf(articleId), likedArticleIds.contains(articleId) ? "1" : "0");
        }
        if (!values.isEmpty()) {
            stringRedisTemplate.opsForHash().putAll(getKey(memberId), values);
        }
    }

    public Map<Long, Boolean> getLikedMap(Long memberId, Collection<Long> articleIds) {
        List<Object> fields = articleIds.stream().map(articleId -> (Object) String.valueOf(articleId)).toList();
        List<Object> values = stringRedisTemplate.opsForHash().multiGet(getKey(memberId), fields);
        Map<Long, Boolean> result = new HashMap<>();
        int index = 0;
        for (Long articleId : articleIds) {
            Object value = values.get(index++);
            if (value != null) {
                result.put(articleId, "1".equals(value));
            }
        }
        return result;
    }

    private String getKey(Long memberId) {
        return ARTICLE_LIKE.formatted(memberId);
    }

}
