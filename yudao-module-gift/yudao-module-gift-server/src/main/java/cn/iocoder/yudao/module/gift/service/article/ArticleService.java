package cn.iocoder.yudao.module.gift.service.article;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.*;
import cn.iocoder.yudao.module.gift.controller.app.article.vo.AppArticlePageReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 文章 Service 接口
 *
 * @author 羔享科技
 */
public interface ArticleService {

    /**
     * 创建文章
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createArticle(@Valid ArticleSaveReqVO createReqVO);

    /**
     * 更新文章
     *
     * @param updateReqVO 更新信息
     */
    void updateArticle(@Valid ArticleSaveReqVO updateReqVO);

    /**
     * 修改文章状态
     *
     * @param ids    文章编号列表
     * @param status 文章状态
     */
    void changeArticleStatus(List<Long> ids, Integer status);

    /**
     * 删除文章
     *
     * @param id 编号
     */
    void deleteArticle(Long id);

    /**
    * 批量删除文章
    *
    * @param ids 编号
    */
    void deleteArticleListByIds(List<Long> ids);

    /**
     * 获得文章
     *
     * @param id 编号
     * @return 文章
     */
    ArticlesDO getArticle(Long id);

    /**
     * 增加文章浏览次数
     *
     * @param id 文章编号
     */
    void addArticleViewCount(Long id);

    /**
     * 点赞文章
     *
     * @param memberId 会员编号
     * @param articleId 文章编号
     */
    void likeArticle(Long memberId, Long articleId);

    /**
     * 取消点赞文章
     *
     * @param memberId 会员编号
     * @param articleId 文章编号
     */
    void unlikeArticle(Long memberId, Long articleId);

    /**
     * 获得会员已点赞的文章编号集合
     *
     * @param memberId 会员编号
     * @param articleIds 文章编号集合
     * @return 已点赞的文章编号集合
     */
    Set<Long> getLikedArticleIds(Long memberId, Collection<Long> articleIds);

    /**
     * 获得文章分页
     *
     * @param pageReqVO 分页查询
     * @return 文章分页
     */
    PageResult<ArticlesDO> getArticlePage(ArticlePageReqVO pageReqVO);

    /**
     * 获得 APP 文章分页
     *
     * @param pageReqVO 分页查询
     * @return 文章分页
     */
    PageResult<ArticlesDO> getArticlePage(AppArticlePageReqVO pageReqVO);

}
