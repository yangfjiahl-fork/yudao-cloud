package cn.iocoder.yudao.module.gift.dal.mysql.video;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.video.VideoDO;
import cn.iocoder.yudao.module.gift.enums.VideoStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.gift.controller.admin.video.vo.*;

/**
 * 视频 Mapper
 *
 * @author 羔享科技
 */
@Mapper
public interface VideoMapper extends BaseMapperX<VideoDO> {

    default VideoDO selectByVodVideoId(String vodVideoId) {
        return selectOne(VideoDO::getVodVideoId, vodVideoId);
    }

    default List<VideoDO> selectRecentOnlineList(Integer limit) {
        return selectList(new LambdaQueryWrapperX<VideoDO>()
                .eq(VideoDO::getStatus, VideoStatusEnum.ONLINE.getType())
                .orderByDesc(VideoDO::getId)
                .last("LIMIT " + limit));
    }

    default PageResult<VideoDO> selectPage(VideoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VideoDO>()
                .eqIfPresent(VideoDO::getVodVideoId, reqVO.getVodVideoId())
                .eqIfPresent(VideoDO::getStatus, reqVO.getStatus())
                .eqIfPresent(VideoDO::getQuality, reqVO.getQuality())
                .orderByDesc(VideoDO::getId));
    }

}
