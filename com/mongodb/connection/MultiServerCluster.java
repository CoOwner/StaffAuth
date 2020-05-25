package com.mongodb.connection;

import org.bson.types.*;
import com.mongodb.*;
import java.util.concurrent.*;
import com.mongodb.assertions.*;
import com.mongodb.event.*;
import java.util.*;
import com.mongodb.diagnostics.logging.*;

final class MultiServerCluster extends BaseCluster
{
    private static final Logger LOGGER;
    private ClusterType clusterType;
    private String replicaSetName;
    private ObjectId maxElectionId;
    private Integer maxSetVersion;
    private final ConcurrentMap<ServerAddress, ServerTuple> addressToServerTupleMap;
    
    public MultiServerCluster(final ClusterId clusterId, final ClusterSettings settings, final ClusterableServerFactory serverFactory) {
        super(clusterId, settings, serverFactory);
        this.addressToServerTupleMap = new ConcurrentHashMap<ServerAddress, ServerTuple>();
        Assertions.isTrue("connection mode is multiple", settings.getMode() == ClusterConnectionMode.MULTIPLE);
        this.clusterType = settings.getRequiredClusterType();
        this.replicaSetName = settings.getRequiredReplicaSetName();
        if (MultiServerCluster.LOGGER.isInfoEnabled()) {
            MultiServerCluster.LOGGER.info(String.format("Cluster created with settings %s", settings.getShortDescription()));
        }
        final ClusterDescription newDescription;
        synchronized (this) {
            for (final ServerAddress serverAddress : settings.getHosts()) {
                this.addServer(serverAddress);
            }
            newDescription = this.updateDescription();
        }
        this.fireChangeEvent(new ClusterDescriptionChangedEvent(clusterId, newDescription, new ClusterDescription(settings.getMode(), ClusterType.UNKNOWN, Collections.emptyList(), settings, serverFactory.getSettings())));
    }
    
    @Override
    protected void connect() {
        for (final ServerTuple cur : this.addressToServerTupleMap.values()) {
            cur.server.connect();
        }
    }
    
    @Override
    public void close() {
        synchronized (this) {
            if (!this.isClosed()) {
                for (final ServerTuple serverTuple : this.addressToServerTupleMap.values()) {
                    serverTuple.server.close();
                }
            }
            super.close();
        }
    }
    
    @Override
    protected ClusterableServer getServer(final ServerAddress serverAddress) {
        Assertions.isTrue("is open", !this.isClosed());
        final ServerTuple serverTuple = this.addressToServerTupleMap.get(serverAddress);
        if (serverTuple == null) {
            return null;
        }
        return serverTuple.server;
    }
    
    private void onChange(final ServerDescriptionChangedEvent event) {
        ClusterDescription oldClusterDescription = null;
        ClusterDescription newClusterDescription = null;
        boolean shouldUpdateDescription = true;
        synchronized (this) {
            if (this.isClosed()) {
                return;
            }
            final ServerDescription newDescription = event.getNewDescription();
            if (MultiServerCluster.LOGGER.isTraceEnabled()) {
                MultiServerCluster.LOGGER.trace(String.format("Handling description changed event for server %s with description %s", newDescription.getAddress(), newDescription));
            }
            final ServerTuple serverTuple = this.addressToServerTupleMap.get(newDescription.getAddress());
            if (serverTuple == null) {
                if (MultiServerCluster.LOGGER.isTraceEnabled()) {
                    MultiServerCluster.LOGGER.trace(String.format("Ignoring description changed event for removed server %s", newDescription.getAddress()));
                }
                return;
            }
            if (event.getNewDescription().isOk()) {
                if (this.clusterType == ClusterType.UNKNOWN && newDescription.getType() != ServerType.REPLICA_SET_GHOST) {
                    this.clusterType = newDescription.getClusterType();
                    if (MultiServerCluster.LOGGER.isInfoEnabled()) {
                        MultiServerCluster.LOGGER.info(String.format("Discovered cluster type of %s", this.clusterType));
                    }
                }
                switch (this.clusterType) {
                    case REPLICA_SET: {
                        shouldUpdateDescription = this.handleReplicaSetMemberChanged(newDescription);
                        break;
                    }
                    case SHARDED: {
                        shouldUpdateDescription = this.handleShardRouterChanged(newDescription);
                        break;
                    }
                    case STANDALONE: {
                        shouldUpdateDescription = this.handleStandAloneChanged(newDescription);
                        break;
                    }
                }
            }
            if (shouldUpdateDescription) {
                serverTuple.description = newDescription;
                oldClusterDescription = this.getCurrentDescription();
                newClusterDescription = this.updateDescription();
            }
        }
        if (shouldUpdateDescription) {
            this.fireChangeEvent(new ClusterDescriptionChangedEvent(this.getClusterId(), newClusterDescription, oldClusterDescription));
        }
    }
    
