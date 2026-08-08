package cn.iocoder.yudao.module.gift.dal.mysql.article;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.*;

/**
 * 文章 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface ArticlesMapper extends BaseMapperX<ArticlesDO> {

    default PageResult<ArticlesDO> selectPage(ArticlePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ArticlesDO>()
                .eqIfPresent(ArticlesDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(ArticlesDO::getStatus, reqVO.getStatus())
                .orderByDesc(ArticlesDO::getId));
    }

}
