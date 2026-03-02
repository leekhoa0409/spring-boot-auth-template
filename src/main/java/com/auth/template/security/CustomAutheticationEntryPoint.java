package com.auth.template.security;

import com.auth.template.common.dto.ErrorResponse;
import com.auth.template.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@AllArgsConstructor
public class CustomAutheticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String requestUri = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");

        boolean isApiRequest = requestUri.startsWith("/api/") ||
                (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE));

        if (isApiRequest) {
            // 1. NẾU LÀ API: Trả về JSON 401 (Logic cũ của bạn)
            ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

            response.setStatus(errorCode.getHttpStatusCode().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .code(errorCode.getCode())
                    .message(errorCode.getMessage())
                    .path(request.getRequestURI())
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            response.flushBuffer();
        }
        else {
            // 2. NẾU LÀ TRÌNH DUYỆT: Redirect thẳng sang Google OAuth2
            response.sendRedirect("/oauth2/authorization/google");
        }
    }
}
