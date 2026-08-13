package cn.iocoder.yudao.module.gift.dal.mysql.article;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticleLikeDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.List;

/**
 * 文章点赞记录 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface ArticleLikeMapper extends BaseMapperX<ArticleLikeDO> {

    @Select("SELECT id, article_id, member_id, deleted FROM gift_article_like "
            + "WHERE article_id = #{articleId} AND member_id = #{memberId} LIMIT 1")
    ArticleLikeDO selectByArticleIdAndMemberId(@Param("articleId") Long articleId, @Param("memberId") Long memberId);

    @Update("UPDATE gift_article_like SET deleted = b'0', updater = #{updater} "
            + "WHERE id = #{id} AND deleted = b'1'")
    int restoreById(@Param("id") Long id, @Param("updater") String updater);

    default List<ArticleLikeDO> selectListByMemberIdAndArticleIds(Long memberId, Collection<Long> articleIds) {
        return selectList(new LambdaQueryWrapperX<ArticleLikeDO>()
                .select(ArticleLikeDO::getArticleId)
                .eq(ArticleLikeDO::getMemberId, memberId)
                .in(ArticleLikeDO::getArticleId, articleIds));
    }

    default int deleteByArticleIdAndMemberId(Long articleId, Long memberId) {
        return delete(Wrappers.<ArticleLikeDO>lambdaQuery()
                .eq(ArticleLikeDO::getArticleId, articleId)
                .eq(ArticleLikeDO::getMemberId, memberId));
    }

}
