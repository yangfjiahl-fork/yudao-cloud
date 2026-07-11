package cn.iocoder.yudao.module.gift.service.wool;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.gift.controller.admin.wool.vo.*;
import cn.iocoder.yudao.module.gift.dal.dataobject.wool.WoolDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.gift.dal.mysql.wool.WoolMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.gift.enums.ErrorCodeConstants.*;

/**
 * 羊毛 Service 实现类
 *
 * @author calvin
 */
@Service
@Validated
public class WoolServiceImpl implements WoolService {

    @Resource
    private WoolMapper woolMapper;

    @Override
    public Long createWool(WoolSaveReqVO createReqVO) {
        // 插入
        WoolDO wool = BeanUtils.toBean(createReqVO, WoolDO.class);
        woolMapper.insert(wool);

        // 返回
        return wool.getId();
    }

    @Override
    public void updateWool(WoolSaveReqVO updateReqVO) {
        // 校验存在
        validateWoolExists(updateReqVO.getId());
        // 更新
        WoolDO updateObj = BeanUtils.toBean(updateReqVO, WoolDO.class);
        woolMapper.updateById(updateObj);
    }

    @Override
    public void deleteWool(Long id) {
        // 校验存在
        validateWoolExists(id);
        // 删除
        woolMapper.deleteById(id);
    }

    @Override
        public void deleteWoolListByIds(List<Long> ids) {
        // 删除
        woolMapper.deleteByIds(ids);
        }


    private void validateWoolExists(Long id) {
        if (woolMapper.selectById(id) == null) {
            throw exception(WOOL_NOT_EXISTS);
        }
    }

    @Override
    public WoolDO getWool(Long id) {
        return woolMapper.selectById(id);
    }

    @Override
    public PageResult<WoolDO> getWoolPage(WoolPageReqVO pageReqVO) {
        return woolMapper.selectPage(pageReqVO);
    }

}