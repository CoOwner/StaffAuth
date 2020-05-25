package com.mongodb.client.model;

import org.bson.conversions.*;

public final class IndexOptionDefaults
{
    private Bson storageEngine;
    
    public Bson getStorageEngine() {
        return this.storageEngine;
    }
    
    public IndexOptionDefaults storageEngine(final Bson storageEngine) {
        this.storageEngine = storageEngine;
        return this;
    }
}
