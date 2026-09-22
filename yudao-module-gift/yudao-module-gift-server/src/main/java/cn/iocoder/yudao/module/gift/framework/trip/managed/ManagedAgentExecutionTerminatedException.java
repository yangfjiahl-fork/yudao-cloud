package cn.iocoder.yudao.module.gift.framework.trip.managed;

/** Managed Agent 因超时或资源预算超限而被主动终止。 */
public class ManagedAgentExecutionTerminatedException extends RuntimeException {

    private final ManagedAgentTerminationReason reason;
    private final ManagedAgentExecutionMetrics metrics;

    public ManagedAgentExecutionTerminatedException(ManagedAgentTerminationReason reason,
                                                     ManagedAgentExecutionMetrics metrics) {
        super("Managed Agent execution terminated: " + reason);
        this.reason = reason;
        this.metrics = metrics;
    }

    public ManagedAgentTerminationReason getReason() {
        return reason;
    }

    public ManagedAgentExecutionMetrics getMetrics() {
        return metrics;
    }

}
