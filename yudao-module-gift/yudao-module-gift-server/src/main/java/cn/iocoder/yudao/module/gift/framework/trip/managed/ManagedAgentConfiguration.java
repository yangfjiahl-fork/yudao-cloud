package cn.iocoder.yudao.module.gift.framework.trip.managed;

import cn.iocoder.yudao.framework.tracer.core.util.MdcContextUtils;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ManagedAgentClientProperties.class, ManagedTripAgentProperties.class})
public class ManagedAgentConfiguration {

    public static final String MANAGED_AGENT_TASK_EXECUTOR = "managedAgentTaskExecutor";

    @Bean
    public ManagedAgentExecutionBudgetDecider managedAgentExecutionBudgetDecider() {
        return new ManagedAgentExecutionBudgetDecider();
    }

    @Bean(MANAGED_AGENT_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor managedAgentTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(16);
        executor.setThreadNamePrefix("managed-agent-");
        executor.setTaskDecorator(runnable -> {
            Context parentContext = Context.current();
            Runnable mdcRunnable = MdcContextUtils.wrap(runnable);
            return () -> {
                try (Scope ignored = parentContext.makeCurrent()) {
                    mdcRunnable.run();
                }
            };
        });
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(false);
        return executor;
    }

    @Bean(destroyMethod = "close")
    public ManagedAgentClient managedAgentClient(
            ManagedAgentClientProperties properties,
            ManagedAgentExecutionBudgetDecider budgetDecider,
            @Qualifier(MANAGED_AGENT_TASK_EXECUTOR) ThreadPoolTaskExecutor taskExecutor) {
        // 延迟创建 SDK Client，未启用旅行功能的服务启动时不强制要求 Managed Agents 配置。
        return new ManagedAgentSessionClient(properties, budgetDecider, taskExecutor);
    }

}
