package com.mongodb.connection;

import java.util.concurrent.atomic.*;
import com.mongodb.internal.connection.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import com.mongodb.async.*;
import com.mongodb.selector.*;
import com.mongodb.event.*;
import com.mongodb.diagnostics.logging.*;
import com.mongodb.*;
import java.util.*;

abstract class BaseCluster implements Cluster
{
    private static final Logger LOGGER;
    private final AtomicReference<CountDownLatch> phase;
    private final ClusterableServerFactory serverFactory;
    private final ThreadLocal<Random> random;
    private final ClusterId clusterId;
    private final ClusterSettings settings;
    private final ClusterListener clusterListener;
    private final Deque<ServerSelectionRequest> waitQueue;
    private final AtomicInteger waitQueueSize;
    private Thread waitQueueHandler;
    private volatile boolean isClosed;
    private volatile ClusterDescription description;
    
    public BaseCluster(final ClusterId clusterId, final ClusterSettings settings, final ClusterableServerFactory serverFactory) {
        this.phase = new AtomicReference<CountDownLatch>(new CountDownLatch(1));
        this.random = new ThreadLocal<Random>();
        this.waitQueue = new ConcurrentLinkedDeque<ServerSelectionRequest>();
        this.waitQueueSize = new AtomicInteger(0);
        this.clusterId = Assertions.notNull("clusterId", clusterId);
        this.settings = Assertions.notNull("settings", settings);
        this.serverFactory = Assertions.notNull("serverFactory", serverFactory);
        (this.clusterListener = (settings.getClusterListeners().isEmpty() ? new NoOpClusterListener() : new ClusterEventMulticaster(settings.getClusterListeners()))).clusterOpening(new ClusterOpeningEvent(clusterId));
    }
    
    @Override
    public Server selectServer(final ServerSelector serverSelector) {
        Assertions.isTrue("open", !this.isClosed());
        try {
            CountDownLatch currentPhase = this.phase.get();
            ClusterDescription curDescription = this.description;
            final ServerSelector compositeServerSelector = this.getCompositeServerSelector(serverSelector);
            Server server = this.selectRandomServer(compositeServerSelector, curDescription);
            boolean selectionFailureLogged = false;
            long curTimeNanos;
            final long startTimeNanos = curTimeNanos = System.nanoTime();
            final long maxWaitTimeNanos = this.getMaxWaitTimeNanos();
            while (true) {
                this.throwIfIncompatible(curDescription);
                if (server != null) {
                    return server;
                }
                if (curTimeNanos - startTimeNanos > maxWaitTimeNanos) {
                    throw this.createTimeoutException(serverSelector, curDescription);
                }
                if (!selectionFailureLogged) {
                    this.logServerSelectionFailure(serverSelector, curDescription);
                    selectionFailureLogged = true;
                }
                this.connect();
                currentPhase.await(Math.min(maxWaitTimeNanos - (curTimeNanos - startTimeNanos), this.getMinWaitTimeNanos()), TimeUnit.NANOSECONDS);
                curTimeNanos = System.nanoTime();
                currentPhase = this.phase.get();
                curDescription = this.description;
                server = this.selectRandomServer(compositeServerSelector, curDescription);
            }
        }
        catch (InterruptedException e) {
            throw new MongoInterruptedException(String.format("Interrupted while waiting for a server that matches %s", serverSelector), e);
        }
    }
    
    @Override
    public void selectServerAsync(final ServerSelector serverSelector, final SingleResultCallback<Server> callback) {
        Assertions.isTrue("open", !this.isClosed());
        if (BaseCluster.LOGGER.isTraceEnabled()) {
            BaseCluster.LOGGER.trace(String.format("Asynchronously selecting server with selector %s", serverSelector));
        }
        final ServerSelectionRequest request = new ServerSelectionRequest(serverSelector, this.getCompositeServerSelector(serverSelector), this.getMaxWaitTimeNanos(), callback);
        final CountDownLatch currentPhase = this.phase.get();
        final ClusterDescription currentDescription = this.description;
        if (!this.handleServerSelectionRequest(request, currentPhase, currentDescription)) {
            this.notifyWaitQueueHandler(request);
        }
    }
    
