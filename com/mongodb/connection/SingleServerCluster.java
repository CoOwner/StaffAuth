package com.mongodb.connection;

import com.mongodb.assertions.*;
import com.mongodb.*;
import java.util.*;
import com.mongodb.event.*;
import com.mongodb.diagnostics.logging.*;

final class SingleServerCluster extends BaseCluster
{
    private static final Logger LOGGER;
    private final ClusterableServer server;
    
    public SingleServerCluster(final ClusterId clusterId, final ClusterSettings settings, final ClusterableServerFactory serverFactory) {
        super(clusterId, settings, serverFactory);
        Assertions.isTrue("one server in a direct cluster", settings.getHosts().size() == 1);
        Assertions.isTrue("connection mode is single", settings.getMode() == ClusterConnectionMode.SINGLE);
        if (SingleServerCluster.LOGGER.isInfoEnabled()) {
            SingleServerCluster.LOGGER.info(String.format("Cluster created with settings %s", settings.getShortDescription()));
        }
        synchronized (this) {
            this.server = this.createServer(settings.getHosts().get(0), new ServerListener() {
                @Override
                public void serverOpening(final ServerOpeningEvent event) {
                }
                
                @Override
                public void serverClosed(final ServerClosedEvent event) {
                }
                
                @Override
                public void serverDescriptionChanged(final ServerDescriptionChangedEvent event) {
                    ServerDescription descriptionToPublish = event.getNewDescription();
                    if (event.getNewDescription().isOk()) {
                        if (SingleServerCluster.this.getSettings().getRequiredClusterType() != ClusterType.UNKNOWN && SingleServerCluster.this.getSettings().getRequiredClusterType() != event.getNewDescription().getClusterType()) {
                            descriptionToPublish = null;
                        }
                        else if (SingleServerCluster.this.getSettings().getRequiredClusterType() == ClusterType.REPLICA_SET && SingleServerCluster.this.getSettings().getRequiredReplicaSetName() != null && !SingleServerCluster.this.getSettings().getRequiredReplicaSetName().equals(event.getNewDescription().getSetName())) {
                            descriptionToPublish = null;
                        }
                    }
                    SingleServerCluster.this.publishDescription(descriptionToPublish);
                }
            });
            this.publishDescription(this.server.getDescription());
        }
    }
    
    @Override
    protected void connect() {
        this.server.connect();
    }
    
    private void publishDescription(final ServerDescription serverDescription) {
        ClusterType clusterType = this.getSettings().getRequiredClusterType();
        if (clusterType == ClusterType.UNKNOWN && serverDescription != null) {
            clusterType = serverDescription.getClusterType();
        }
        final ClusterDescription oldDescription = this.getCurrentDescription();
        final ClusterDescription description = new ClusterDescription(ClusterConnectionMode.SINGLE, clusterType, (serverDescription == null) ? Collections.emptyList() : Arrays.asList(serverDescription), this.getSettings(), this.getServerFactory().getSettings());
        this.updateDescription(description);
        this.fireChangeEvent(new ClusterDescriptionChangedEvent(this.getClusterId(), description, (oldDescription == null) ? this.getInitialDescription() : oldDescription));
    }
    
    private ClusterDescription getInitialDescription() {
        return new ClusterDescription(this.getSettings().getMode(), this.getSettings().getRequiredClusterType(), Collections.emptyList(), this.getSettings(), this.getServerFactory().getSettings());
    }
    
    @Override
    protected ClusterableServer getServer(final ServerAddress serverAddress) {
        Assertions.isTrue("open", !this.isClosed());
        return this.server;
    }
    
    @Override
    public void close() {
        if (!this.isClosed()) {
            this.server.close();
            super.close();
        }
    }
    
    static {
        LOGGER = Loggers.getLogger("cluster");
    }
}
