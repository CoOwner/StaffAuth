package com.mongodb.client.model;

public enum CollationMaxVariable
{
    PUNCT("punct"), 
    SPACE("space");
    
    private final String value;
    
    private CollationMaxVariable(final String caseFirst) {
        this.value = caseFirst;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public static CollationMaxVariable fromString(final String collationMaxVariable) {
        if (collationMaxVariable != null) {
            for (final CollationMaxVariable maxVariable : values()) {
                if (collationMaxVariable.equals(maxVariable.value)) {
                    return maxVariable;
                }
            }
        }
        throw new IllegalArgumentException(String.format("'%s' is not a valid collationMaxVariable", collationMaxVariable));
    }
}
