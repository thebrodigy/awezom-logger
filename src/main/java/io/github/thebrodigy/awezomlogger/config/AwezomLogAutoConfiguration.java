package io.github.thebrodigy.awezomlogger.config;

import io.github.thebrodigy.awezomlogger.CorrelationIdFilter;
import io.github.thebrodigy.awezomlogger.AwezomLogAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "awezomlog.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AwezomLogProperties.class)
public class AwezomLogAutoConfiguration {
    
    @Bean
    public AwezomLogAspect awezomLogAspect() {
        return new AwezomLogAspect();
    }
    
    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }
}