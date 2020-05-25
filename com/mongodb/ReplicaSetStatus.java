package com.mongodb;

import java.util.*;
import com.mongodb.connection.*;

public class ReplicaSetStatus
{
    private final Cluster cluster;
    
    ReplicaSetStatus(final Cluster cluster) {
        this.cluster = cluster;
    }
    
    public String getName() {
        final List<ServerDescription> any = this.getClusterDescription().getAnyPrimaryOrSecondary();
        return any.isEmpty() ? null : any.get(0).getSetName();
    }
    
    public ServerAddress getMaster() {
        final List<ServerDescription> primaries = this.getClusterDescription().getPrimaries();
        return primaries.isEmpty() ? null : primaries.get(0).getAddress();
    }
    
    public boolean isMaster(final ServerAddress serverAddress) {
        return this.getMaster().equals(serverAddress);
    }
    
    public int getMaxBsonObjectSize() {
        final List<ServerDescription> primaries = this.getClusterDescription().getPrimaries();
        return primaries.isEmpty() ? ServerDescription.getDefaultMaxDocumentSize() : primaries.get(0).getMaxDocumentSize();
    }
    
    private ClusterDescription getClusterDescription() {
        return this.cluster.getDescription();
    }
    
    @Override
    public String toString() {
        return "ReplicaSetStatus{name=" + this.getName() + ", cluster=" + this.getClusterDescription() + '}';
    }
}
