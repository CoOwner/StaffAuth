package com.mongodb.event;

import com.mongodb.connection.*;
import com.mongodb.assertions.*;

public final class ClusterOpeningEvent
{
    private final ClusterId clusterId;
    
    public ClusterOpeningEvent(final ClusterId clusterId) {
        this.clusterId = Assertions.notNull("clusterId", clusterId);
    }
    
    public ClusterId getClusterId() {
        return this.clusterId;
    }
    
    @Override
    public String toString() {
        return "ClusterOpeningEvent{clusterId=" + this.clusterId + '}';
    }
}
