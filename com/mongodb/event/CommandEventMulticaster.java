package com.mongodb.event;

import com.mongodb.annotations.*;
import com.mongodb.assertions.*;
import java.util.*;
import com.mongodb.diagnostics.logging.*;

@Immutable
public final class CommandEventMulticaster implements CommandListener
{
    private static final Logger LOGGER;
    private final List<CommandListener> commandListeners;
    
    public CommandEventMulticaster(final List<CommandListener> commandListeners) {
        Assertions.notNull("commandListeners", commandListeners);
        Assertions.isTrue("All CommandListener instances are non-null", !commandListeners.contains(null));
        this.commandListeners = new ArrayList<CommandListener>(commandListeners);
    }
    
    public List<CommandListener> getCommandListeners() {
        return Collections.unmodifiableList((List<? extends CommandListener>)this.commandListeners);
    }
    
    @Override
    public void commandStarted(final CommandStartedEvent event) {
        for (final CommandListener cur : this.commandListeners) {
            try {
                cur.commandStarted(event);
            }
            catch (Exception e) {
                if (!CommandEventMulticaster.LOGGER.isWarnEnabled()) {
                    continue;
                }
                CommandEventMulticaster.LOGGER.warn(String.format("Exception thrown raising command started event to listener %s", cur), e);
            }
        }
    }
    
    @Override
    public void commandSucceeded(final CommandSucceededEvent event) {
        for (final CommandListener cur : this.commandListeners) {
            try {
                cur.commandSucceeded(event);
            }
            catch (Exception e) {
                if (!CommandEventMulticaster.LOGGER.isWarnEnabled()) {
                    continue;
                }
                CommandEventMulticaster.LOGGER.warn(String.format("Exception thrown raising command succeeded event to listener %s", cur), e);
            }
        }
    }
    
    @Override
    public void commandFailed(final CommandFailedEvent event) {
        for (final CommandListener cur : this.commandListeners) {
            try {
                cur.commandFailed(event);
            }
            catch (Exception e) {
                if (!CommandEventMulticaster.LOGGER.isWarnEnabled()) {
                    continue;
                }
                CommandEventMulticaster.LOGGER.warn(String.format("Exception thrown raising command failed event to listener %s", cur), e);
            }
        }
    }
    
    static {
        LOGGER = Loggers.getLogger("protocol.event");
    }
}
