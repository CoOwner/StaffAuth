package com.mongodb.connection;

import com.mongodb.annotations.*;
import com.mongodb.*;
import com.mongodb.assertions.*;

@Immutable
public final class ServerId
{
    private final ClusterId clusterId;
    private final ServerAddress address;
    
    public ServerId(final ClusterId clusterId, final ServerAddress address) {
        this.clusterId = Assertions.notNull("clusterId", clusterId);
        this.address = Assertions.notNull("address", address);
    }
    
    public ClusterId getClusterId() {
        return this.clusterId;
    }
    
    public ServerAddress getAddress() {
        return this.address;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ServerId serverId = (ServerId)o;
        return this.address.equals(serverId.address) && this.clusterId.equals(serverId.clusterId);
    }
    
    @Override
    public int hashCode() {
        int result = this.clusterId.hashCode();
        result = 31 * result + this.address.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "ServerId{clusterId=" + this.clusterId + ", address=" + this.address + '}';
    }
}
