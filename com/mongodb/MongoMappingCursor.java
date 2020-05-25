package com.mongodb;

import com.mongodb.client.*;
import com.mongodb.assertions.*;

class MongoMappingCursor<T, U> implements MongoCursor<U>
{
    private final MongoCursor<T> proxied;
    private final Function<T, U> mapper;
    
    public MongoMappingCursor(final MongoCursor<T> proxied, final Function<T, U> mapper) {
        this.proxied = Assertions.notNull("proxied", proxied);
        this.mapper = Assertions.notNull("mapper", mapper);
    }
    
    @Override
    public void close() {
        this.proxied.close();
    }
    
    @Override
    public boolean hasNext() {
        return this.proxied.hasNext();
    }
    
    @Override
    public U next() {
        return this.mapper.apply(this.proxied.next());
    }
    
    @Override
    public U tryNext() {
        final T proxiedNext = this.proxied.tryNext();
        if (proxiedNext == null) {
            return null;
        }
        return this.mapper.apply(proxiedNext);
    }
    
    @Override
    public void remove() {
        this.proxied.remove();
    }
    
    @Override
    public ServerCursor getServerCursor() {
        return this.proxied.getServerCursor();
    }
    
    @Override
    public ServerAddress getServerAddress() {
        return this.proxied.getServerAddress();
    }
}