    @Override
    public ClusterDescription getDescription() {
        Assertions.isTrue("open", !this.isClosed());
        try {
            CountDownLatch currentPhase = this.phase.get();
            ClusterDescription curDescription = this.description;
            boolean selectionFailureLogged = false;
            long curTimeNanos;
            final long startTimeNanos = curTimeNanos = System.nanoTime();
            final long maxWaitTimeNanos = this.getMaxWaitTimeNanos();
            while (curDescription.getType() == ClusterType.UNKNOWN) {
                if (curTimeNanos - startTimeNanos > maxWaitTimeNanos) {
                    throw new MongoTimeoutException(String.format("Timed out after %d ms while waiting to connect. Client view of cluster state is %s", this.settings.getServerSelectionTimeout(TimeUnit.MILLISECONDS), curDescription.getShortDescription()));
                }
                if (!selectionFailureLogged) {
                    if (BaseCluster.LOGGER.isInfoEnabled()) {
                        if (this.settings.getServerSelectionTimeout(TimeUnit.MILLISECONDS) < 0L) {
                            BaseCluster.LOGGER.info(String.format("Cluster description not yet available. Waiting indefinitely.", new Object[0]));
                        }
                        else {
                            BaseCluster.LOGGER.info(String.format("Cluster description not yet available. Waiting for %d ms before timing out", this.settings.getServerSelectionTimeout(TimeUnit.MILLISECONDS)));
                        }
                    }
                    selectionFailureLogged = true;
                }
                this.connect();
                currentPhase.await(Math.min(maxWaitTimeNanos - (curTimeNanos - startTimeNanos), this.getMinWaitTimeNanos()), TimeUnit.NANOSECONDS);
                curTimeNanos = System.nanoTime();
                currentPhase = this.phase.get();
                curDescription = this.description;
            }
            return curDescription;
        }
        catch (InterruptedException e) {
            throw new MongoInterruptedException(String.format("Interrupted while waiting to connect", new Object[0]), e);
        }
    }
    
    protected ClusterId getClusterId() {
        return this.clusterId;
    }
    
    @Override
    public ClusterSettings getSettings() {
        return this.settings;
    }
    
    public ClusterableServerFactory getServerFactory() {
        return this.serverFactory;
    }
    
    protected abstract void connect();
    
    @Override
    public void close() {
        if (!this.isClosed()) {
            this.isClosed = true;
            this.phase.get().countDown();
            this.clusterListener.clusterClosed(new ClusterClosedEvent(this.clusterId));
            this.stopWaitQueueHandler();
        }
    }
    
    @Override
    public boolean isClosed() {
        return this.isClosed;
    }
    
    protected abstract ClusterableServer getServer(final ServerAddress p0);
    
    protected synchronized void updateDescription(final ClusterDescription newDescription) {
        if (BaseCluster.LOGGER.isDebugEnabled()) {
            BaseCluster.LOGGER.debug(String.format("Updating cluster description to  %s", newDescription.getShortDescription()));
        }
        this.description = newDescription;
        this.phase.getAndSet(new CountDownLatch(1)).countDown();
    }
    
    protected void fireChangeEvent(final ClusterDescriptionChangedEvent event) {
        this.clusterListener.clusterDescriptionChanged(event);
    }
    
    ClusterDescription getCurrentDescription() {
        return this.description;
    }
    
    private long getMaxWaitTimeNanos() {
        if (this.settings.getServerSelectionTimeout(TimeUnit.NANOSECONDS) < 0L) {
            return Long.MAX_VALUE;
        }
        return this.settings.getServerSelectionTimeout(TimeUnit.NANOSECONDS);
    }
    
    private long getMinWaitTimeNanos() {
        return this.serverFactory.getSettings().getMinHeartbeatFrequency(TimeUnit.NANOSECONDS);
    }
    
