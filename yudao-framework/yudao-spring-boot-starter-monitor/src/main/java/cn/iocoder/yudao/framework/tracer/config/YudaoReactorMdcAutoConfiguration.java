package cn.iocoder.yudao.framework.tracer.config;

import cn.iocoder.yudao.framework.tracer.core.util.MdcContextUtils;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import reactor.core.scheduler.Schedulers;

/**
 * 将 MDC 自动传播到 Reactor Scheduler 的异步任务。
 */
@AutoConfiguration
@ConditionalOnClass(Schedulers.class)
@ConditionalOnProperty(prefix = "yudao.tracer", value = "enable", matchIfMissing = true)
public class YudaoReactorMdcAutoConfiguration {

    private static final String MDC_CONTEXT_HOOK_KEY = "yudao.trace.mdc";

    public YudaoReactorMdcAutoConfiguration() {
        Schedulers.onScheduleHook(MDC_CONTEXT_HOOK_KEY, MdcContextUtils::wrap);
    }

    @PreDestroy
    public void destroy() {
        Schedulers.resetOnScheduleHook(MDC_CONTEXT_HOOK_KEY);
    }

}
