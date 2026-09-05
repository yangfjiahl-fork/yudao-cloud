package cn.iocoder.yudao.framework.tracer.core.util;

import org.slf4j.MDC;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * MDC 上下文传播工具。
 *
 * <p>用于 {@link java.util.concurrent.CompletableFuture} 等未接入 Spring 或 Reactor 的线程池任务。</p>
 */
public final class MdcContextUtils {

    public static final String REACTOR_CONTEXT_MDC_KEY = MdcContextUtils.class.getName() + ".reactorContext";

    private MdcContextUtils() {
    }

    public static Runnable wrap(Runnable runnable) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return () -> runWithContext(mdcContext, runnable);
    }

    public static <T> Supplier<T> wrap(Supplier<T> supplier) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previousMdcContext = MDC.getCopyOfContextMap();
            setMdcContext(mdcContext);
            try {
                return supplier.get();
            } finally {
                setMdcContext(previousMdcContext);
            }
        };
    }

    /**
     * 为当前请求创建的 Reactor 流保存 MDC。配合全局信号钩子，确保 {@code doOnNext}/{@code doOnComplete}
     * 等信号回调也能读取到原始 Trace。
     */
    public static <T> Flux<T> withReactorContext(Flux<T> flux) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        Map<String, String> capturedContext = mdcContext == null ? Collections.emptyMap() : Map.copyOf(mdcContext);
        return flux.contextWrite(context -> context.put(REACTOR_CONTEXT_MDC_KEY, capturedContext));
    }

    public static void runWithContext(Map<String, String> mdcContext, Runnable runnable) {
        runWithContextInternal(mdcContext, runnable);
    }

    private static void runWithContextInternal(Map<String, String> mdcContext, Runnable runnable) {
        Map<String, String> previousMdcContext = MDC.getCopyOfContextMap();
        setMdcContext(mdcContext);
        try {
            runnable.run();
        } finally {
            setMdcContext(previousMdcContext);
        }
    }

    private static void setMdcContext(Map<String, String> mdcContext) {
        if (mdcContext == null) {
            MDC.clear();
            return;
        }
        MDC.setContextMap(mdcContext);
    }

}
