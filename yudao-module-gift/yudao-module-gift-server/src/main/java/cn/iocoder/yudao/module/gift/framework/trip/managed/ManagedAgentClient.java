package cn.iocoder.yudao.module.gift.framework.trip.managed;

/** 与具体云厂商无关的 Managed Agent 调用入口。 */
public interface ManagedAgentClient extends AutoCloseable {

    String createSession(ManagedAgentSessionCreateRequest request);

    ManagedAgentExecutionResult execute(String sessionId, String task, ManagedAgentExecutionOptions options);

    @Override
    void close();

}
