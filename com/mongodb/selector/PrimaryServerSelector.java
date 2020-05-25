package com.mongodb.selector;

import java.util.*;
import com.mongodb.connection.*;

@Deprecated
public final class PrimaryServerSelector implements ServerSelector
{
    @Override
    public List<ServerDescription> select(final ClusterDescription clusterDescription) {
        return clusterDescription.getPrimaries();
    }
    
    @Override
    public String toString() {
        return "PrimaryServerSelector";
    }
}
