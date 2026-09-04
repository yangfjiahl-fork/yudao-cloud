package cn.iocoder.yudao.module.gift.framework.trip.config;

import cn.iocoder.yudao.framework.tracer.core.util.MdcContextUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 旅行规划异步任务配置。
 */
@Configuration(proxyBeanMethods = false)
public class TripAsyncConfiguration {

    public static final String TRIP_ITINERARY_TASK_EXECUTOR = "tripItineraryTaskExecutor";

    @Bean(TRIP_ITINERARY_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor tripItineraryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(30);
        executor.setThreadNamePrefix("trip-itinerary-");
        executor.setTaskDecorator(MdcContextUtils::wrap);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }

}
