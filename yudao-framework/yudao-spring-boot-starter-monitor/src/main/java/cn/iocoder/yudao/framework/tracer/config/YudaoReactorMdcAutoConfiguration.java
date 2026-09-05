package cn.iocoder.yudao.framework.tracer.config;

import cn.iocoder.yudao.framework.tracer.core.util.MdcContextUtils;
import jakarta.annotation.PreDestroy;
import org.reactivestreams.Subscription;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.util.Map;

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
        Hooks.onEachOperator(MDC_CONTEXT_HOOK_KEY,
                Operators.lift((Scannable ignored, CoreSubscriber<? super Object> subscriber) -> new MdcContextSubscriber<>(subscriber)));
    }

    @PreDestroy
    public void destroy() {
        Schedulers.resetOnScheduleHook(MDC_CONTEXT_HOOK_KEY);
        Hooks.resetOnEachOperator(MDC_CONTEXT_HOOK_KEY);
    }

    private static final class MdcContextSubscriber<T> implements CoreSubscriber<T> {

        private final CoreSubscriber<? super T> delegate;

        private MdcContextSubscriber(CoreSubscriber<? super T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Context currentContext() {
            return delegate.currentContext();
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            runWithMdcContext(() -> delegate.onSubscribe(subscription));
        }

        @Override
        public void onNext(T value) {
            runWithMdcContext(() -> delegate.onNext(value));
        }

        @Override
        public void onError(Throwable throwable) {
            runWithMdcContext(() -> delegate.onError(throwable));
        }

        @Override
        public void onComplete() {
            runWithMdcContext(delegate::onComplete);
        }

        @SuppressWarnings("unchecked")
        private void runWithMdcContext(Runnable runnable) {
            Object contextValue = currentContext().getOrDefault(MdcContextUtils.REACTOR_CONTEXT_MDC_KEY, null);
            if (!(contextValue instanceof Map<?, ?> rawContext)) {
                runnable.run();
                return;
            }
            MdcContextUtils.runWithContext((Map<String, String>) rawContext, runnable);
        }
    }

}
