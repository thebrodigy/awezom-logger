package io.github.thebrodigy.awezomlogger;

import io.github.thebrodigy.awezomlogger.annotations.NoLog;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class PiiMasker {
    
    private static final Map<String, MaskingPattern> PATTERNS = new HashMap<>();
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[^@]+@[^@]+$");
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\+?[0-9\\-\\(\\)\\s]+$");
    
    static {
        register("cardNumber", new MaskingPattern("****", 4, 4));
        register("creditCard", new MaskingPattern("****", 4, 4));
        register("cvv", new MaskingPattern("***"));
        register("cvc", new MaskingPattern("***"));
        register("ssn", new MaskingPattern("***-**", 0, 4));
        register("password", new MaskingPattern("****"));
        register("apiKey", new MaskingPattern("****", 4, 4));
        register("token", new MaskingPattern("****", 4, 4));
        register("secret", new MaskingPattern("****"));
        register("email", new MaskingPattern("****", 1, 5));
        register("phone", new MaskingPattern("***", 1, 4));
    }
    
    public static String mask(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) {
            return maskString((String) obj);
        }
        
        try {
            Class<?> clazz = obj.getClass();
            String className = clazz.getSimpleName();
            StringBuilder sb = new StringBuilder(className).append("{");
            
            Field[] fields = clazz.getDeclaredFields();
            boolean first = true;
            
            for (Field field : fields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                
                if (field.isAnnotationPresent(NoLog.class)) {
                    continue;
                }
                
                if (!first) sb.append(", ");
                first = false;
                
                field.setAccessible(true);
                Object value = field.get(obj);
                
                if (isSensitive(field.getName())) {
                    String masked = maskValue(field.getName(), value);
                    sb.append(field.getName()).append(": ").append(masked);
                } else {
                    sb.append(field.getName()).append(": ").append(value);
                }
            }
            
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            return obj.toString();
        }
    }
    
    private static boolean isSensitive(String fieldName) {
        String lower = fieldName.toLowerCase();
        
        if (PATTERNS.containsKey(fieldName) || PATTERNS.containsKey(lower)) {
            return true;
        }
        
        return lower.contains("password") ||
               lower.contains("secret") ||
               lower.contains("token") ||
               lower.contains("key") ||
               lower.contains("card") ||
               lower.contains("ssn") ||
               lower.contains("cvv") ||
               lower.contains("pin");
    }
    
    private static String maskValue(String fieldName, Object value) {
        if (value == null) return "null";
        
        String str = value.toString();
        
        MaskingPattern pattern = PATTERNS.get(fieldName);
        if (pattern != null) {
            return pattern.mask(str);
        }
        
        if (EMAIL_PATTERN.matcher(str).matches()) {
            return maskEmail(str);
        }
        if (PHONE_PATTERN.matcher(str).matches()) {
            return maskPhone(str);
        }
        
        if (str.length() <= 4) return "****";
        return str.substring(0, 2) + "****" + str.substring(str.length() - 2);
    }
    
    private static String maskString(String str) {
        if (str == null || str.isEmpty()) return "null";
        if (str.length() <= 4) return "****";
        return str.substring(0, 2) + "****";
    }
    
    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "****@***";
        
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (local.length() <= 2) {
            return "****" + domain;
        }
        
        return local.charAt(0) + "****" + domain;
    }
    
    private static String maskPhone(String phone) {
        if (phone.length() <= 5) return "****";
        
        int start = 0;
        for (int i = 0; i < phone.length(); i++) {
            if (Character.isDigit(phone.charAt(i))) {
                start = i;
                break;
            }
        }
        
        int end = phone.length() - 1;
        for (int i = phone.length() - 1; i >= 0; i--) {
            if (Character.isDigit(phone.charAt(i))) {
                end = i;
                break;
            }
        }
        
        return phone.substring(0, start + 1) + "****" + phone.substring(end);
    }
    
    private static void register(String fieldName, MaskingPattern pattern) {
        PATTERNS.put(fieldName, pattern);
    }
}

class MaskingPattern {
    private final String mask;
    private final int prefixChars;
    private final int suffixChars;
    
    public MaskingPattern(String mask) {
        this(mask, 0, 0);
    }
    
    public MaskingPattern(String mask, int prefixChars, int suffixChars) {
        this.mask = mask;
        this.prefixChars = prefixChars;
        this.suffixChars = suffixChars;
    }
    
    public String mask(String value) {
        if (value == null || value.isEmpty()) return mask;
        if (value.length() <= prefixChars + suffixChars) return mask;
        
        String prefix = value.substring(0, Math.min(prefixChars, value.length()));
        String suffix = value.substring(Math.max(0, value.length() - suffixChars));
        
        return prefix + mask + suffix;
    }
}