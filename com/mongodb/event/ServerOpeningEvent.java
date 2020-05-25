package com.mongodb.event;

import com.mongodb.connection.*;
import com.mongodb.assertions.*;

public final class ServerOpeningEvent
{
    private final ServerId serverId;
    
    public ServerOpeningEvent(final ServerId serverId) {
        this.serverId = Assertions.notNull("serverId", serverId);
    }
    
    public ServerId getServerId() {
        return this.serverId;
    }
    
    @Override
    public String toString() {
        return "ServerOpeningEvent{serverId=" + this.serverId + '}';
    }
}
