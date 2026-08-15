package cn.iocoder.yudao.module.gift.service.article;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleCategoryListReqVO;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleCategorySaveReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesCategoryDO;
import cn.iocoder.yudao.module.gift.dal.mysql.article.ArticlesCategoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 文章分类 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class ArticlesCategoryServiceImpl implements ArticleCategoryService {

    @Resource
    private ArticlesCategoryMapper articlesCategoryMapper;

    @Override
    public Long createArticleCategory(ArticleCategorySaveReqVO createReqVO) {
        // 校验父分类编号，0 表示一级分类的有效性
        validateParentArticleCategory(null, createReqVO.getParentId());
        // 校验分类名称的唯一性
        validateArticleCategoryNameUnique(null, createReqVO.getParentId(), createReqVO.getName());

        // 插入
        ArticlesCategoryDO articlesCategory = BeanUtils.toBean(createReqVO, ArticlesCategoryDO.class);
        articlesCategoryMapper.insert(articlesCategory);

        // 返回
        return articlesCategory.getId();
    }

    @Override
    public void updateArticleCategory(ArticleCategorySaveReqVO updateReqVO) {
        // 校验存在
        validateArticleCategoryExists(updateReqVO.getId());
        // 校验父分类编号，0 表示一级分类的有效性
        validateParentArticleCategory(updateReqVO.getId(), updateReqVO.getParentId());
        // 校验分类名称的唯一性
        validateArticleCategoryNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());

        // 更新
        ArticlesCategoryDO updateObj = BeanUtils.toBean(updateReqVO, ArticlesCategoryDO.class);
        articlesCategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteArticleCategory(Long id) {
        // 校验存在
        validateArticleCategoryExists(id);
        // 校验是否有子文章分类
        if (articlesCategoryMapper.selectCountByParentId(id) > 0) {
            throw exception(ARTICLE_CATEGORY_EXISTS_CHILDREN);
        }
        // 删除
        articlesCategoryMapper.deleteById(id);
    }


    private void validateArticleCategoryExists(Long id) {
        if (articlesCategoryMapper.selectById(id) == null) {
            throw exception(ARTICLE_CATEGORY_NOT_EXISTS);
        }
    }

    private void validateParentArticleCategory(Long id, Long parentId) {
        if (parentId == null || ArticlesCategoryDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父文章分类
        if (Objects.equals(id, parentId)) {
            throw exception(ARTICLE_CATEGORY_PARENT_ERROR);
        }
        // 2. 父文章分类不存在
        ArticlesCategoryDO parentArticlesCategory = articlesCategoryMapper.selectById(parentId);
        if (parentArticlesCategory == null) {
            throw exception(ARTICLE_CATEGORY_PARENT_NOT_EXISTS);
        }
        // 3. 递归校验父文章分类，如果父文章分类是自己的子文章分类，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentArticlesCategory.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(ARTICLE_CATEGORY_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父文章分类
            if (parentId == null || ArticlesCategoryDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentArticlesCategory = articlesCategoryMapper.selectById(parentId);
            if (parentArticlesCategory == null) {
                break;
            }
        }
    }

    private void validateArticleCategoryNameUnique(Long id, Long parentId, String name) {
        ArticlesCategoryDO articlesCategory = articlesCategoryMapper.selectByParentIdAndName(parentId, name);
        if (articlesCategory == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的文章分类
        if (id == null) {
            throw exception(ARTICLE_CATEGORY_NAME_DUPLICATE);
        }
        if (!Objects.equals(articlesCategory.getId(), id)) {
            throw exception(ARTICLE_CATEGORY_NAME_DUPLICATE);
        }
    }

    @Override
    public ArticlesCategoryDO getArticleCategory(Long id) {
        return articlesCategoryMapper.selectById(id);
    }

    @Override
    public List<ArticlesCategoryDO> getArticleCategoryList(ArticleCategoryListReqVO listReqVO) {
        return articlesCategoryMapper.selectList(listReqVO);
    }

    @Override
    public void validateArticleCategory(Long categoryId) {
        validateArticleCategoryExists(categoryId);
    }
}
