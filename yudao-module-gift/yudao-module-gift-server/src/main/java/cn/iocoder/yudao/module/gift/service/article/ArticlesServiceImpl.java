package cn.iocoder.yudao.module.gift.service.article;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.*;
import cn.iocoder.yudao.module.gift.controller.app.article.vo.AppArticlePageReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.article.ArticlesMapper;

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
    public PageResult<ArticlesDO> getArticlePage(ArticlePageReqVO pageReqVO) {
        return articlesMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<ArticlesDO> getArticlePage(AppArticlePageReqVO pageReqVO) {
        return articlesMapper.selectPage(pageReqVO);
    }

}
