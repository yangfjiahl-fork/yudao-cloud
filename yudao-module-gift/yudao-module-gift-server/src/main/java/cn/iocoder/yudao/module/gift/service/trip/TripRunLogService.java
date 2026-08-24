package cn.iocoder.yudao.module.gift.service.trip;

/** 旅行编排运行记录；独立事务，避免主流程回滚时丢失审计数据。 */
public interface TripRunLogService {

    Long create(Long tripId, String stage);

    Long create(Long tripId, String stage, String inputJson);

    void complete(Long runId, String model, Long promptTokens, Long completionTokens, Long totalTokens, long durationMs);

    void complete(Long runId, String model, Long promptTokens, Long completionTokens, Long totalTokens, long durationMs,
                  String outputJson);

    void fail(Long runId, long durationMs, String errorMessage);

}
