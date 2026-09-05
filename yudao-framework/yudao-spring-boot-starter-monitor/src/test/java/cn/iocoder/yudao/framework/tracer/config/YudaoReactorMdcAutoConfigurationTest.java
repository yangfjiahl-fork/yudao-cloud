package cn.iocoder.yudao.framework.tracer.config;

import cn.iocoder.yudao.framework.tracer.core.util.MdcContextUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YudaoReactorMdcAutoConfigurationTest {

    @Test
    void shouldPropagateMdcToReactorScheduler() throws InterruptedException {
        YudaoReactorMdcAutoConfiguration configuration = new YudaoReactorMdcAutoConfiguration();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> traceId = new AtomicReference<>();
        MDC.put("traceId", "reactor-test-trace");
        try {
            Schedulers.boundedElastic().schedule(() -> {
                traceId.set(MDC.get("traceId"));
                latch.countDown();
            });
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals("reactor-test-trace", traceId.get());
        } finally {
            MDC.remove("traceId");
            configuration.destroy();
        }
    }

    @Test
    void shouldPropagateMdcToReactorSignals() {
        YudaoReactorMdcAutoConfiguration configuration = new YudaoReactorMdcAutoConfiguration();
        AtomicReference<String> traceId = new AtomicReference<>();
        MDC.put("traceId", "reactor-signal-trace");
        try {
            MdcContextUtils.withReactorContext(Flux.<String>create(sink -> {
                        sink.next("event");
                        sink.complete();
                    }).subscribeOn(Schedulers.boundedElastic())
                    .doOnNext(ignored -> traceId.set(MDC.get("traceId"))))
                    .blockLast();
            assertEquals("reactor-signal-trace", traceId.get());
        } finally {
            MDC.remove("traceId");
            configuration.destroy();
        }
    }

}
