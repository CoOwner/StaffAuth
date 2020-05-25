package com.mongodb.event;

import com.mongodb.annotations.*;

@Beta
public abstract class ConnectionPoolListenerAdapter implements ConnectionPoolListener
{
    @Override
    public void connectionPoolOpened(final ConnectionPoolOpenedEvent event) {
    }
    
    @Override
    public void connectionPoolClosed(final ConnectionPoolClosedEvent event) {
    }
    
    @Override
    public void connectionCheckedOut(final ConnectionCheckedOutEvent event) {
    }
    
    @Override
    public void connectionCheckedIn(final ConnectionCheckedInEvent event) {
    }
    
    @Override
    public void waitQueueEntered(final ConnectionPoolWaitQueueEnteredEvent event) {
    }
    
    @Override
    public void waitQueueExited(final ConnectionPoolWaitQueueExitedEvent event) {
    }
    
    @Override
    public void connectionAdded(final ConnectionAddedEvent event) {
    }
    
    @Override
    public void connectionRemoved(final ConnectionRemovedEvent event) {
    }
}
