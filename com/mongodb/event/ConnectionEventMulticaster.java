package com.mongodb.event;

import com.mongodb.annotations.*;
import java.util.concurrent.*;
import java.util.*;

@Beta
public final class ConnectionEventMulticaster implements ConnectionListener
{
    private final Set<ConnectionListener> connectionListeners;
    
    public ConnectionEventMulticaster() {
        this.connectionListeners = Collections.newSetFromMap(new ConcurrentHashMap<ConnectionListener, Boolean>());
    }
    
    public void add(final ConnectionListener connectionListener) {
        this.connectionListeners.add(connectionListener);
    }
    
    public void remove(final ConnectionListener connectionListener) {
        this.connectionListeners.remove(connectionListener);
    }
    
    @Override
    public void connectionOpened(final ConnectionOpenedEvent event) {
        for (final ConnectionListener cur : this.connectionListeners) {
            cur.connectionOpened(event);
        }
    }
    
    @Override
    public void connectionClosed(final ConnectionClosedEvent event) {
        for (final ConnectionListener cur : this.connectionListeners) {
            cur.connectionClosed(event);
        }
    }
    
    @Override
    public void messagesSent(final ConnectionMessagesSentEvent event) {
        for (final ConnectionListener cur : this.connectionListeners) {
            cur.messagesSent(event);
        }
    }
    
    @Override
    public void messageReceived(final ConnectionMessageReceivedEvent event) {
        for (final ConnectionListener cur : this.connectionListeners) {
            cur.messageReceived(event);
        }
    }
}
