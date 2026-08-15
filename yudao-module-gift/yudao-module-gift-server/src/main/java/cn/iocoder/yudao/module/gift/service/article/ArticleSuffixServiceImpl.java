package cn.iocoder.yudao.module.gift.service.article;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;

import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleSuffixPageReqVO;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleSuffixSaveReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticleSuffixDO;
import cn.iocoder.yudao.module.gift.dal.mysql.article.ArticleSuffixMapper;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;


import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 文章后缀 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class ArticleSuffixServiceImpl implements ArticleSuffixService {

    @Resource
    private ArticleSuffixMapper articleSuffixMapper;

    @Override
    public Long createArticleSuffix(ArticleSuffixSaveReqVO createReqVO) {
        // 插入
        ArticleSuffixDO articleSuffix = BeanUtils.toBean(createReqVO, ArticleSuffixDO.class);
        articleSuffixMapper.insert(articleSuffix);

        // 返回
        return articleSuffix.getId();
    }

    @Override
    public void updateArticleSuffix(ArticleSuffixSaveReqVO updateReqVO) {
        // 校验存在
        validateArticleSuffixExists(updateReqVO.getId());
        // 更新
        ArticleSuffixDO updateObj = BeanUtils.toBean(updateReqVO, ArticleSuffixDO.class);
        articleSuffixMapper.updateById(updateObj);
    }

    @Override
    public void deleteArticleSuffix(Long id) {
        // 校验存在
        validateArticleSuffixExists(id);
        // 删除
        articleSuffixMapper.deleteById(id);
    }

    @Override
        public void deleteArticleSuffixListByIds(List<Long> ids) {
        // 删除
        articleSuffixMapper.deleteByIds(ids);
        }


    private void validateArticleSuffixExists(Long id) {
        if (articleSuffixMapper.selectById(id) == null) {
            throw exception(ARTICLE_SUFFIX_NOT_EXISTS);
        }
    }

    @Override
    public ArticleSuffixDO getArticleSuffix(Long id) {
        return articleSuffixMapper.selectById(id);
    }

    @Override
    public PageResult<ArticleSuffixDO> getArticleSuffixPage(ArticleSuffixPageReqVO pageReqVO) {
        return articleSuffixMapper.selectPage(pageReqVO);
    }

}
