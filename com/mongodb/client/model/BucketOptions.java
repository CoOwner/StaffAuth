package com.mongodb.client.model;

import java.util.*;

public class BucketOptions
{
    private Object defaultBucket;
    private List<BsonField> output;
    
    public BucketOptions defaultBucket(final Object name) {
        this.defaultBucket = name;
        return this;
    }
    
    public Object getDefaultBucket() {
        return this.defaultBucket;
    }
    
    public List<BsonField> getOutput() {
        return (this.output == null) ? null : new ArrayList<BsonField>(this.output);
    }
    
    public BucketOptions output(final BsonField... output) {
        this.output = Arrays.asList(output);
        return this;
    }
    
    public BucketOptions output(final List<BsonField> output) {
        this.output = output;
        return this;
    }
}
