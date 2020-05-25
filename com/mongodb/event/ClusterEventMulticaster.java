package com.mongodb.event;

import com.mongodb.assertions.*;
import java.util.*;
import com.mongodb.diagnostics.logging.*;

public final class ClusterEventMulticaster implements ClusterListener
{
    private static final Logger LOGGER;
    private final List<ClusterListener> clusterListeners;
    
    public ClusterEventMulticaster(final List<ClusterListener> clusterListeners) {
        Assertions.notNull("clusterListeners", clusterListeners);
        Assertions.isTrue("All ClusterListener instances are non-null", !clusterListeners.contains(null));
        this.clusterListeners = new ArrayList<ClusterListener>(clusterListeners);
    }
    
    public List<ClusterListener> getClusterListeners() {
        return Collections.unmodifiableList((List<? extends ClusterListener>)this.clusterListeners);
    }
    
    @Override
    public void clusterOpening(final ClusterOpeningEvent event) {
        for (final ClusterListener cur : this.clusterListeners) {
            try {
                cur.clusterOpening(event);
            }
            catch (Exception e) {
                if (!ClusterEventMulticaster.LOGGER.isWarnEnabled()) {
                    continue;
                }
                ClusterEventMulticaster.LOGGER.warn(String.format("Exception thrown raising cluster opening event to listener %s", cur), e);
            }
        }
    }
    
    @Override
    public void clusterClosed(final ClusterClosedEvent event) {
        for (final ClusterListener cur : this.clusterListeners) {
            try {
                cur.clusterClosed(event);
            }
            catch (Exception e) {
                if (!ClusterEventMulticaster.LOGGER.isWarnEnabled()) {
                    continue;
                }
                ClusterEventMulticaster.LOGGER.warn(String.format("Exception thrown raising cluster closed event to listener %s", cur), e);
            }
        }
    }
    
    @Override
    public void clusterDescriptionChanged(final ClusterDescriptionChangedEvent event) {
        for (final ClusterListener cur : this.clusterListeners) {
            try {
                cur.clusterDescriptionChanged(event);
            }
            catch (Exception e) {
                if (!ClusterEventMulticaster.LOGGER.isWarnEnabled()) {
                    continue;
                }
                ClusterEventMulticaster.LOGGER.warn(String.format("Exception thrown raising cluster description changed event to listener %s", cur), e);
            }
        }
    }
    
    static {
        LOGGER = Loggers.getLogger("cluster.event");
    }
}
