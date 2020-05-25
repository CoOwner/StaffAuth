package com.mongodb.event;

import com.mongodb.connection.*;

public abstract class CommandEvent
{
    private final int requestId;
    private final ConnectionDescription connectionDescription;
    private final String commandName;
    
    public CommandEvent(final int requestId, final ConnectionDescription connectionDescription, final String commandName) {
        this.requestId = requestId;
        this.connectionDescription = connectionDescription;
        this.commandName = commandName;
    }
    
    public int getRequestId() {
        return this.requestId;
    }
    
    public ConnectionDescription getConnectionDescription() {
        return this.connectionDescription;
    }
    
    public String getCommandName() {
        return this.commandName;
    }
}
