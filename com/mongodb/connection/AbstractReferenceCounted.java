package com.mongodb.connection;

import com.mongodb.binding.*;
import java.util.concurrent.atomic.*;

abstract class AbstractReferenceCounted implements ReferenceCounted
{
    private final AtomicInteger referenceCount;
    
    AbstractReferenceCounted() {
        this.referenceCount = new AtomicInteger(1);
    }
    
    @Override
    public int getCount() {
        return this.referenceCount.get();
    }
    
    @Override
    public ReferenceCounted retain() {
        if (this.referenceCount.incrementAndGet() == 1) {
            throw new IllegalStateException("Attempted to increment the reference count when it is already 0");
        }
        return this;
    }
    
    @Override
    public void release() {
        if (this.referenceCount.decrementAndGet() < 0) {
            throw new IllegalStateException("Attempted to decrement the reference count below 0");
        }
    }
}
