package com.mongodb;

import java.util.*;

public enum ErrorCategory
{
    UNCATEGORIZED, 
    DUPLICATE_KEY, 
    EXECUTION_TIMEOUT;
    
    private static final List<Integer> DUPLICATE_KEY_ERROR_CODES;
    private static final List<Integer> EXECUTION_TIMEOUT_ERROR_CODES;
    
    public static ErrorCategory fromErrorCode(final int code) {
        if (ErrorCategory.DUPLICATE_KEY_ERROR_CODES.contains(code)) {
            return ErrorCategory.DUPLICATE_KEY;
        }
        if (ErrorCategory.EXECUTION_TIMEOUT_ERROR_CODES.contains(code)) {
            return ErrorCategory.EXECUTION_TIMEOUT;
        }
        return ErrorCategory.UNCATEGORIZED;
    }
    
    static {
        DUPLICATE_KEY_ERROR_CODES = Arrays.asList(11000, 11001, 12582);
        EXECUTION_TIMEOUT_ERROR_CODES = Arrays.asList(50);
    }
}
