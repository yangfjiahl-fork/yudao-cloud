package cn.iocoder.yudao.module.gift.service.itinerary;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.itinerary.vo.*;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryCityPageReqVO;
import cn.iocoder.yudao.module.gift.controller.app.itinerary.vo.AppItineraryPageReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.itinerary.ItineraryDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.itinerary.ItineraryMapper;
import cn.iocoder.yudao.module.gift.service.usercity.UserCityService;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 行程 Service 实现类
 *
 * @author 羔享科技
 */
@Service
@Validated
public class ItineraryServiceImpl implements ItineraryService {

    @Resource
    private ItineraryMapper itineraryMapper;
    @Resource
    private UserCityService userCityService;

    @Override
    public Long createItinerary(ItinerarySaveReqVO createReqVO) {
        // 插入
        ItineraryDO itinerary = BeanUtils.toBean(createReqVO, ItineraryDO.class);
        itineraryMapper.insert(itinerary);

        // 返回
        return itinerary.getId();
    }

    @Override
    public void updateItinerary(ItinerarySaveReqVO updateReqVO) {
        // 校验存在
        validateItineraryExists(updateReqVO.getId());
        // 更新
        ItineraryDO updateObj = BeanUtils.toBean(updateReqVO, ItineraryDO.class);
        itineraryMapper.updateById(updateObj);
    }

    @Override
    public void deleteItinerary(Long id) {
        // 校验存在
        validateItineraryExists(id);
        // 删除
        itineraryMapper.deleteById(id);
    }

    @Override
        public void deleteItineraryListByIds(List<Long> ids) {
        // 删除
        itineraryMapper.deleteByIds(ids);
        }


    private void validateItineraryExists(Long id) {
        if (itineraryMapper.selectById(id) == null) {
            throw exception(ITINERARY_NOT_EXISTS);
        }
    }

    @Override
    public ItineraryDO getItinerary(Long id) {
        return itineraryMapper.selectById(id);
    }

    @Override
    public PageResult<ItineraryDO> getItineraryPage(ItineraryPageReqVO pageReqVO) {
        return itineraryMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<ItineraryDO> getItineraryExportPage(ItineraryPageReqVO pageReqVO) {
        return itineraryMapper.selectExportPage(pageReqVO);
    }

    @Override
    public PageResult<ItineraryDO> getItineraryPage(AppItineraryPageReqVO pageReqVO) {
        return getAppItineraryPage(pageReqVO, null, pageReqVO.getCategoryId());
    }

    @Override
    public PageResult<ItineraryDO> getItineraryCityPage(Long memberId, AppItineraryCityPageReqVO pageReqVO) {
        Integer cityId = pageReqVO.getCityId();
        if (cityId == null) {
            UserCityService.UserCity userCity = userCityService.getUserCity(memberId);
            if (userCity == null || userCity.cityId() == null) {
                return PageResult.empty();
            }
            cityId = Math.toIntExact(userCity.cityId());
        }
        return getAppItineraryPage(pageReqVO, cityId, pageReqVO.getCategoryId());
    }

    private PageResult<ItineraryDO> getAppItineraryPage(PageParam pageParam, Integer cityId, Long categoryId) {
        return itineraryMapper.selectPage(pageParam, cityId, categoryId);
    }

}
