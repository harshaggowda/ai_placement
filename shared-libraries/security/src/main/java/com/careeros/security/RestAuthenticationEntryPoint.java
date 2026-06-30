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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Renders a {@code 401} as the platform's standard {@link ApiResponse} JSON envelope.
 *
 * <p>Authentication failures surface inside the security filter chain, before Spring MVC, so they are
 * never seen by {@code GlobalExceptionHandler}. This entry point guarantees unauthenticated requests
 * still receive the same consistent error shape (with trace id) as the rest of the API.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.UNAUTHENTICATED.getCode())
                .message(ErrorCode.UNAUTHENTICATED.getDefaultMessage())
                .status(ErrorCode.UNAUTHENTICATED.getHttpStatus().value())
                .path(request.getRequestURI())
                .traceId(MDC.get(CorrelationIdFilter.TRACE_ID_KEY))
                .timestamp(Instant.now())
                .build();
        response.setStatus(ErrorCode.UNAUTHENTICATED.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(error));
    }
}
