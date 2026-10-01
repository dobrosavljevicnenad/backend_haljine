package io.github.dobrosavljevicnenad.dressinventory.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Returns 401 as a Problem Details JSON body (instead of an empty response),
 * consistent with all other API errors.
 */
@Component
class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String MESSAGE = "Token nije prosleđen ili nije važeći";
    private static final String BODY = """
            {"type":"about:blank","title":"Unauthorized","status":401,"detail":"%s","error":"%s"}"""
            .formatted(MESSAGE, MESSAGE);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(BODY);
    }
}
