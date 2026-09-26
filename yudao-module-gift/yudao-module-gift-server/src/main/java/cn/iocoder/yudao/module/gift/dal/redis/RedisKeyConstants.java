package cn.iocoder.yudao.module.gift.dal.redis;

/**
 * Gift Redis Key 常量
 *
 * @author 羔享科技
 */
public interface RedisKeyConstants {

    /**
     * 会员文章点赞状态，采用 HASH 结构
     * <p>
     * KEY 格式：gift:article:like:{memberId}
     * HASH KEY：{articleId}
     * VALUE：1-已点赞，0-未点赞
     */
    String ARTICLE_LIKE = "gift:article:like:%d";

    /**
     * 会员当前城市
     * <p>
     * KEY 格式：gift:member:current-city:{memberId}
     * VALUE：城市编号与名称 JSON
     */
    String MEMBER_CURRENT_CITY = "gift:member:current-city:%d";

}
