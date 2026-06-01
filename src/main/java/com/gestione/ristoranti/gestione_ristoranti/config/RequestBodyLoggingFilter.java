package com.gestione.ristoranti.gestione_ristoranti.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RequestBodyLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestBodyLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().contains("/stato") && "PATCH".equals(request.getMethod())) {
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
            filterChain.doFilter(wrappedRequest, response);
            byte[] body = wrappedRequest.getContentAsByteArray();
            log.info("[RAW_REQUEST] URI={} ContentType={} Body='{}'",
                    request.getRequestURI(),
                    request.getContentType(),
                    new String(body, StandardCharsets.UTF_8));
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
