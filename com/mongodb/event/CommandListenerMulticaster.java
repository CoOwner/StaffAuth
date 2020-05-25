package com.mongodb.event;

import com.mongodb.annotations.*;
import java.util.*;

@Immutable
@Deprecated
public class CommandListenerMulticaster implements CommandListener
{
    private final CommandEventMulticaster wrapped;
    
    public CommandListenerMulticaster(final List<CommandListener> commandListeners) {
        this.wrapped = new CommandEventMulticaster(commandListeners);
    }
    
    public List<CommandListener> getCommandListeners() {
        return this.wrapped.getCommandListeners();
    }
    
    @Override
    public void commandStarted(final CommandStartedEvent event) {
        this.wrapped.commandStarted(event);
    }
    
    @Override
    public void commandSucceeded(final CommandSucceededEvent event) {
        this.wrapped.commandSucceeded(event);
    }
    
    @Override
    public void commandFailed(final CommandFailedEvent event) {
        this.wrapped.commandFailed(event);
    }
}
