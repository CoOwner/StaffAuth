package com.mongodb.binding;

import com.mongodb.*;
import com.mongodb.connection.*;
import com.mongodb.assertions.*;

public class SingleConnectionReadBinding extends AbstractReferenceCounted implements ReadBinding
{
    private final ReadPreference readPreference;
    private final ServerDescription serverDescription;
    private final Connection connection;
    
    public SingleConnectionReadBinding(final ReadPreference readPreference, final ServerDescription serverDescription, final Connection connection) {
        this.readPreference = Assertions.notNull("readPreference", readPreference);
        this.serverDescription = Assertions.notNull("serverDescription", serverDescription);
        this.connection = Assertions.notNull("connection", connection).retain();
    }
    
    @Override
    public ReadPreference getReadPreference() {
        return this.readPreference;
    }
    
    @Override
    public ConnectionSource getReadConnectionSource() {
        return new SingleConnectionSource();
    }
    
    @Override
    public ReadBinding retain() {
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
    
    private class SingleConnectionSource extends AbstractReferenceCounted implements ConnectionSource
    {
        public SingleConnectionSource() {
            SingleConnectionReadBinding.this.retain();
        }
        
        @Override
        public ServerDescription getServerDescription() {
            return SingleConnectionReadBinding.this.serverDescription;
        }
        
        @Override
        public Connection getConnection() {
            return SingleConnectionReadBinding.this.connection.retain();
        }
        
        @Override
        public ConnectionSource retain() {
            super.retain();
            return this;
        }
        
        @Override
        public void release() {
            super.release();
            if (super.getCount() == 0) {
                SingleConnectionReadBinding.this.release();
            }
        }
    }
}
