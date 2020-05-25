package com.mongodb.event;

import com.mongodb.annotations.*;

@Beta
public abstract class ConnectionListenerAdapter implements ConnectionListener
{
    @Override
    public void connectionOpened(final ConnectionOpenedEvent event) {
    }
    
    @Override
    public void connectionClosed(final ConnectionClosedEvent event) {
    }
    
    @Override
    public void messagesSent(final ConnectionMessagesSentEvent event) {
    }
    
    @Override
    public void messageReceived(final ConnectionMessageReceivedEvent event) {
    }
}
