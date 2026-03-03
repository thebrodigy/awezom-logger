package io.github.thebrodigy.awezomlogger;

import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.UUID;

public class CorrelationContext {
    
    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();
    private static final String CORRELATION_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";
    
    public static String getOrCreate() {
        String id = correlationId.get();
        if (id == null) {
            id = getFromRequest();
            if (id == null) {
                id = generateId();
            }
            setCurrentId(id);
        }
        return id;
    }
    
    public static String getCurrentId() {
        return getOrCreate();
    }
    
    public static void setCurrentId(String id) {
        correlationId.set(id);
        MDC.put(MDC_KEY, id);
    }
    
    public static void clear() {
        correlationId.remove();
        MDC.remove(MDC_KEY);
    }
    
    private static String getFromRequest() {
        try {
            ServletRequestAttributes attrs = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String id = attrs.getRequest().getHeader(CORRELATION_HEADER);
                if (id != null && !id.isEmpty()) {
                    return id;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    
    private static String generateId() {
        return "CORR-" + System.currentTimeMillis() + "-" + 
               UUID.randomUUID().toString().substring(0, 8);
    }
}