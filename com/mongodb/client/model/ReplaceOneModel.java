package com.mongodb.client.model;

import org.bson.conversions.*;
import com.mongodb.assertions.*;

public final class ReplaceOneModel<T> extends WriteModel<T>
{
    private final Bson filter;
    private final T replacement;
    private final UpdateOptions options;
    
    public ReplaceOneModel(final Bson filter, final T replacement) {
        this(filter, replacement, new UpdateOptions());
    }
    
    public ReplaceOneModel(final Bson filter, final T replacement, final UpdateOptions options) {
        this.filter = Assertions.notNull("filter", filter);
        this.options = Assertions.notNull("options", options);
        this.replacement = Assertions.notNull("replacement", replacement);
    }
    
    public Bson getFilter() {
        return this.filter;
    }
    
    public T getReplacement() {
        return this.replacement;
    }
    
    public UpdateOptions getOptions() {
        return this.options;
    }
}
