package com.mongodb.event;

import com.mongodb.connection.*;
import com.mongodb.assertions.*;

public final class ClusterDescriptionChangedEvent
{
    private final ClusterId clusterId;
    private final ClusterDescription newDescription;
    private final ClusterDescription previousDescription;
    
    public ClusterDescriptionChangedEvent(final ClusterId clusterId, final ClusterDescription newDescription, final ClusterDescription previousDescription) {
        this.clusterId = Assertions.notNull("clusterId", clusterId);
        this.newDescription = Assertions.notNull("newDescription", newDescription);
        this.previousDescription = Assertions.notNull("previousDescription", previousDescription);
    }
    
    public ClusterId getClusterId() {
        return this.clusterId;
    }
    
    public ClusterDescription getNewDescription() {
        return this.newDescription;
    }
    
    public ClusterDescription getPreviousDescription() {
        return this.previousDescription;
    }
    
    @Override
    public String toString() {
        return "ClusterDescriptionChangedEvent{clusterId=" + this.clusterId + ", newDescription=" + this.newDescription + ", previousDescription=" + this.previousDescription + '}';
    }
}
