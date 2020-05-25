package com.mongodb.client.model;

public enum CollationCaseFirst
{
    UPPER("upper"), 
    LOWER("lower"), 
    OFF("off");
    
    private final String value;
    
    private CollationCaseFirst(final String caseFirst) {
        this.value = caseFirst;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public static CollationCaseFirst fromString(final String collationCaseFirst) {
        if (collationCaseFirst != null) {
            for (final CollationCaseFirst caseFirst : values()) {
                if (collationCaseFirst.equals(caseFirst.value)) {
                    return caseFirst;
                }
            }
        }
        throw new IllegalArgumentException(String.format("'%s' is not a valid collationCaseFirst", collationCaseFirst));
    }
}
