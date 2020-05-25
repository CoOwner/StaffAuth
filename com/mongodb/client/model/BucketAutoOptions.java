package com.mongodb.client.model;

import java.util.*;

public class BucketAutoOptions
{
    private List<BsonField> output;
    private BucketGranularity granularity;
    
    public BucketGranularity getGranularity() {
        return this.granularity;
    }
    
    public List<BsonField> getOutput() {
        return (this.output == null) ? null : new ArrayList<BsonField>(this.output);
    }
    
    public BucketAutoOptions granularity(final BucketGranularity granularity) {
        this.granularity = granularity;
        return this;
    }
    
    public BucketAutoOptions output(final BsonField... output) {
        this.output = Arrays.asList(output);
        return this;
    }
    
    public BucketAutoOptions output(final List<BsonField> output) {
        this.output = output;
        return this;
    }
}