    private boolean handleServerSelectionRequest(final ServerSelectionRequest request, final CountDownLatch currentPhase, final ClusterDescription description) {
        try {
            if (currentPhase != request.phase) {
                final CountDownLatch prevPhase = request.phase;
                request.phase = currentPhase;
                if (!description.isCompatibleWithDriver()) {
                    if (BaseCluster.LOGGER.isTraceEnabled()) {
                        BaseCluster.LOGGER.trace(String.format("Asynchronously failed server selection due to driver incompatibility with server", new Object[0]));
                    }
                    request.onResult(null, this.createIncompatibleException(description));
                    return true;
                }
                final Server server = this.selectRandomServer(request.compositeSelector, description);
                if (server != null) {
                    if (BaseCluster.LOGGER.isTraceEnabled()) {
                        BaseCluster.LOGGER.trace(String.format("Asynchronously selected server %s", server.getDescription().getAddress()));
                    }
                    request.onResult(server, null);
                    return true;
                }
                if (prevPhase == null) {
                    this.logServerSelectionFailure(request.originalSelector, description);
                }
            }
            if (request.timedOut()) {
                if (BaseCluster.LOGGER.isTraceEnabled()) {
                    BaseCluster.LOGGER.trace(String.format("Asynchronously failed server selection after timeout", new Object[0]));
                }
                request.onResult(null, this.createTimeoutException(request.originalSelector, description));
                return true;
            }
            return false;
        }
        catch (Exception e) {
            request.onResult(null, e);
            return true;
        }
    }
    
    private void logServerSelectionFailure(final ServerSelector serverSelector, final ClusterDescription curDescription) {
        if (BaseCluster.LOGGER.isInfoEnabled()) {
            if (this.settings.getServerSelectionTimeout(TimeUnit.MILLISECONDS) < 0L) {
                BaseCluster.LOGGER.info(String.format("No server chosen by %s from cluster description %s. Waiting indefinitely.", serverSelector, curDescription));
            }
            else {
                BaseCluster.LOGGER.info(String.format("No server chosen by %s from cluster description %s. Waiting for %d ms before timing out", serverSelector, curDescription, this.settings.getServerSelectionTimeout(TimeUnit.MILLISECONDS)));
            }
        }
    }
    
    private Server selectRandomServer(final ServerSelector serverSelector, final ClusterDescription clusterDescription) {
        final List<ServerDescription> serverDescriptions = serverSelector.select(clusterDescription);
        if (!serverDescriptions.isEmpty()) {
            return this.getRandomServer(new ArrayList<ServerDescription>(serverDescriptions));
        }
        return null;
    }
    
    private ServerSelector getCompositeServerSelector(final ServerSelector serverSelector) {
        if (this.settings.getServerSelector() == null) {
            return serverSelector;
        }
        return new CompositeServerSelector(Arrays.asList(serverSelector, this.settings.getServerSelector()));
    }
    
    private ClusterableServer getRandomServer(final List<ServerDescription> serverDescriptions) {
        while (!serverDescriptions.isEmpty()) {
            final int serverPos = this.getRandom().nextInt(serverDescriptions.size());
            final ClusterableServer server = this.getServer(serverDescriptions.get(serverPos).getAddress());
            if (server != null) {
                return server;
            }
            serverDescriptions.remove(serverPos);
        }
        return null;
    }
    
    private Random getRandom() {
        Random result = this.random.get();
        if (result == null) {
            result = new Random();
            this.random.set(result);
        }
        return result;
    }
    
    protected ClusterableServer createServer(final ServerAddress serverAddress, final ServerListener serverListener) {
        final ClusterableServer server = this.serverFactory.create(serverAddress, serverListener);
        return server;
    }
    
    private void throwIfIncompatible(final ClusterDescription curDescription) {
        if (!curDescription.isCompatibleWithDriver()) {
            throw this.createIncompatibleException(curDescription);
        }
    }
    
    private MongoIncompatibleDriverException createIncompatibleException(final ClusterDescription curDescription) {
        return new MongoIncompatibleDriverException(String.format("This version of the driver is not compatible with one or more of the servers to which it is connected: %s", curDescription), curDescription);
    }
    
