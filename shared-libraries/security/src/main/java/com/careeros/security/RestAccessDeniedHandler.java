package com.careeros.security;

import com.careeros.common.response.ApiResponse;
import com.careeros.common.response.ErrorResponse;
import com.careeros.exception.ErrorCode;
import com.careeros.logging.CorrelationIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Renders a {@code 403} as the platform's standard {@link ApiResponse} JSON envelope, mirroring
 * {@link RestAuthenticationEntryPoint} for authorization failures raised inside the filter chain.
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.ACCESS_DENIED.getCode())
                .message(ErrorCode.ACCESS_DENIED.getDefaultMessage())
                .status(ErrorCode.ACCESS_DENIED.getHttpStatus().value())
                .path(request.getRequestURI())
                .traceId(MDC.get(CorrelationIdFilter.TRACE_ID_KEY))
                .timestamp(Instant.now())
                .build();
        response.setStatus(ErrorCode.ACCESS_DENIED.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(error));
    }
}
