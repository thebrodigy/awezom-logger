package io.github.thebrodigy.awezomlogger;

import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CorrelationIdFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) 
            throws ServletException, IOException {
        
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null) {
            correlationId = CorrelationContext.getOrCreate();
        } else {
            CorrelationContext.setCurrentId(correlationId);
        }
        
        response.setHeader("X-Correlation-ID", correlationId);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            CorrelationContext.clear();
        }
    }
}