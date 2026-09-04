package cn.iocoder.yudao.framework.tracer.config;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
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

}
