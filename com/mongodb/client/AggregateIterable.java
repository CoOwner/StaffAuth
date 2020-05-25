package com.mongodb.client;

import java.util.concurrent.*;
import com.mongodb.client.model.*;

public interface AggregateIterable<TResult> extends MongoIterable<TResult>
{
    void toCollection();
    
    AggregateIterable<TResult> allowDiskUse(final Boolean p0);
    
    AggregateIterable<TResult> batchSize(final int p0);
    
    AggregateIterable<TResult> maxTime(final long p0, final TimeUnit p1);
    
    AggregateIterable<TResult> useCursor(final Boolean p0);
    
    AggregateIterable<TResult> bypassDocumentValidation(final Boolean p0);
    
    AggregateIterable<TResult> collation(final Collation p0);
}
