package cn.iocoder.yudao.module.gift.dal.mysql.slideritem;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.slideritem.SliderItemDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.slideritem.vo.*;

/**
 * 轮播图 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface SliderItemMapper extends BaseMapperX<SliderItemDO> {

    default PageResult<SliderItemDO> selectPage(SliderItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SliderItemDO>()
                .eqIfPresent(SliderItemDO::getSliderId, reqVO.getSliderId())
                .eqIfPresent(SliderItemDO::getImageUrl, reqVO.getImageUrl())
                .eqIfPresent(SliderItemDO::getImageWidth, reqVO.getImageWidth())
                .eqIfPresent(SliderItemDO::getImageHeight, reqVO.getImageHeight())
                .eqIfPresent(SliderItemDO::getSort, reqVO.getSort())
                .eqIfPresent(SliderItemDO::getJumpPage, reqVO.getJumpPage())
                .eqIfPresent(SliderItemDO::getJumpPageId, reqVO.getJumpPageId())
                .betweenIfPresent(SliderItemDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SliderItemDO::getId));
    }

    default List<SliderItemDO> selectListBySliderId(Long sliderId) {
        return selectList(new LambdaQueryWrapperX<SliderItemDO>()
                .eq(SliderItemDO::getSliderId, sliderId)
                .orderByAsc(SliderItemDO::getSort)
                .orderByAsc(SliderItemDO::getId));
    }

}
