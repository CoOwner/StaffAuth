package com.mongodb.event;

import com.mongodb.annotations.*;
import com.mongodb.connection.*;
import com.mongodb.assertions.*;

@Beta
public final class ConnectionOpenedEvent
{
    private final ConnectionId connectionId;
    
    public ConnectionOpenedEvent(final ConnectionId connectionId) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
    }
    
    public ConnectionId getConnectionId() {
        return this.connectionId;
    }
    
    @Override
    public String toString() {
        return "ConnectionOpenedEvent{connectionId=" + this.connectionId + '}';
    }
}
