package cn.iocoder.yudao.module.gift.service.article;

import com.baomidou.lock.annotation.Lock4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.*;
import cn.iocoder.yudao.module.gift.controller.app.article.vo.AppArticlePageReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesDO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticleLikeDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.article.ArticlesMapper;
import cn.iocoder.yudao.module.gift.dal.mysql.article.ArticleLikeMapper;
import cn.iocoder.yudao.module.gift.dal.redis.article.ArticleLikeRedisDAO;
import cn.iocoder.yudao.module.gift.enums.ArticleStatusEnum;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 文章 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class ArticlesServiceImpl implements ArticleService {

    @Resource
    private ArticlesMapper articlesMapper;
    @Resource
    private ArticleLikeMapper articleLikeMapper;
    @Resource
    private ArticleLikeRedisDAO articleLikeRedisDAO;

    @Override
    public Long createArticle(ArticleSaveReqVO createReqVO) {
        // 插入
        ArticlesDO article = BeanUtils.toBean(createReqVO, ArticlesDO.class);
        articlesMapper.insert(article);

        // 返回
        return article.getId();
    }

    @Override
    public void updateArticle(ArticleSaveReqVO updateReqVO) {
        // 校验存在
        validateArticleExists(updateReqVO.getId());
        // 更新
        ArticlesDO updateObj = BeanUtils.toBean(updateReqVO, ArticlesDO.class);
        articlesMapper.updateById(updateObj);
    }

    @Override
    public void changeArticleStatus(List<Long> ids, Integer status) {
        articlesMapper.updateStatusByIds(ids, status);
    }

    @Override
    public void deleteArticle(Long id) {
        // 校验存在
        validateArticleExists(id);
        // 删除
        articlesMapper.deleteById(id);
    }

    @Override
        public void deleteArticleListByIds(List<Long> ids) {
        // 删除
        articlesMapper.deleteByIds(ids);
        }


    private void validateArticleExists(Long id) {
        if (articlesMapper.selectById(id) == null) {
            throw exception(ARTICLE_NOT_EXISTS);
        }
    }

    @Override
    public ArticlesDO getArticle(Long id) {
        return articlesMapper.selectById(id);
    }

    @Override
    public void addArticleViewCount(Long id) {
        articlesMapper.updateViewCount(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = {"#memberId", "#articleId"}, expire = 10000, acquireTimeout = 3000)
    public void likeArticle(Long memberId, Long articleId) {
        validateOnlineArticleExists(articleId);
        ArticleLikeDO existingArticleLike = articleLikeMapper.selectByArticleIdAndMemberId(articleId, memberId);
        if (existingArticleLike != null && !Boolean.TRUE.equals(existingArticleLike.getDeleted())) {
            articleLikeRedisDAO.updateLiked(memberId, articleId, true);
            return;
        }
        if (existingArticleLike != null) {
            articleLikeMapper.restoreById(existingArticleLike.getId(), String.valueOf(memberId));
        } else {
            ArticleLikeDO articleLike = new ArticleLikeDO();
            articleLike.setArticleId(articleId);
            articleLike.setMemberId(memberId);
            articleLikeMapper.insert(articleLike);
        }
        articlesMapper.incrementLikeCount(articleId);
        articleLikeRedisDAO.updateLiked(memberId, articleId, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Lock4j(keys = {"#memberId", "#articleId"}, expire = 10000, acquireTimeout = 3000)
    public void unlikeArticle(Long memberId, Long articleId) {
        if (articleLikeMapper.deleteByArticleIdAndMemberId(articleId, memberId) > 0) {
            articlesMapper.decrementLikeCount(articleId);
        }
        articleLikeRedisDAO.updateLiked(memberId, articleId, false);
    }

    @Override
    public Set<Long> getLikedArticleIds(Long memberId, Collection<Long> articleIds) {
        if (memberId == null || articleIds.isEmpty()) {
            return Collections.emptySet();
        }
        Map<Long, Boolean> cachedLikedMap = articleLikeRedisDAO.getLikedMap(memberId, articleIds);
        Set<Long> likedArticleIds = new HashSet<>();
        for (Map.Entry<Long, Boolean> entry : cachedLikedMap.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                likedArticleIds.add(entry.getKey());
            }
        }

        List<Long> missingArticleIds = articleIds.stream()
                .filter(articleId -> !cachedLikedMap.containsKey(articleId))
                .toList();
        if (missingArticleIds.isEmpty()) {
            return likedArticleIds;
        }
        List<ArticleLikeDO> articleLikes = articleLikeMapper.selectListByMemberIdAndArticleIds(memberId, missingArticleIds);
        Set<Long> databaseLikedArticleIds = new HashSet<>(convertList(articleLikes, ArticleLikeDO::getArticleId));
        articleLikeRedisDAO.updateLiked(memberId, missingArticleIds, databaseLikedArticleIds);
        likedArticleIds.addAll(databaseLikedArticleIds);
        return likedArticleIds;
    }

    @Override
    public PageResult<ArticlesDO> getArticlePage(ArticlePageReqVO pageReqVO) {
        return articlesMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<ArticlesDO> getArticlePage(AppArticlePageReqVO pageReqVO) {
        return articlesMapper.selectPage(pageReqVO);
    }

    private void validateOnlineArticleExists(Long articleId) {
        ArticlesDO article = articlesMapper.selectById(articleId);
        if (article == null || !Objects.equals(article.getStatus(), ArticleStatusEnum.ONLINE.getType())) {
            throw exception(ARTICLE_NOT_EXISTS);
        }
    }

}
