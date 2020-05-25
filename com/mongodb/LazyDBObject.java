package com.mongodb;

import com.mongodb.annotations.*;
import org.bson.*;

@Immutable
public class LazyDBObject extends LazyBSONObject implements DBObject
{
    private boolean isPartial;
    
    public LazyDBObject(final byte[] bytes, final LazyBSONCallback callback) {
        super(bytes, callback);
        this.isPartial = false;
    }
    
    public LazyDBObject(final byte[] bytes, final int offset, final LazyBSONCallback callback) {
        super(bytes, offset, callback);
        this.isPartial = false;
    }
    
    @Override
    public void markAsPartialObject() {
        this.isPartial = true;
    }
    
    @Override
    public boolean isPartialObject() {
        return this.isPartial;
    }
}
