package io.github.thebrodigy.awezomlogger;

import io.github.thebrodigy.awezomlogger.annotations.AwezomLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Aspect
@Component
public class AwezomLogAspect {
    
    private static final Logger log = LoggerFactory.getLogger(AwezomLogAspect.class);
    
    @Pointcut("@within(io.github.thebrodigy.awezomlogger.annotations.AwezomLog)")
    public void classAnnotated() {}
    
    @Pointcut("@annotation(io.github.thebrodigy.awezomlogger.annotations.AwezomLog)")
    public void methodAnnotated() {}
    
    @Around("classAnnotated() || methodAnnotated()")
    public Object logMethodExecution(ProceedingJoinPoint pjp) throws Throwable {
        String correlationId = CorrelationContext.getOrCreate();

        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        String fullMethod = className + "." + methodName;

        AwezomLog config = getAwezomLogConfig(pjp);

        if (config.includeArgs()) {
            String args = maskArguments(pjp.getArgs(), config.mask());
            log.info("[{}] → {}({})", correlationId, fullMethod, args);
        } else {
            log.info("[{}] → {}()", correlationId, fullMethod);
        }

        long startTime = System.nanoTime();

        try {
            Object result = pjp.proceed();

            long durationMs = (System.nanoTime() - startTime) / 1000000;

            if (config.includeResult()) {
                String resultStr = PiiMasker.mask(result);
                log.info("[{}] ← {}() = {} | {}ms",
                        correlationId, fullMethod, resultStr, durationMs);
            } else {
                log.info("[{}] ← {}() | {}ms",
                        correlationId, fullMethod, durationMs);
            }

            if (durationMs > config.slowCallThreshold()) {
                log.warn("[{}] SLOW CALL: {} took {}ms (threshold: {}ms)",
                        correlationId, fullMethod, durationMs, config.slowCallThreshold());
            }

            return result;
        } catch (Exception e) {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            log.error("[{}] {}() failed after {}ms: {}",
                    correlationId, fullMethod, durationMs, e.getMessage(), e);

            throw e;
        }
    }
    
    private AwezomLog getAwezomLogConfig(ProceedingJoinPoint pjp) {
        try {
            Method method = 
                ((org.aspectj.lang.reflect.MethodSignature) pjp.getSignature())
                    .getMethod();
            
            if (method.isAnnotationPresent(AwezomLog.class)) {
                return method.getAnnotation(AwezomLog.class);
            }
            
            Class<?> clazz = pjp.getTarget().getClass();
            if (clazz.isAnnotationPresent(AwezomLog.class)) {
                return clazz.getAnnotation(AwezomLog.class);
            }
        } catch (Exception ignored) {
        }
        
        return createDefaultConfig();
    }
    
    private AwezomLog createDefaultConfig() {
        return new AwezomLog() {
            @Override public String[] mask() { return new String[0]; }
            @Override public boolean logRequest() { return false; }
            @Override public boolean logResponse() { return false; }
            @Override public boolean includeArgs() { return true; }
            @Override public boolean includeResult() { return true; }
            @Override public boolean includeExecutionTime() { return true; }
            @Override public int slowCallThreshold() { return 1000; }
            @Override public boolean logHeaders() { return false; }
            @Override public String format() { return "COMPACT"; }
            @Override public String condition() { return ""; }
            @Override public Class<? extends Annotation> annotationType() { 
                return AwezomLog.class;
            }
        };
    }
    
    private String maskArguments(Object[] args, String[] maskFields) {
        if (args == null || args.length == 0) return "";
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(PiiMasker.mask(args[i]));
        }
        return sb.toString();
    }
}