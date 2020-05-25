package com.mongodb.event;

import com.mongodb.annotations.*;
import com.mongodb.connection.*;
import com.mongodb.assertions.*;

@Beta
public final class ConnectionAddedEvent
{
    private final ConnectionId connectionId;
    
    public ConnectionAddedEvent(final ConnectionId connectionId) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
    }
    
    public ConnectionId getConnectionId() {
        return this.connectionId;
    }
    
    @Override
    public String toString() {
        return "ConnectionAddedEvent{connectionId=" + this.connectionId + '}';
    }
}
