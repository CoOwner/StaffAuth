package com.mongodb.selector;

import java.util.*;
import com.mongodb.connection.*;

public final class WritableServerSelector implements ServerSelector
{
    @Override
    public List<ServerDescription> select(final ClusterDescription clusterDescription) {
        return clusterDescription.getPrimaries();
    }
    
    @Override
    public String toString() {
        return "WritableServerSelector";
    }
}
