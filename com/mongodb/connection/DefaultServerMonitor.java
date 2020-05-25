package com.mongodb.connection;

import com.mongodb.annotations.*;
import java.util.concurrent.locks.*;
import com.mongodb.diagnostics.logging.*;
import com.mongodb.*;
import org.bson.*;
import com.mongodb.event.*;
import java.util.concurrent.*;

@ThreadSafe
class DefaultServerMonitor implements ServerMonitor
{
    private static final Logger LOGGER;
    private final ServerId serverId;
    private final ServerMonitorListener serverMonitorListener;
    private final ChangeListener<ServerDescription> serverStateListener;
    private final InternalConnectionFactory internalConnectionFactory;
    private final ConnectionPool connectionPool;
    private final ServerSettings settings;
    private final ServerMonitorRunnable monitor;
    private final Thread monitorThread;
    private final Lock lock;
    private final Condition condition;
    private volatile boolean isClosed;
    
    DefaultServerMonitor(final ServerId serverId, final ServerSettings settings, final ChangeListener<ServerDescription> serverStateListener, final InternalConnectionFactory internalConnectionFactory, final ConnectionPool connectionPool) {
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
        this.settings = settings;
        this.serverId = serverId;
        this.serverMonitorListener = (settings.getServerMonitorListeners().isEmpty() ? new NoOpServerMonitorListener() : new ServerMonitorEventMulticaster(settings.getServerMonitorListeners()));
        this.serverStateListener = serverStateListener;
        this.internalConnectionFactory = internalConnectionFactory;
        this.connectionPool = connectionPool;
        this.monitor = new ServerMonitorRunnable();
        (this.monitorThread = new Thread(this.monitor, "cluster-" + this.serverId.getClusterId() + "-" + this.serverId.getAddress())).setDaemon(true);
        this.isClosed = false;
    }
    
    @Override
    public void start() {
        this.monitorThread.start();
    }
    