    private boolean handleReplicaSetMemberChanged(final ServerDescription newDescription) {
        if (!newDescription.isReplicaSetMember()) {
            MultiServerCluster.LOGGER.error(String.format("Expecting replica set member, but found a %s.  Removing %s from client view of cluster.", newDescription.getType(), newDescription.getAddress()));
            this.removeServer(newDescription.getAddress());
            return true;
        }
        if (newDescription.getType() == ServerType.REPLICA_SET_GHOST) {
            if (MultiServerCluster.LOGGER.isInfoEnabled()) {
                MultiServerCluster.LOGGER.info(String.format("Server %s does not appear to be a member of an initiated replica set.", newDescription.getAddress()));
            }
            return true;
        }
        if (this.replicaSetName == null) {
            this.replicaSetName = newDescription.getSetName();
        }
        if (!this.replicaSetName.equals(newDescription.getSetName())) {
            MultiServerCluster.LOGGER.error(String.format("Expecting replica set member from set '%s', but found one from set '%s'.  Removing %s from client view of cluster.", this.replicaSetName, newDescription.getSetName(), newDescription.getAddress()));
            this.removeServer(newDescription.getAddress());
            return true;
        }
        this.ensureServers(newDescription);
        if (newDescription.getCanonicalAddress() != null && !newDescription.getAddress().sameHost(newDescription.getCanonicalAddress())) {
            if (MultiServerCluster.LOGGER.isInfoEnabled()) {
                MultiServerCluster.LOGGER.info(String.format("Canonical address %s does not match server address.  Removing %s from client view of cluster", newDescription.getCanonicalAddress(), newDescription.getAddress()));
            }
            this.removeServer(newDescription.getAddress());
            return true;
        }
        if (newDescription.isPrimary()) {
            if (newDescription.getSetVersion() != null && newDescription.getElectionId() != null) {
                if (this.isStalePrimary(newDescription)) {
                    if (MultiServerCluster.LOGGER.isInfoEnabled()) {
                        MultiServerCluster.LOGGER.info(String.format("Invalidating potential primary %s whose (set version, election id) tuple of (%d, %s) is less than one already seen of (%d, %s)", newDescription.getAddress(), newDescription.getSetVersion(), newDescription.getElectionId(), this.maxSetVersion, this.maxElectionId));
                    }
                    this.addressToServerTupleMap.get(newDescription.getAddress()).server.invalidate();
                    return false;
                }
                if (!newDescription.getElectionId().equals(this.maxElectionId)) {
                    if (MultiServerCluster.LOGGER.isInfoEnabled()) {
                        MultiServerCluster.LOGGER.info(String.format("Setting max election id to %s from replica set primary %s", newDescription.getElectionId(), newDescription.getAddress()));
                    }
                    this.maxElectionId = newDescription.getElectionId();
                }
            }
            if (newDescription.getSetVersion() != null && (this.maxSetVersion == null || newDescription.getSetVersion().compareTo(this.maxSetVersion) > 0)) {
                if (MultiServerCluster.LOGGER.isInfoEnabled()) {
                    MultiServerCluster.LOGGER.info(String.format("Setting max set version to %d from replica set primary %s", newDescription.getSetVersion(), newDescription.getAddress()));
                }
                this.maxSetVersion = newDescription.getSetVersion();
            }
            if (this.isNotAlreadyPrimary(newDescription.getAddress())) {
                MultiServerCluster.LOGGER.info(String.format("Discovered replica set primary %s", newDescription.getAddress()));
            }
            this.invalidateOldPrimaries(newDescription.getAddress());
        }
        return true;
    }
    
    private boolean isStalePrimary(final ServerDescription newDescription) {
        return this.maxSetVersion != null && this.maxElectionId != null && (this.maxSetVersion.compareTo(newDescription.getSetVersion()) > 0 || (this.maxSetVersion.equals(newDescription.getSetVersion()) && this.maxElectionId.compareTo(newDescription.getElectionId()) > 0));
    }
    
    private boolean isNotAlreadyPrimary(final ServerAddress address) {
        final ServerTuple serverTuple = this.addressToServerTupleMap.get(address);
        return serverTuple == null || !serverTuple.description.isPrimary();
    }
    
    private boolean handleShardRouterChanged(final ServerDescription newDescription) {
        if (!newDescription.isShardRouter()) {
            MultiServerCluster.LOGGER.error(String.format("Expecting a %s, but found a %s.  Removing %s from client view of cluster.", ServerType.SHARD_ROUTER, newDescription.getType(), newDescription.getAddress()));
            this.removeServer(newDescription.getAddress());
        }
        return true;
    }
    
    private boolean handleStandAloneChanged(final ServerDescription newDescription) {
        if (this.getSettings().getHosts().size() > 1) {
            MultiServerCluster.LOGGER.error(String.format("Expecting a single %s, but found more than one.  Removing %s from client view of cluster.", ServerType.STANDALONE, newDescription.getAddress()));
            this.clusterType = ClusterType.UNKNOWN;
            this.removeServer(newDescription.getAddress());
        }
        return true;
    }
    
