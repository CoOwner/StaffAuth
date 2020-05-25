package com.mongodb.event;

import com.mongodb.annotations.*;
import com.mongodb.connection.*;
import org.bson.assertions.*;

@Beta
public final class ConnectionClosedEvent
{
    private final ConnectionId connectionId;
    
    public ConnectionClosedEvent(final ConnectionId connectionId) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
    }
    
    public ConnectionId getConnectionId() {
        return this.connectionId;
    }
    
    @Override
    public String toString() {
        return "ConnectionClosedEvent{connectionId=" + this.connectionId + '}';
    }
}
