package cn.iocoder.yudao.framework.quartz.config;

import com.alibaba.ttl.TtlRunnable;
import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

/**
 * 异步任务 Configuration
 */
@AutoConfiguration
@EnableAsync
public class YudaoAsyncAutoConfiguration {

    private static final TaskDecorator CONTEXT_TASK_DECORATOR = runnable -> {
        Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();
        return TtlRunnable.get(() -> {
            Map<String, String> previousMdcContextMap = MDC.getCopyOfContextMap();
            setMdcContextMap(mdcContextMap);
            try {
                runnable.run();
            } finally {
                setMdcContextMap(previousMdcContextMap);
            }
        });
    };

    @Bean
    public BeanPostProcessor threadPoolTaskExecutorBeanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            @SuppressWarnings("PatternVariableCanBeUsed")
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                // 处理 ThreadPoolTaskExecutor
                if (bean instanceof ThreadPoolTaskExecutor) {
                    ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) bean;
                    executor.setTaskDecorator(CONTEXT_TASK_DECORATOR);
                    return executor;
                }
                // 处理 SimpleAsyncTaskExecutor
                // 参考 https://t.zsxq.com/CBoks 增加
                if (bean instanceof SimpleAsyncTaskExecutor) {
                    SimpleAsyncTaskExecutor executor = (SimpleAsyncTaskExecutor) bean;
                    executor.setTaskDecorator(CONTEXT_TASK_DECORATOR);
                    return executor;
                }
                return bean;
            }

        };
    }

    private static void setMdcContextMap(Map<String, String> mdcContextMap) {
        if (mdcContextMap == null) {
            MDC.clear();
            return;
        }
        MDC.setContextMap(mdcContextMap);
    }

}
