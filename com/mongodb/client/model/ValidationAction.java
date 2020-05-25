package com.mongodb.client.model;

import com.mongodb.assertions.*;

public enum ValidationAction
{
    ERROR("error"), 
    WARN("warn");
    
    private final String value;
    
    private ValidationAction(final String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public static ValidationAction fromString(final String validationAction) {
        Assertions.notNull("validationAction", validationAction);
        for (final ValidationAction action : values()) {
            if (validationAction.equalsIgnoreCase(action.value)) {
                return action;
            }
        }
        throw new IllegalArgumentException(String.format("'%s' is not a valid validationAction", validationAction));
    }
}
