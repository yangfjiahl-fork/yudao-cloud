package cn.iocoder.yudao.module.gift.dal.mysql.article;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleCategoryListReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesCategoryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 文章分类 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface ArticlesCategoryMapper extends BaseMapperX<ArticlesCategoryDO> {

    default List<ArticlesCategoryDO> selectList(ArticleCategoryListReqVO listReqVO) {
        return selectList(new LambdaQueryWrapperX<ArticlesCategoryDO>()
                .likeIfPresent(ArticlesCategoryDO::getName, listReqVO.getName())
                .eqIfPresent(ArticlesCategoryDO::getParentId, listReqVO.getParentId())
                .inIfPresent(ArticlesCategoryDO::getParentId, listReqVO.getParentIds())
                .eqIfPresent(ArticlesCategoryDO::getStatus, listReqVO.getStatus())
                .orderByDesc(ArticlesCategoryDO::getSort));
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(ArticlesCategoryDO::getParentId, parentId);
    }

    default List<ArticlesCategoryDO> selectListByStatus(Integer status) {
        return selectList(ArticlesCategoryDO::getStatus, status);
    }

    default List<ArticlesCategoryDO> selectListByIdAndStatus(Collection<Long> ids, Integer status) {
        return selectList(new LambdaQueryWrapperX<ArticlesCategoryDO>()
                .in(ArticlesCategoryDO::getId, ids)
                .eq(ArticlesCategoryDO::getStatus, status));
    }

    default ArticlesCategoryDO selectByParentIdAndName(Long parentId, String name) {
        return selectOne(ArticlesCategoryDO::getParentId, parentId, ArticlesCategoryDO::getName, name);
    }
}
