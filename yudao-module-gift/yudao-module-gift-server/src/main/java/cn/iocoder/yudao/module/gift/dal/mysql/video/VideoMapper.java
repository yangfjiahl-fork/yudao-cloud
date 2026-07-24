package cn.iocoder.yudao.module.gift.dal.mysql.video;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.video.VideoDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.video.vo.*;

/**
 * 视频 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface VideoMapper extends BaseMapperX<VideoDO> {

    default PageResult<VideoDO> selectPage(VideoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VideoDO>()
                .eqIfPresent(VideoDO::getVodVideoId, reqVO.getVodVideoId())
                .eqIfPresent(VideoDO::getStatus, reqVO.getStatus())
                .eqIfPresent(VideoDO::getQuality, reqVO.getQuality())
                .orderByDesc(VideoDO::getId));
    }

}