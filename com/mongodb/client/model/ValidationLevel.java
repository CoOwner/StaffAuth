package com.mongodb.client.model;

import com.mongodb.assertions.*;

public enum ValidationLevel
{
    OFF("off"), 
    STRICT("strict"), 
    MODERATE("moderate");
    
    private final String value;
    
    private ValidationLevel(final String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public static ValidationLevel fromString(final String validationLevel) {
        Assertions.notNull("ValidationLevel", validationLevel);
        for (final ValidationLevel action : values()) {
            if (validationLevel.equalsIgnoreCase(action.value)) {
                return action;
            }
        }
        throw new IllegalArgumentException(String.format("'%s' is not a valid ValidationLevel", validationLevel));
    }
}
