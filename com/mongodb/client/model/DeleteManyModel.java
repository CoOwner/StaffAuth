package com.mongodb.client.model;

import org.bson.conversions.*;
import com.mongodb.assertions.*;

public final class DeleteManyModel<T> extends WriteModel<T>
{
    private final Bson filter;
    private final DeleteOptions options;
    
    public DeleteManyModel(final Bson filter) {
        this(filter, new DeleteOptions());
    }
    
    public DeleteManyModel(final Bson filter, final DeleteOptions options) {
        this.filter = Assertions.notNull("filter", filter);
        this.options = Assertions.notNull("options", options);
    }
    
    public Bson getFilter() {
        return this.filter;
    }
    
    public DeleteOptions getOptions() {
        return this.options;
    }
}
