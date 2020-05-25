package com.mongodb.client.model;

@Deprecated
public class ParallelCollectionScanOptions
{
    private int batchSize;
    
    public int getBatchSize() {
        return this.batchSize;
    }
    
    public ParallelCollectionScanOptions batchSize(final int batchSize) {
        this.batchSize = batchSize;
        return this;
    }
}
