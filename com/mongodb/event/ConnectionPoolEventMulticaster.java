package com.mongodb.event;

import com.mongodb.annotations.*;
import java.util.concurrent.*;
import java.util.*;

@Beta
public final class ConnectionPoolEventMulticaster implements ConnectionPoolListener
{
    private final Set<ConnectionPoolListener> connectionPoolListeners;
    
    public ConnectionPoolEventMulticaster() {
        this.connectionPoolListeners = Collections.newSetFromMap(new ConcurrentHashMap<ConnectionPoolListener, Boolean>());
    }
    
    public void add(final ConnectionPoolListener connectionPoolListener) {
        this.connectionPoolListeners.add(connectionPoolListener);
    }
    
    public void remove(final ConnectionPoolListener connectionPoolListener) {
        this.connectionPoolListeners.remove(connectionPoolListener);
    }
    
    @Override
    public void connectionPoolOpened(final ConnectionPoolOpenedEvent event) {
        for (final ConnectionPoolListener cur : this.connectionPoolListeners) {
            cur.connectionPoolOpened(event);
        }
    }
    
    @Override
    public void connectionPoolClosed(final ConnectionPoolClosedEvent event) {
        for (final ConnectionPoolListener cur : this.connectionPoolListeners) {
            cur.connectionPoolClosed(event);
        }
    }
    
    @Override
    public void connectionCheckedOut(final ConnectionCheckedOutEvent event) {
        for (final ConnectionPoolListener cur : this.connectionPoolListeners) {
            cur.connectionCheckedOut(event);
        }
    }
    
    @Override
    public void connectionCheckedIn(final ConnectionCheckedInEvent event) {
        for (final ConnectionPoolListener cur : this.connectionPoolListeners) {
            cur.connectionCheckedIn(event);
        }
    }
    
    @Override
    public void waitQueueEntered(final ConnectionPoolWaitQueueEnteredEvent event) {
        for (final ConnectionPoolListener cur : this.connectionPoolListeners) {
            cur.waitQueueEntered(event);
        }
    }
    
    @Override
    public void waitQueueExited(final ConnectionPoolWaitQueueExitedEvent event) {
        for (final ConnectionPoolListener cur : this.connectionPoolListeners) {
            cur.waitQueueExited(event);
        }
    }
    
    @Override
    public void connectionAdded(final ConnectionAddedEvent event) {
        for (final ConnectionPoolListener cur : this.connectionPoolListeners) {
            cur.connectionAdded(event);
        }
    }
    
    @Override
    public void connectionRemoved(final ConnectionRemovedEvent event) {
        for (final ConnectionPoolListener cur : this.connectionPoolListeners) {
            cur.connectionRemoved(event);
        }
    }
}
