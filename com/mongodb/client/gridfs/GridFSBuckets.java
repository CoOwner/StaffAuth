package com.mongodb.client.gridfs;

import com.mongodb.client.*;

public final class GridFSBuckets
{
    public static GridFSBucket create(final MongoDatabase database) {
        return new GridFSBucketImpl(database);
    }
    
    public static GridFSBucket create(final MongoDatabase database, final String bucketName) {
        return new GridFSBucketImpl(database, bucketName);
    }
    
    private GridFSBuckets() {
    }
}
