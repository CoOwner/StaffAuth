package com.mongodb.client.model;

public final class UnwindOptions
{
    private Boolean preserveNullAndEmptyArrays;
    private String includeArrayIndex;
    
    public Boolean isPreserveNullAndEmptyArrays() {
        return this.preserveNullAndEmptyArrays;
    }
    
    public UnwindOptions preserveNullAndEmptyArrays(final Boolean preserveNullAndEmptyArrays) {
        this.preserveNullAndEmptyArrays = preserveNullAndEmptyArrays;
        return this;
    }
    
    public String getIncludeArrayIndex() {
        return this.includeArrayIndex;
    }
    
    public UnwindOptions includeArrayIndex(final String arrayIndexFieldName) {
        this.includeArrayIndex = arrayIndexFieldName;
        return this;
    }
}
