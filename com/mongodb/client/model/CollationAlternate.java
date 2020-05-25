package com.mongodb.client.model;

public enum CollationAlternate
{
    NON_IGNORABLE("non-ignorable"), 
    SHIFTED("shifted");
    
    private final String value;
    
    private CollationAlternate(final String caseFirst) {
        this.value = caseFirst;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public static CollationAlternate fromString(final String collationAlternate) {
        if (collationAlternate != null) {
            for (final CollationAlternate alternate : values()) {
                if (collationAlternate.equals(alternate.value)) {
                    return alternate;
                }
            }
        }
        throw new IllegalArgumentException(String.format("'%s' is not a valid collationAlternate", collationAlternate));
    }
}
