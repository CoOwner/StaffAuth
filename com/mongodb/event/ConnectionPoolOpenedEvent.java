package com.mongodb.event;

import com.mongodb.annotations.*;
import com.mongodb.connection.*;
import com.mongodb.assertions.*;

@Beta
public final class ConnectionPoolOpenedEvent
{
    private final ServerId serverId;
    private final ConnectionPoolSettings settings;
    
    public ConnectionPoolOpenedEvent(final ServerId serverId, final ConnectionPoolSettings settings) {
        this.serverId = Assertions.notNull("serverId", serverId);
        this.settings = Assertions.notNull("settings", settings);
    }
    
    public ServerId getServerId() {
        return this.serverId;
    }
    
    public ConnectionPoolSettings getSettings() {
        return this.settings;
    }
    
    @Override
    public String toString() {
        return "ConnectionPoolOpenedEvent{serverId=" + this.serverId + "settings=" + this.settings + '}';
    }
}
