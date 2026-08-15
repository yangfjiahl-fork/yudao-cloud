package cn.iocoder.yudao.module.gift.dal.mysql.article;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleSuffixPageReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticleSuffixDO;

import org.apache.ibatis.annotations.Mapper;

/**
 * 文章后缀 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface ArticleSuffixMapper extends BaseMapperX<ArticleSuffixDO> {

    default PageResult<ArticleSuffixDO> selectPage(ArticleSuffixPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ArticleSuffixDO>()
                .likeIfPresent(ArticleSuffixDO::getTitle, reqVO.getTitle())
                // 列表不查询正文，避免加载大字段
                .select(ArticleSuffixDO::getId, ArticleSuffixDO::getTitle, ArticleSuffixDO::getCreateTime)
                .orderByDesc(ArticleSuffixDO::getId));
    }

}
