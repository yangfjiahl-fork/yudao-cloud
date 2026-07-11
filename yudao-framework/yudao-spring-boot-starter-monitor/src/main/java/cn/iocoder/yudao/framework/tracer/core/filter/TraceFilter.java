package cn.iocoder.yudao.framework.tracer.core.filter;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Trace 过滤器，打印 traceId 到 header 中返回
 *
 * @author 芋道源码
 */
public class TraceFilter extends OncePerRequestFilter {

    /**
     * Header 名 - 链路追踪编号
     */
    private static final String HEADER_NAME_TRACE_ID = "trace-id";

    /**
     * MDC Key - 链路追踪编号
     */
    private static final String MDC_KEY_TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String traceId = TracerUtils.getTraceId();
        if (StrUtil.isEmpty(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(MDC_KEY_TRACE_ID, traceId);
        try {
            // 设置响应 traceId
            response.addHeader(HEADER_NAME_TRACE_ID, traceId);
            // 继续过滤
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY_TRACE_ID);
        }
    }

}
