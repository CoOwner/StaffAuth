package com.mongodb.event;

import com.mongodb.connection.*;
import com.mongodb.assertions.*;

public final class ServerClosedEvent
{
    private final ServerId serverId;
    
    public ServerClosedEvent(final ServerId serverId) {
        this.serverId = Assertions.notNull("serverId", serverId);
    }
    
    public ServerId getServerId() {
        return this.serverId;
    }
    
    @Override
    public String toString() {
        return "ServerClosedEvent{serverId=" + this.serverId + '}';
    }
}
