package com.mongodb.binding;

import com.mongodb.*;
import com.mongodb.connection.*;
import com.mongodb.assertions.*;
import com.mongodb.async.*;

public class AsyncSingleConnectionReadBinding extends AbstractReferenceCounted implements AsyncReadBinding
{
    private final ReadPreference readPreference;
    private final ServerDescription serverDescription;
    private final AsyncConnection connection;
    
    public AsyncSingleConnectionReadBinding(final ReadPreference readPreference, final ServerDescription serverDescription, final AsyncConnection connection) {
        this.readPreference = Assertions.notNull("readPreference", readPreference);
        this.serverDescription = Assertions.notNull("serverDescription", serverDescription);
        this.connection = Assertions.notNull("connection", connection).retain();
    }
    
    @Override
    public ReadPreference getReadPreference() {
        return this.readPreference;
    }
    
    @Override
    public void getReadConnectionSource(final SingleResultCallback<AsyncConnectionSource> callback) {
        callback.onResult(new AsyncSingleConnectionSource(), null);
    }
    
    @Override
    public AsyncReadBinding retain() {
        super.retain();
        return this;
    }
    
    @Override
    public void release() {
        super.release();
        if (this.getCount() == 0) {
            this.connection.release();
        }
    }
    
    private class AsyncSingleConnectionSource extends AbstractReferenceCounted implements AsyncConnectionSource
    {
        public AsyncSingleConnectionSource() {
            AsyncSingleConnectionReadBinding.this.retain();
        }
        
        @Override
        public ServerDescription getServerDescription() {
            return AsyncSingleConnectionReadBinding.this.serverDescription;
        }
        
        @Override
        public void getConnection(final SingleResultCallback<AsyncConnection> callback) {
            callback.onResult(AsyncSingleConnectionReadBinding.this.connection.retain(), null);
        }
        
        @Override
        public AsyncConnectionSource retain() {
            super.retain();
            return this;
        }
        
        @Override
        public void release() {
            super.release();
            if (super.getCount() == 0) {
                AsyncSingleConnectionReadBinding.this.release();
            }
        }
    }
}
