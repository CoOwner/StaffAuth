package com.mongodb.client;

import org.bson.conversions.*;
import java.util.concurrent.*;
import com.mongodb.client.model.*;

public interface DistinctIterable<TResult> extends MongoIterable<TResult>
{
    DistinctIterable<TResult> filter(final Bson p0);
    
    DistinctIterable<TResult> maxTime(final long p0, final TimeUnit p1);
    
    DistinctIterable<TResult> batchSize(final int p0);
    
    DistinctIterable<TResult> collation(final Collation p0);
}
