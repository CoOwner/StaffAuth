package com.mongodb.selector;

import com.mongodb.*;
import com.mongodb.assertions.*;
import java.util.*;
import com.mongodb.connection.*;

public class ReadPreferenceServerSelector implements ServerSelector
{
    private final ReadPreference readPreference;
    
    public ReadPreferenceServerSelector(final ReadPreference readPreference) {
        this.readPreference = Assertions.notNull("readPreference", readPreference);
    }
    
    public ReadPreference getReadPreference() {
        return this.readPreference;
    }
    
    @Override
    public List<ServerDescription> select(final ClusterDescription clusterDescription) {
        if (clusterDescription.getConnectionMode() == ClusterConnectionMode.SINGLE) {
            return clusterDescription.getAny();
        }
        return this.readPreference.choose(clusterDescription);
    }
    
    @Override
    public String toString() {
        return "ReadPreferenceServerSelector{readPreference=" + this.readPreference + '}';
    }
}
