package cn.iocoder.yudao.module.gift.dal.mysql.article;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.controller.app.article.vo.AppArticlePageReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticlesDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.*;

import java.util.Collection;

/**
 * 文章 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface ArticlesMapper extends BaseMapperX<ArticlesDO> {

    default int updateStatusByIds(Collection<Long> ids, Integer status) {
        return update(new ArticlesDO().setStatus(status), new LambdaQueryWrapperX<ArticlesDO>()
                .in(ArticlesDO::getId, ids));
    }

    default void updateViewCount(Long id) {
        update(null, new LambdaUpdateWrapper<ArticlesDO>()
                .eq(ArticlesDO::getId, id)
                .setSql("view_count = view_count + 1"));
    }

    default PageResult<ArticlesDO> selectPage(ArticlePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ArticlesDO>()
                .eqIfPresent(ArticlesDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(ArticlesDO::getStatus, reqVO.getStatus())
                .orderByDesc(ArticlesDO::getStatus)
                .orderByDesc(ArticlesDO::getSort)
                .orderByDesc(ArticlesDO::getId));
    }

    default PageResult<ArticlesDO> selectPage(AppArticlePageReqVO reqVO) {
        LambdaQueryWrapperX<ArticlesDO> query = new LambdaQueryWrapperX<ArticlesDO>()
                .eqIfPresent(ArticlesDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(ArticlesDO::getStatus, reqVO.getStatus());
        query.ltIfPresent(ArticlesDO::getId, reqVO.getMaxId());
        return selectPage(reqVO, query
                .orderByDesc(ArticlesDO::getSort)
                .orderByDesc(ArticlesDO::getId));
    }

}
