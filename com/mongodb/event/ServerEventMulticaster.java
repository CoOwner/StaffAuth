package com.mongodb.event;

import com.mongodb.assertions.*;
import java.util.*;
import com.mongodb.diagnostics.logging.*;

public final class ServerEventMulticaster implements ServerListener
{
    private static final Logger LOGGER;
    private final List<ServerListener> serverListeners;
    
    public ServerEventMulticaster(final List<ServerListener> serverListeners) {
        Assertions.notNull("serverListeners", serverListeners);
        Assertions.isTrue("All ServerListener instances are non-null", !serverListeners.contains(null));
        this.serverListeners = new ArrayList<ServerListener>(serverListeners);
    }
    
    public List<ServerListener> getServerListeners() {
        return Collections.unmodifiableList((List<? extends ServerListener>)this.serverListeners);
    }
    
    @Override
    public void serverOpening(final ServerOpeningEvent event) {
        for (final ServerListener cur : this.serverListeners) {
            try {
                cur.serverOpening(event);
            }
            catch (Exception e) {
                if (!ServerEventMulticaster.LOGGER.isWarnEnabled()) {
                    continue;
                }
                ServerEventMulticaster.LOGGER.warn(String.format("Exception thrown raising server opening event to listener %s", cur), e);
            }
        }
    }
    
    @Override
    public void serverClosed(final ServerClosedEvent event) {
        for (final ServerListener cur : this.serverListeners) {
            try {
                cur.serverClosed(event);
            }
            catch (Exception e) {
                if (!ServerEventMulticaster.LOGGER.isWarnEnabled()) {
                    continue;
                }
                ServerEventMulticaster.LOGGER.warn(String.format("Exception thrown raising server opening event to listener %s", cur), e);
            }
        }
    }
    
    @Override
    public void serverDescriptionChanged(final ServerDescriptionChangedEvent event) {
        for (final ServerListener cur : this.serverListeners) {
            try {
                cur.serverDescriptionChanged(event);
            }
            catch (Exception e) {
                if (!ServerEventMulticaster.LOGGER.isWarnEnabled()) {
                    continue;
                }
                ServerEventMulticaster.LOGGER.warn(String.format("Exception thrown raising server description changed event to listener %s", cur), e);
            }
        }
    }
    
    static {
        LOGGER = Loggers.getLogger("cluster.event");
    }
}
