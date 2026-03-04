package io.github.thebrodigy.awezomlogger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "awezomlog")
public class AwezomLogProperties {
    
    private boolean enabled = true;
    private List<String> maskFields = Arrays.asList(
        "password", "cardNumber", "cvv", "ssn", "apiKey", "token", "secret"
    );
    private int slowCallThreshold = 1000;
    private String format = "COMPACT";
    
    // Getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public List<String> getMaskFields() { return maskFields; }
    public void setMaskFields(List<String> maskFields) { this.maskFields = maskFields; }
    
    public int getSlowCallThreshold() { return slowCallThreshold; }
    public void setSlowCallThreshold(int slowCallThreshold) { 
        this.slowCallThreshold = slowCallThreshold; 
    }
    
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}