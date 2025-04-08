package com.plutus360.chronologix.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenInfo extends HashMap<String, Object>{

    public TokenInfo() {
        super();
    }


    @Override
    public Object put(String key, Object value) {
        validateKeyValue(key, value);
        return super.put(key, value);
    }



    private void validateKeyValue(String key, Object value) {
        validatePath(key);
        validateHttpMethods(value);
    }

    private void validatePath(String key) {
        if (!Resource.isValidPath(key)) {
            throw new IllegalArgumentException("Invalid resource path: " + key);
        }
    }

    private void validateHttpMethods(Object value) {
        if (!(value instanceof Map)) {
            return;
        }
        Map<?, ?> valueMap = (Map<?, ?>) value;
        if (!valueMap.containsKey("methods")) {
            return;
        }
        List<?> methods = (List<?>) valueMap.get("methods");
        for (Object method : methods) {
            validateSingleMethod(method);
        }
    }

    private void validateSingleMethod(Object method) {
        if (!(method instanceof String)) {
            throw new IllegalArgumentException("HTTP method must be a string");
        }
        String methodName = ((String) method).toUpperCase();
        try {
            HttpMethod.valueOf(methodName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid HTTP method: " + method);
        }
    }

}
