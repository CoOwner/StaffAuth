package com.mongodb.client;

import org.bson.conversions.*;
import java.util.concurrent.*;

public interface ListCollectionsIterable<TResult> extends MongoIterable<TResult>
{
    ListCollectionsIterable<TResult> filter(final Bson p0);
    
    ListCollectionsIterable<TResult> maxTime(final long p0, final TimeUnit p1);
    
    ListCollectionsIterable<TResult> batchSize(final int p0);
}
