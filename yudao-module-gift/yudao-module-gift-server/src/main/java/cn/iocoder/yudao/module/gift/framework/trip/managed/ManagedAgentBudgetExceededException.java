package cn.iocoder.yudao.module.gift.framework.trip.managed;

import lombok.Getter;

@Getter
public class ManagedAgentBudgetExceededException extends RuntimeException {

    private final ManagedAgentExecutionBudgetDecider.Reason reason;
    private final ManagedAgentExecutionBudgetDecider.Snapshot snapshot;

    public ManagedAgentBudgetExceededException(ManagedAgentExecutionBudgetDecider.Reason reason,
                                                ManagedAgentExecutionBudgetDecider.Snapshot snapshot) {
        super("Managed Agents execution budget exceeded: " + reason);
        this.reason = reason;
        this.snapshot = snapshot;
    }

}