    @Override
    public void connect() {
        this.lock.lock();
        try {
            this.condition.signal();
        }
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public void close() {
        this.isClosed = true;
        this.monitorThread.interrupt();
    }
    
    static boolean shouldLogStageChange(final ServerDescription previous, final ServerDescription current) {
        if (previous.isOk() != current.isOk()) {
            return true;
        }
        if (!previous.getAddress().equals(current.getAddress())) {
            return true;
        }
        Label_0062: {
            if (previous.getCanonicalAddress() != null) {
                if (previous.getCanonicalAddress().equals(current.getCanonicalAddress())) {
                    break Label_0062;
                }
            }
            else if (current.getCanonicalAddress() == null) {
                break Label_0062;
            }
            return true;
        }
        if (!previous.getHosts().equals(current.getHosts())) {
            return true;
        }
        if (!previous.getArbiters().equals(current.getArbiters())) {
            return true;
        }
        if (!previous.getPassives().equals(current.getPassives())) {
            return true;
        }
        Label_0149: {
            if (previous.getPrimary() != null) {
                if (previous.getPrimary().equals(current.getPrimary())) {
                    break Label_0149;
                }
            }
            else if (current.getPrimary() == null) {
                break Label_0149;
            }
            return true;
        }
        Label_0182: {
            if (previous.getSetName() != null) {
                if (previous.getSetName().equals(current.getSetName())) {
                    break Label_0182;
                }
            }
            else if (current.getSetName() == null) {
                break Label_0182;
            }
            return true;
        }
        if (previous.getState() != current.getState()) {
            return true;
        }
        if (!previous.getTagSet().equals(current.getTagSet())) {
            return true;
        }
        if (previous.getType() != current.getType()) {
            return true;
        }
        if (!previous.getVersion().equals(current.getVersion())) {
            return true;
        }
        Label_0273: {
            if (previous.getElectionId() != null) {
                if (previous.getElectionId().equals(current.getElectionId())) {
                    break Label_0273;
                }
            }
            else if (current.getElectionId() == null) {
                break Label_0273;
            }
            return true;
        }
        Label_0306: {
            if (previous.getSetVersion() != null) {
                if (previous.getSetVersion().equals(current.getSetVersion())) {
                    break Label_0306;
                }
            }
            else if (current.getSetVersion() == null) {
                break Label_0306;
            }
            return true;
        }
        final Class<?> thisExceptionClass = (previous.getException() != null) ? previous.getException().getClass() : null;
        final Class<?> thatExceptionClass = (current.getException() != null) ? current.getException().getClass() : null;
        Label_0365: {
            if (thisExceptionClass != null) {
                if (thisExceptionClass.equals(thatExceptionClass)) {
                    break Label_0365;
                }
            }
            else if (thatExceptionClass == null) {
                break Label_0365;
            }
            return true;
        }
        final String thisExceptionMessage = (previous.getException() != null) ? previous.getException().getMessage() : null;
        final String thatExceptionMessage = (current.getException() != null) ? current.getException().getMessage() : null;
        if (thisExceptionMessage != null) {
            if (thisExceptionMessage.equals(thatExceptionMessage)) {
                return false;
            }
        }
        else if (thatExceptionMessage == null) {
            return false;
        }
        return true;
    }
    
    static {
        LOGGER = Loggers.getLogger("cluster");
    }
    
    class ServerMonitorRunnable implements Runnable
    {
        private final ExponentiallyWeightedMovingAverage averageRoundTripTime;
        
        ServerMonitorRunnable() {
            this.averageRoundTripTime = new ExponentiallyWeightedMovingAverage(0.2);
        }
        
        @Override
        public synchronized void run() {
            InternalConnection connection = null;
            try {
                ServerDescription currentServerDescription = this.getConnectingServerDescription(null);
                while (!DefaultServerMonitor.this.isClosed) {
                    final ServerDescription previousServerDescription = currentServerDescription;
                    try {
                        if (connection == null) {
                            connection = DefaultServerMonitor.this.internalConnectionFactory.create(DefaultServerMonitor.this.serverId);
                            try {
                                connection.open();
                            }
                            catch (Throwable t) {
                                connection = null;
                                throw t;
                            }
                        }
                        try {
                            currentServerDescription = this.lookupServerDescription(connection);
                        }
                        catch (MongoSocketException e2) {
                            DefaultServerMonitor.this.connectionPool.invalidate();
                            connection.close();
                            connection = null;
                            connection = DefaultServerMonitor.this.internalConnectionFactory.create(DefaultServerMonitor.this.serverId);
                            try {
                                connection.open();
                            }
                            catch (Throwable t2) {
                                connection = null;
                                throw t2;
                            }
                            try {
                                currentServerDescription = this.lookupServerDescription(connection);
                            }
                            catch (MongoSocketException e1) {
                                connection.close();
                                connection = null;
                                throw e1;
                            }
                        }
                    }
                    catch (Throwable t) {
                        this.averageRoundTripTime.reset();
                        currentServerDescription = this.getConnectingServerDescription(t);
                    }
                    if (!DefaultServerMonitor.this.isClosed) {
                        try {
                            this.logStateChange(previousServerDescription, currentServerDescription);
                            DefaultServerMonitor.this.serverStateListener.stateChanged(new ChangeEvent<ServerDescription>(previousServerDescription, currentServerDescription));
                        }
                        catch (Throwable t) {
                            DefaultServerMonitor.LOGGER.warn("Exception in monitor thread during notification of server description state change", t);
                        }
                        this.waitForNext();
                    }
                }
            }
            finally {
                if (connection != null) {
                    connection.close();
                }
            }
        }
        
        private ServerDescription getConnectingServerDescription(final Throwable exception) {
            return ServerDescription.builder().type(ServerType.UNKNOWN).state(ServerConnectionState.CONNECTING).address(DefaultServerMonitor.this.serverId.getAddress()).exception(exception).build();
        }
        
        private ServerDescription lookupServerDescription(final InternalConnection connection) {
            if (DefaultServerMonitor.LOGGER.isDebugEnabled()) {
                DefaultServerMonitor.LOGGER.debug(String.format("Checking status of %s", DefaultServerMonitor.this.serverId.getAddress()));
            }
            DefaultServerMonitor.this.serverMonitorListener.serverHearbeatStarted(new ServerHeartbeatStartedEvent(connection.getDescription().getConnectionId()));
            final long start = System.nanoTime();
            try {
                final BsonDocument isMasterResult = CommandHelper.executeCommand("admin", new BsonDocument("ismaster", new BsonInt32(1)), connection);
                final long elapsedTimeNanos = System.nanoTime() - start;
                this.averageRoundTripTime.addSample(elapsedTimeNanos);
                DefaultServerMonitor.this.serverMonitorListener.serverHeartbeatSucceeded(new ServerHeartbeatSucceededEvent(connection.getDescription().getConnectionId(), isMasterResult, elapsedTimeNanos));
                return DescriptionHelper.createServerDescription(DefaultServerMonitor.this.serverId.getAddress(), isMasterResult, connection.getDescription().getServerVersion(), this.averageRoundTripTime.getAverage());
            }
            catch (RuntimeException e) {
                DefaultServerMonitor.this.serverMonitorListener.serverHeartbeatFailed(new ServerHeartbeatFailedEvent(connection.getDescription().getConnectionId(), System.nanoTime() - start, e));
                throw e;
            }
        }
        
        private void logStateChange(final ServerDescription previousServerDescription, final ServerDescription currentServerDescription) {
            if (DefaultServerMonitor.shouldLogStageChange(previousServerDescription, currentServerDescription)) {
                if (currentServerDescription.getException() != null) {
                    DefaultServerMonitor.LOGGER.info(String.format("Exception in monitor thread while connecting to server %s", DefaultServerMonitor.this.serverId.getAddress()), currentServerDescription.getException());
                }
                else {
                    DefaultServerMonitor.LOGGER.info(String.format("Monitor thread successfully connected to server with description %s", currentServerDescription));
                }
            }
        }
        
        private void waitForNext() {
            try {
                final long timeRemaining = this.waitForSignalOrTimeout();
                if (timeRemaining > 0L) {
                    final long timeWaiting = DefaultServerMonitor.this.settings.getHeartbeatFrequency(TimeUnit.NANOSECONDS) - timeRemaining;
                    final long minimumNanosToWait = DefaultServerMonitor.this.settings.getMinHeartbeatFrequency(TimeUnit.NANOSECONDS);
                    if (timeWaiting < minimumNanosToWait) {
                        final long millisToSleep = TimeUnit.MILLISECONDS.convert(minimumNanosToWait - timeWaiting, TimeUnit.NANOSECONDS);
                        if (millisToSleep > 0L) {
                            Thread.sleep(millisToSleep);
                        }
                    }
                }
            }
            catch (InterruptedException ex) {}
        }
        
        private long waitForSignalOrTimeout() throws InterruptedException {
            DefaultServerMonitor.this.lock.lock();
            try {
                return DefaultServerMonitor.this.condition.awaitNanos(DefaultServerMonitor.this.settings.getHeartbeatFrequency(TimeUnit.NANOSECONDS));
            }
            finally {
                DefaultServerMonitor.this.lock.unlock();
            }
        }
    }
}
