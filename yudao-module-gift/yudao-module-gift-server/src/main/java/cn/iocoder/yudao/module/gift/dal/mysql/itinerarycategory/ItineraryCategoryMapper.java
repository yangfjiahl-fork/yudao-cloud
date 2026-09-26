package cn.iocoder.yudao.module.gift.dal.mysql.itinerarycategory;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerarycategory.ItineraryCategoryDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.itinerarycategory.vo.*;

/**
 * 行程类别 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface ItineraryCategoryMapper extends BaseMapperX<ItineraryCategoryDO> {

    default PageResult<ItineraryCategoryDO> selectPage(ItineraryCategoryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ItineraryCategoryDO>()
                .likeIfPresent(ItineraryCategoryDO::getTitle, reqVO.getTitle())
                .eqIfPresent(ItineraryCategoryDO::getIcon, reqVO.getIcon())
                .eqIfPresent(ItineraryCategoryDO::getSort, reqVO.getSort())
                .eqIfPresent(ItineraryCategoryDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ItineraryCategoryDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ItineraryCategoryDO::getId));
    }

    default List<ItineraryCategoryDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ItineraryCategoryDO>()
                .eq(ItineraryCategoryDO::getStatus, status)
                .orderByDesc(ItineraryCategoryDO::getSort)
                .orderByDesc(ItineraryCategoryDO::getId));
    }

}
