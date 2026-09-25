package cn.iocoder.yudao.module.gift.dal.mysql.slider;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.slider.SliderDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.slider.vo.*;

/**
 * 轮播 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface SliderMapper extends BaseMapperX<SliderDO> {

    default PageResult<SliderDO> selectPage(SliderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SliderDO>()
                .eqIfPresent(SliderDO::getPositionCode, reqVO.getPositionCode())
                .eqIfPresent(SliderDO::getCityId, reqVO.getCityId())
                .betweenIfPresent(SliderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SliderDO::getId));
    }

    default SliderDO selectByPositionCodeAndCityId(String positionCode, Long cityId) {
        LambdaQueryWrapperX<SliderDO> query = new LambdaQueryWrapperX<SliderDO>()
                .eq(SliderDO::getPositionCode, positionCode);
        if (cityId == null) {
            query.isNull(SliderDO::getCityId);
        } else {
            query.eq(SliderDO::getCityId, cityId);
        }
        return selectOne(query);
    }

}
