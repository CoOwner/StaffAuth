package com.mongodb.client.model;

import org.bson.conversions.*;
import com.mongodb.assertions.*;

public class DeleteOneModel<T> extends WriteModel<T>
{
    private final Bson filter;
    private final DeleteOptions options;
    
    public DeleteOneModel(final Bson filter) {
        this(filter, new DeleteOptions());
    }
    
    public DeleteOneModel(final Bson filter, final DeleteOptions options) {
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
