package com.mongodb.event;

public abstract class ClusterListenerAdapter implements ClusterListener
{
    @Override
    public void clusterOpening(final ClusterOpeningEvent event) {
    }
    
    @Override
    public void clusterClosed(final ClusterClosedEvent event) {
    }
    
    @Override
    public void clusterDescriptionChanged(final ClusterDescriptionChangedEvent event) {
    }
}
