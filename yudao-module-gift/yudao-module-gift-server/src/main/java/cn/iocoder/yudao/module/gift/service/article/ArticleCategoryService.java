package cn.iocoder.yudao.module.gift.service.article;

import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleCategoryListReqVO;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleCategorySaveReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesCategoryDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 文章分类 Service 接口
 *
 * @author 羔享科技
 */
public interface ArticleCategoryService {

    /**
     * 创建文章分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createArticleCategory(@Valid ArticleCategorySaveReqVO createReqVO);

    /**
     * 更新文章分类
     *
     * @param updateReqVO 更新信息
     */
    void updateArticleCategory(@Valid ArticleCategorySaveReqVO updateReqVO);

    /**
     * 删除文章分类
     *
     * @param id 编号
     */
    void deleteArticleCategory(Long id);

    /**
     * 获得文章分类
     *
     * @param id 编号
     * @return 文章分类
     */
    ArticlesCategoryDO getArticleCategory(Long id);

    /**
     * 获得文章分类列表
     *
     * @param listReqVO 查询条件
     * @return 文章分类列表
     */
    List<ArticlesCategoryDO> getArticleCategoryList(ArticleCategoryListReqVO listReqVO);

    void validateArticleCategory(Long categoryId);
}
