package com.mongodb.event;

import com.mongodb.connection.*;
import com.mongodb.assertions.*;

public final class ClusterClosedEvent
{
    private final ClusterId clusterId;
    
    public ClusterClosedEvent(final ClusterId clusterId) {
        this.clusterId = Assertions.notNull("clusterId", clusterId);
    }
    
    public ClusterId getClusterId() {
        return this.clusterId;
    }
    
    @Override
    public String toString() {
        return "ClusterClosedEvent{clusterId=" + this.clusterId + '}';
    }
}