    private void addServer(final ServerAddress serverAddress) {
        if (!this.addressToServerTupleMap.containsKey(serverAddress)) {
            if (MultiServerCluster.LOGGER.isInfoEnabled()) {
                MultiServerCluster.LOGGER.info(String.format("Adding discovered server %s to client view of cluster", serverAddress));
            }
            final ClusterableServer server = this.createServer(serverAddress, new DefaultServerStateListener());
            this.addressToServerTupleMap.put(serverAddress, new ServerTuple(server, this.getConnectingServerDescription(serverAddress)));
        }
    }
    
    private void removeServer(final ServerAddress serverAddress) {
        final ServerTuple removed = this.addressToServerTupleMap.remove(serverAddress);
        if (removed != null) {
            removed.server.close();
        }
    }
    
    private void invalidateOldPrimaries(final ServerAddress newPrimary) {
        for (final ServerTuple serverTuple : this.addressToServerTupleMap.values()) {
            if (!serverTuple.description.getAddress().equals(newPrimary) && serverTuple.description.isPrimary()) {
                if (MultiServerCluster.LOGGER.isInfoEnabled()) {
                    MultiServerCluster.LOGGER.info(String.format("Rediscovering type of existing primary %s", serverTuple.description.getAddress()));
                }
                serverTuple.server.invalidate();
            }
        }
    }
    
    private ServerDescription getConnectingServerDescription(final ServerAddress serverAddress) {
        return ServerDescription.builder().state(ServerConnectionState.CONNECTING).address(serverAddress).build();
    }
    
    private ClusterDescription updateDescription() {
        final ClusterDescription newDescription = new ClusterDescription(ClusterConnectionMode.MULTIPLE, this.clusterType, this.getNewServerDescriptionList(), this.getSettings(), this.getServerFactory().getSettings());
        this.updateDescription(newDescription);
        return newDescription;
    }
    
    private List<ServerDescription> getNewServerDescriptionList() {
        final List<ServerDescription> serverDescriptions = new ArrayList<ServerDescription>();
        for (final ServerTuple cur : this.addressToServerTupleMap.values()) {
            serverDescriptions.add(cur.description);
        }
        return serverDescriptions;
    }
    
    private void ensureServers(final ServerDescription description) {
        if (description.isPrimary() || !this.hasPrimary()) {
            this.addNewHosts(description.getHosts());
            this.addNewHosts(description.getPassives());
            this.addNewHosts(description.getArbiters());
        }
        if (description.isPrimary()) {
            this.removeExtraHosts(description);
        }
    }
    
    private boolean hasPrimary() {
        for (final ServerTuple serverTuple : this.addressToServerTupleMap.values()) {
            if (serverTuple.description.isPrimary()) {
                return true;
            }
        }
        return false;
    }
    
    private void addNewHosts(final Set<String> hosts) {
        for (final String cur : hosts) {
            this.addServer(new ServerAddress(cur));
        }
    }
    
    private void removeExtraHosts(final ServerDescription serverDescription) {
        final Set<ServerAddress> allServerAddresses = this.getAllServerAddresses(serverDescription);
        for (final ServerTuple cur : this.addressToServerTupleMap.values()) {
            if (!allServerAddresses.contains(cur.description.getAddress())) {
                if (MultiServerCluster.LOGGER.isInfoEnabled()) {
                    MultiServerCluster.LOGGER.info(String.format("Server %s is no longer a member of the replica set.  Removing from client view of cluster.", cur.description.getAddress()));
                }
                this.removeServer(cur.description.getAddress());
            }
        }
    }
    
    private Set<ServerAddress> getAllServerAddresses(final ServerDescription serverDescription) {
        final Set<ServerAddress> retVal = new HashSet<ServerAddress>();
        this.addHostsToSet(serverDescription.getHosts(), retVal);
        this.addHostsToSet(serverDescription.getPassives(), retVal);
        this.addHostsToSet(serverDescription.getArbiters(), retVal);
        return retVal;
    }
    
    private void addHostsToSet(final Set<String> hosts, final Set<ServerAddress> retVal) {
        for (final String host : hosts) {
            retVal.add(new ServerAddress(host));
        }
    }
    
    static {
        LOGGER = Loggers.getLogger("cluster");
    }
    
    private static final class ServerTuple
    {
        private final ClusterableServer server;
        private ServerDescription description;
        
        private ServerTuple(final ClusterableServer server, final ServerDescription description) {
            this.server = server;
            this.description = description;
        }
    }
    
    private final class DefaultServerStateListener extends NoOpServerListener
    {
        @Override
        public void serverDescriptionChanged(final ServerDescriptionChangedEvent event) {
            MultiServerCluster.this.onChange(event);
        }
    }
}
