package com.mongodb.client.model;

import org.bson.conversions.*;
import com.mongodb.assertions.*;

public class IndexModel
{
    private final Bson keys;
    private final IndexOptions options;
    
    public IndexModel(final Bson keys) {
        this(keys, new IndexOptions());
    }
    
    public IndexModel(final Bson keys, final IndexOptions options) {
        this.keys = Assertions.notNull("keys", keys);
        this.options = Assertions.notNull("options", options);
    }
    
    public Bson getKeys() {
        return this.keys;
    }
    
    public IndexOptions getOptions() {
        return this.options;
    }
}
