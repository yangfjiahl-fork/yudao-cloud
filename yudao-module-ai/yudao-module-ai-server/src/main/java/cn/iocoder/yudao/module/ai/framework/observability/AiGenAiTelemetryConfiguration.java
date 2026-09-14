package cn.iocoder.yudao.module.ai.framework.observability;

import com.alibaba.loongsuite.otel.util.genai.GenAiTelemetryHandler;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CMS AI 可观测的 GenAI 遥测配置。
 *
 * @see <a href="https://help.aliyun.com/zh/cms/cloudmonitor-2-0/best-practices-for-custom-instrumentation-in-java-llm-applications">Java LLM 应用自定义埋点最佳实践</a>
 */
@Configuration(proxyBeanMethods = false)
public class AiGenAiTelemetryConfiguration {

    /**
     * CMS OTLP SDK 与 ARMS Java Agent 使用各自的 Provider。不能覆盖全局 Provider，避免影响已有 ARMS 自动埋点。
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "yudao.otel.gen-ai", name = "cms-export-enabled")
    public OpenTelemetrySdk cmsGenAiOpenTelemetrySdk() {
        return AutoConfiguredOpenTelemetrySdk.builder().build().getOpenTelemetrySdk();
    }

    @Bean
    @ConditionalOnMissingBean
    public GenAiTelemetryHandler genAiTelemetryHandler(
            ObjectProvider<OpenTelemetrySdk> cmsGenAiOpenTelemetrySdkProvider) {
        OpenTelemetrySdk sdk = cmsGenAiOpenTelemetrySdkProvider.getIfAvailable();
        OpenTelemetry openTelemetry = sdk != null ? sdk : GlobalOpenTelemetry.get();
        return GenAiTelemetryHandler.create(openTelemetry);
    }

}
