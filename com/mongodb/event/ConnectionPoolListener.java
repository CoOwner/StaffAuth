package com.mongodb.event;

import java.util.*;
import com.mongodb.annotations.*;

@Beta
public interface ConnectionPoolListener extends EventListener
{
    void connectionPoolOpened(final ConnectionPoolOpenedEvent p0);
    
    void connectionPoolClosed(final ConnectionPoolClosedEvent p0);
    
    void connectionCheckedOut(final ConnectionCheckedOutEvent p0);
    
    void connectionCheckedIn(final ConnectionCheckedInEvent p0);
    
    void waitQueueEntered(final ConnectionPoolWaitQueueEnteredEvent p0);
    
    void waitQueueExited(final ConnectionPoolWaitQueueExitedEvent p0);
    
    void connectionAdded(final ConnectionAddedEvent p0);
    
    void connectionRemoved(final ConnectionRemovedEvent p0);
}
