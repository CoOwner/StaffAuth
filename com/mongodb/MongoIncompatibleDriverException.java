package com.mongodb;

import com.mongodb.connection.*;

public class MongoIncompatibleDriverException extends MongoException
{
    private static final long serialVersionUID = -5213381354402601890L;
    private ClusterDescription clusterDescription;
    
    public MongoIncompatibleDriverException(final String message, final ClusterDescription clusterDescription) {
        super(message);
        this.clusterDescription = clusterDescription;
    }
    
    public ClusterDescription getClusterDescription() {
        return this.clusterDescription;
    }
}
