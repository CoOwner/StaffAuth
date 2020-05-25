package com.mongodb.event;

import com.mongodb.annotations.*;
import com.mongodb.connection.*;
import com.mongodb.assertions.*;

@Beta
public final class ConnectionCheckedInEvent
{
    private final ConnectionId connectionId;
    
    public ConnectionCheckedInEvent(final ConnectionId connectionId) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
    }
    
    public ConnectionId getConnectionId() {
        return this.connectionId;
    }
    
    @Override
    public String toString() {
        return "ConnectionCheckedInEvent{connectionId=" + this.connectionId + '}';
    }
}
