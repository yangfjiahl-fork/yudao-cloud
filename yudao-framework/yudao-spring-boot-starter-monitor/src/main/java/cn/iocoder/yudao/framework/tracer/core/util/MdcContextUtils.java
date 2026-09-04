package cn.iocoder.yudao.framework.tracer.core.util;

import org.slf4j.MDC;

import java.util.Map;
import java.util.function.Supplier;

/**
 * MDC 上下文传播工具。
 *
 * <p>用于 {@link java.util.concurrent.CompletableFuture} 等未接入 Spring 或 Reactor 的线程池任务。</p>
 */
public final class MdcContextUtils {

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

    private static void runWithContext(Map<String, String> mdcContext, Runnable runnable) {
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