    private MongoTimeoutException createTimeoutException(final ServerSelector serverSelector, final ClusterDescription curDescription) {
        return new MongoTimeoutException(String.format("Timed out after %d ms while waiting for a server that matches %s. Client view of cluster state is %s", this.settings.getServerSelectionTimeout(TimeUnit.MILLISECONDS), serverSelector, curDescription.getShortDescription()));
    }
    
    private MongoWaitQueueFullException createWaitQueueFullException() {
        return new MongoWaitQueueFullException(String.format("Too many operations are already waiting for a server. Max number of operations (maxWaitQueueSize) of %d has been exceeded.", this.settings.getMaxWaitQueueSize()));
    }
    
    private synchronized void notifyWaitQueueHandler(final ServerSelectionRequest request) {
        if (this.isClosed) {
            return;
        }
        if (this.waitQueueSize.incrementAndGet() > this.settings.getMaxWaitQueueSize()) {
            this.waitQueueSize.decrementAndGet();
            request.onResult(null, this.createWaitQueueFullException());
        }
        else {
            this.waitQueue.add(request);
            if (this.waitQueueHandler == null) {
                (this.waitQueueHandler = new Thread(new WaitQueueHandler(), "cluster-" + this.clusterId.getValue())).setDaemon(true);
                this.waitQueueHandler.start();
            }
        }
    }
    
    private synchronized void stopWaitQueueHandler() {
        if (this.waitQueueHandler != null) {
            this.waitQueueHandler.interrupt();
        }
    }
    
    static {
        LOGGER = Loggers.getLogger("cluster");
    }
    
    private static final class ServerSelectionRequest
    {
        private final ServerSelector originalSelector;
        private final ServerSelector compositeSelector;
        private final long maxWaitTimeNanos;
        private final SingleResultCallback<Server> callback;
        private final long startTimeNanos;
        private CountDownLatch phase;
        
        ServerSelectionRequest(final ServerSelector serverSelector, final ServerSelector compositeSelector, final long maxWaitTimeNanos, final SingleResultCallback<Server> callback) {
            this.startTimeNanos = System.nanoTime();
            this.originalSelector = serverSelector;
            this.compositeSelector = compositeSelector;
            this.maxWaitTimeNanos = maxWaitTimeNanos;
            this.callback = callback;
        }
        
        void onResult(final Server server, final Throwable t) {
            try {
                this.callback.onResult(server, t);
            }
            catch (Throwable t2) {}
        }
        
        boolean timedOut() {
            return System.nanoTime() - this.startTimeNanos > this.maxWaitTimeNanos;
        }
        
        long getRemainingTime() {
            return this.startTimeNanos + this.maxWaitTimeNanos - System.nanoTime();
        }
    }
    
    private final class WaitQueueHandler implements Runnable
    {
        @Override
        public void run() {
            while (!BaseCluster.this.isClosed) {
                final CountDownLatch currentPhase = BaseCluster.this.phase.get();
                final ClusterDescription curDescription = BaseCluster.this.description;
                long waitTimeNanos = Long.MAX_VALUE;
                final Iterator<ServerSelectionRequest> iter = BaseCluster.this.waitQueue.iterator();
                while (iter.hasNext()) {
                    final ServerSelectionRequest nextRequest = iter.next();
                    if (BaseCluster.this.handleServerSelectionRequest(nextRequest, currentPhase, curDescription)) {
                        iter.remove();
                        BaseCluster.this.waitQueueSize.decrementAndGet();
                    }
                    else {
                        waitTimeNanos = Math.min(nextRequest.getRemainingTime(), Math.min(BaseCluster.this.getMinWaitTimeNanos(), waitTimeNanos));
                    }
                }
                if (waitTimeNanos < Long.MAX_VALUE) {
                    BaseCluster.this.connect();
                }
                try {
                    currentPhase.await(waitTimeNanos, TimeUnit.NANOSECONDS);
                }
                catch (InterruptedException ex) {}
            }
            final Iterator<ServerSelectionRequest> iter2 = BaseCluster.this.waitQueue.iterator();
            while (iter2.hasNext()) {
                iter2.next().onResult(null, new MongoClientException("Shutdown in progress"));
                iter2.remove();
            }
        }
    }
}
