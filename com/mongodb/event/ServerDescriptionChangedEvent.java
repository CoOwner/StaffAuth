package com.mongodb.event;

import com.mongodb.connection.*;
import com.mongodb.assertions.*;

public final class ServerDescriptionChangedEvent
{
    private final ServerId serverId;
    private final ServerDescription newDescription;
    private final ServerDescription previousDescription;
    
    public ServerDescriptionChangedEvent(final ServerId serverId, final ServerDescription newDescription, final ServerDescription previousDescription) {
        this.serverId = Assertions.notNull("serverId", serverId);
        this.newDescription = Assertions.notNull("newDescription", newDescription);
        this.previousDescription = Assertions.notNull("previousDescription", previousDescription);
    }
    
    public ServerId getServerId() {
        return this.serverId;
    }
    
    public ServerDescription getNewDescription() {
        return this.newDescription;
    }
    
    public ServerDescription getPreviousDescription() {
        return this.previousDescription;
    }
    
    @Override
    public String toString() {
        return "ServerDescriptionChangedEvent{serverId=" + this.serverId + ", newDescription=" + this.newDescription + ", previousDescription=" + this.previousDescription + '}';
    }
}
