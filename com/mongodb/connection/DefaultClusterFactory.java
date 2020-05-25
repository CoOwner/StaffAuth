package com.mongodb.connection;

import java.util.*;
import com.mongodb.*;
import com.mongodb.event.*;
import com.mongodb.client.*;

public final class DefaultClusterFactory implements ClusterFactory
{
    @Override
    public Cluster create(final ClusterSettings settings, final ServerSettings serverSettings, final ConnectionPoolSettings connectionPoolSettings, final StreamFactory streamFactory, final StreamFactory heartbeatStreamFactory, final List<MongoCredential> credentialList, final ClusterListener clusterListener, final ConnectionPoolListener connectionPoolListener, final ConnectionListener connectionListener) {
        return this.create(settings, serverSettings, connectionPoolSettings, streamFactory, heartbeatStreamFactory, credentialList, clusterListener, connectionPoolListener, connectionListener, null);
    }
    
    public Cluster create(final ClusterSettings settings, final ServerSettings serverSettings, final ConnectionPoolSettings connectionPoolSettings, final StreamFactory streamFactory, final StreamFactory heartbeatStreamFactory, final List<MongoCredential> credentialList, final ClusterListener clusterListener, final ConnectionPoolListener connectionPoolListener, final ConnectionListener connectionListener, final CommandListener commandListener) {
        return this.create(settings, serverSettings, connectionPoolSettings, streamFactory, heartbeatStreamFactory, credentialList, clusterListener, connectionPoolListener, connectionListener, commandListener, null, null);
    }
    
    public Cluster create(final ClusterSettings settings, final ServerSettings serverSettings, final ConnectionPoolSettings connectionPoolSettings, final StreamFactory streamFactory, final StreamFactory heartbeatStreamFactory, final List<MongoCredential> credentialList, final ClusterListener clusterListener, final ConnectionPoolListener connectionPoolListener, final ConnectionListener connectionListener, final CommandListener commandListener, final String applicationName, final MongoDriverInformation mongoDriverInformation) {
        if (clusterListener != null) {
            throw new IllegalArgumentException("Add cluster listener to ClusterSettings");
        }
        final ClusterId clusterId = new ClusterId(settings.getDescription());
        final ClusterableServerFactory serverFactory = new DefaultClusterableServerFactory(clusterId, settings, serverSettings, connectionPoolSettings, streamFactory, heartbeatStreamFactory, credentialList, (connectionListener != null) ? connectionListener : new NoOpConnectionListener(), (connectionPoolListener != null) ? connectionPoolListener : new NoOpConnectionPoolListener(), commandListener, applicationName, (mongoDriverInformation != null) ? mongoDriverInformation : MongoDriverInformation.builder().build());
        if (settings.getMode() == ClusterConnectionMode.SINGLE) {
            return new SingleServerCluster(clusterId, settings, serverFactory);
        }
        if (settings.getMode() == ClusterConnectionMode.MULTIPLE) {
            return new MultiServerCluster(clusterId, settings, serverFactory);
        }
        throw new UnsupportedOperationException("Unsupported cluster mode: " + settings.getMode());
    }
}
