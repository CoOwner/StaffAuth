package com.mongodb.event;

import com.mongodb.assertions.*;
import java.util.*;
import com.mongodb.diagnostics.logging.*;

public final class ServerMonitorEventMulticaster implements ServerMonitorListener
{
    private static final Logger LOGGER;
    private final List<ServerMonitorListener> serverMonitorListeners;
    
    public ServerMonitorEventMulticaster(final List<ServerMonitorListener> serverMonitorListeners) {
        Assertions.notNull("serverMonitorListeners", serverMonitorListeners);
        Assertions.isTrue("All ServerMonitorListener instances are non-null", !serverMonitorListeners.contains(null));
        this.serverMonitorListeners = new ArrayList<ServerMonitorListener>(serverMonitorListeners);
    }
    
    public List<ServerMonitorListener> getServerMonitorListeners() {
        return Collections.unmodifiableList((List<? extends ServerMonitorListener>)this.serverMonitorListeners);
    }
    
    @Override
    public void serverHearbeatStarted(final ServerHeartbeatStartedEvent event) {
        for (final ServerMonitorListener cur : this.serverMonitorListeners) {
            try {
                cur.serverHearbeatStarted(event);
            }
            catch (Exception e) {
                if (!ServerMonitorEventMulticaster.LOGGER.isWarnEnabled()) {
                    continue;
                }
                ServerMonitorEventMulticaster.LOGGER.warn(String.format("Exception thrown raising server heartbeat started event to listener %s", cur), e);
            }
        }
    }
    
    @Override
    public void serverHeartbeatSucceeded(final ServerHeartbeatSucceededEvent event) {
        for (final ServerMonitorListener cur : this.serverMonitorListeners) {
            try {
                cur.serverHeartbeatSucceeded(event);
            }
            catch (Exception e) {
                if (!ServerMonitorEventMulticaster.LOGGER.isWarnEnabled()) {
                    continue;
                }
                ServerMonitorEventMulticaster.LOGGER.warn(String.format("Exception thrown raising server heartbeat succeeded event to listener %s", cur), e);
            }
        }
    }
    
    @Override
    public void serverHeartbeatFailed(final ServerHeartbeatFailedEvent event) {
        for (final ServerMonitorListener cur : this.serverMonitorListeners) {
            try {
                cur.serverHeartbeatFailed(event);
            }
            catch (Exception e) {
                if (!ServerMonitorEventMulticaster.LOGGER.isWarnEnabled()) {
                    continue;
                }
                ServerMonitorEventMulticaster.LOGGER.warn(String.format("Exception thrown raising server heartbeat failed event to listener %s", cur), e);
            }
        }
    }
    
    static {
        LOGGER = Loggers.getLogger("cluster.event");
    }
}
