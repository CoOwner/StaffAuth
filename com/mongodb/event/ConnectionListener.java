package com.mongodb.event;

import java.util.*;
import com.mongodb.annotations.*;

@Beta
public interface ConnectionListener extends EventListener
{
    void connectionOpened(final ConnectionOpenedEvent p0);
    
    void connectionClosed(final ConnectionClosedEvent p0);
    
    void messagesSent(final ConnectionMessagesSentEvent p0);
    
    void messageReceived(final ConnectionMessageReceivedEvent p0);
}
