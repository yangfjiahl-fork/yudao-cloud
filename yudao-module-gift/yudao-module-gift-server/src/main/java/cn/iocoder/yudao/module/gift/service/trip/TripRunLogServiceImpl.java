package cn.iocoder.yudao.module.gift.service.trip;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.gift.dal.dataobject.trip.TripRunDO;
import cn.iocoder.yudao.module.gift.dal.mysql.trip.TripRunMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TripRunLogServiceImpl implements TripRunLogService {

    @Resource
    private TripRunMapper tripRunMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Long create(Long tripId, String stage) {
        TripRunDO run = new TripRunDO();
        run.setTripId(tripId);
        run.setStage(stage);
        run.setStatus(0);
        tripRunMapper.insert(run);
        return run.getId();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void complete(Long runId, String model, Long promptTokens, Long completionTokens, Long totalTokens, long durationMs) {
        tripRunMapper.updateById(new TripRunDO().setId(runId).setStatus(1).setModel(model)
                .setPromptTokens(promptTokens).setCompletionTokens(completionTokens).setTotalTokens(totalTokens)
                .setDurationMs(durationMs));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void fail(Long runId, long durationMs, String errorMessage) {
        tripRunMapper.updateById(new TripRunDO().setId(runId).setStatus(2).setDurationMs(durationMs)
                .setErrorMessage(StrUtil.sub(errorMessage, 0, 500)));
    }

}
