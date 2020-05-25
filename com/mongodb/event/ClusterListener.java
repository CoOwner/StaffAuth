package com.mongodb.event;

import java.util.*;

public interface ClusterListener extends EventListener
{
    void clusterOpening(final ClusterOpeningEvent p0);
    
    void clusterClosed(final ClusterClosedEvent p0);
    
    void clusterDescriptionChanged(final ClusterDescriptionChangedEvent p0);
}
