package com.mongodb.binding;

import com.mongodb.*;
import com.mongodb.assertions.*;
import com.mongodb.selector.*;
import com.mongodb.connection.*;

public class SingleServerBinding extends AbstractReferenceCounted implements ReadWriteBinding
{
    private final Cluster cluster;
    private final ServerAddress serverAddress;
    private final ReadPreference readPreference;
    
    public SingleServerBinding(final Cluster cluster, final ServerAddress serverAddress) {
        this(cluster, serverAddress, ReadPreference.primary());
    }
    
    public SingleServerBinding(final Cluster cluster, final ServerAddress serverAddress, final ReadPreference readPreference) {
        this.cluster = Assertions.notNull("cluster", cluster);
        this.serverAddress = Assertions.notNull("serverAddress", serverAddress);
        this.readPreference = Assertions.notNull("readPreference", readPreference);
    }
    
    @Override
    public ConnectionSource getWriteConnectionSource() {
        return new SingleServerBindingConnectionSource();
    }
    
    @Override
    public ReadPreference getReadPreference() {
        return this.readPreference;
    }
    
    @Override
    public ConnectionSource getReadConnectionSource() {
        return new SingleServerBindingConnectionSource();
    }
    
    @Override
    public SingleServerBinding retain() {
        super.retain();
        return this;
    }
    
    private final class SingleServerBindingConnectionSource extends AbstractReferenceCounted implements ConnectionSource
    {
        private final Server server;
        
        private SingleServerBindingConnectionSource() {
            SingleServerBinding.this.retain();
            this.server = SingleServerBinding.this.cluster.selectServer(new ServerAddressSelector(SingleServerBinding.this.serverAddress));
        }
        
        @Override
        public ServerDescription getServerDescription() {
            return this.server.getDescription();
        }
        
        @Override
        public Connection getConnection() {
            return SingleServerBinding.this.cluster.selectServer(new ServerAddressSelector(SingleServerBinding.this.serverAddress)).getConnection();
        }
        
        @Override
        public ConnectionSource retain() {
            super.retain();
            SingleServerBinding.this.retain();
            return this;
        }
        
        @Override
        public void release() {
            SingleServerBinding.this.release();
        }
    }
}
