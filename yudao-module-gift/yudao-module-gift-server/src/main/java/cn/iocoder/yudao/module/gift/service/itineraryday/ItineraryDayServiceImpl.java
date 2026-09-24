package cn.iocoder.yudao.module.gift.service.itineraryday;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.itineraryday.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.itineraryday.ItineraryDayDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.itineraryday.ItineraryDayMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 通用行程每日安排 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class ItineraryDayServiceImpl implements ItineraryDayService {

    @Resource
    private ItineraryDayMapper itineraryDayMapper;

    @Override
    public Long createItineraryDay(ItineraryDaySaveReqVO createReqVO) {
        // 插入
        ItineraryDayDO itineraryDay = BeanUtils.toBean(createReqVO, ItineraryDayDO.class);
        itineraryDayMapper.insert(itineraryDay);

        // 返回
        return itineraryDay.getId();
    }

    @Override
    public void updateItineraryDay(ItineraryDaySaveReqVO updateReqVO) {
        // 校验存在
        validateItineraryDayExists(updateReqVO.getId());
        // 更新
        ItineraryDayDO updateObj = BeanUtils.toBean(updateReqVO, ItineraryDayDO.class);
        itineraryDayMapper.updateById(updateObj);
    }

    @Override
    public void deleteItineraryDay(Long id) {
        // 校验存在
        validateItineraryDayExists(id);
        // 删除
        itineraryDayMapper.deleteById(id);
    }

    @Override
        public void deleteItineraryDayListByIds(List<Long> ids) {
        // 删除
        itineraryDayMapper.deleteByIds(ids);
        }


    private void validateItineraryDayExists(Long id) {
        if (itineraryDayMapper.selectById(id) == null) {
            throw exception(ITINERARY_DAY_NOT_EXISTS);
        }
    }

    @Override
    public ItineraryDayDO getItineraryDay(Long id) {
        return itineraryDayMapper.selectById(id);
    }

    @Override
    public PageResult<ItineraryDayDO> getItineraryDayPage(ItineraryDayPageReqVO pageReqVO) {
        return itineraryDayMapper.selectPage(pageReqVO);
    }

}