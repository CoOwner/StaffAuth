package com.mongodb.client;

import java.util.concurrent.*;

public interface ListIndexesIterable<TResult> extends MongoIterable<TResult>
{
    ListIndexesIterable<TResult> maxTime(final long p0, final TimeUnit p1);
    
    ListIndexesIterable<TResult> batchSize(final int p0);
}
