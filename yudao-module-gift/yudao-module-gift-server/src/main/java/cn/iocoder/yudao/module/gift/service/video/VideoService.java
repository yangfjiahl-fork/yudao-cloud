package cn.iocoder.yudao.module.gift.service.video;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.gift.controller.admin.video.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.video.VideoDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 视频 Service 接口
 *
 * @author 羔享科技
 */
public interface VideoService {

    /**
     * 随机获得可播放视频列表。
     *
     * @param pageSize 返回数量
     * @return 视频列表，最多 50 条
     */
    List<VideoDO> getRandomVideoList(Integer pageSize);

    /**
     * 将URL授权
     *
     * @param list
     */
    void authUrl(List<VideoDO> list);

    /**
     * 处理阿里云 VOD 转码或截图完成回调。
     *
     * @param callbackReqVO 回调数据
     * @return 是否已处理
     */
    boolean receiveAliyunVideoCallback(AliyunVideoCallbackReqVO callbackReqVO);

    /**
     * 创建视频
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createVideo(@Valid VideoSaveReqVO createReqVO);

    /**
     * 更新视频
     *
     * @param updateReqVO 更新信息
     */
    void updateVideo(@Valid VideoSaveReqVO updateReqVO);

    /**
     * 删除视频
     *
     * @param id 编号
     */
    void deleteVideo(Long id);

    /**
    * 批量删除视频
    *
    * @param ids 编号
    */
    void deleteVideoListByIds(List<Long> ids);

    /**
     * 获得视频
     *
     * @param id 编号
     * @return 视频
     */
    VideoDO getVideo(Long id);

    /**
     * 获得视频分页
     *
     * @param pageReqVO 分页查询
     * @return 视频分页
     */
    PageResult<VideoDO> getVideoPage(VideoPageReqVO pageReqVO);

    /**
     * 根据id获取
     *
     * @param ids
     * @return
     */
    List<VideoDO> getByIds(Set<Long> ids);
}
