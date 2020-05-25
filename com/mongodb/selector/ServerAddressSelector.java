package com.mongodb.selector;

import com.mongodb.*;
import com.mongodb.assertions.*;
import com.mongodb.connection.*;
import java.util.*;

public class ServerAddressSelector implements ServerSelector
{
    private final ServerAddress serverAddress;
    
    public ServerAddressSelector(final ServerAddress serverAddress) {
        this.serverAddress = Assertions.notNull("serverAddress", serverAddress);
    }
    
    public ServerAddress getServerAddress() {
        return this.serverAddress;
    }
    
    @Override
    public List<ServerDescription> select(final ClusterDescription clusterDescription) {
        if (clusterDescription.getByServerAddress(this.serverAddress) != null) {
            return Arrays.asList(clusterDescription.getByServerAddress(this.serverAddress));
        }
        return Collections.emptyList();
    }
    
    @Override
    public String toString() {
        return "ServerAddressSelector{serverAddress=" + this.serverAddress + '}';
    }
}
