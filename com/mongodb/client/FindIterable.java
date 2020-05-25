package com.mongodb.client;

import org.bson.conversions.*;
import java.util.concurrent.*;
import com.mongodb.*;
import com.mongodb.client.model.*;

public interface FindIterable<TResult> extends MongoIterable<TResult>
{
    FindIterable<TResult> filter(final Bson p0);
    
    FindIterable<TResult> limit(final int p0);
    
    FindIterable<TResult> skip(final int p0);
    
    FindIterable<TResult> maxTime(final long p0, final TimeUnit p1);
    
    FindIterable<TResult> maxAwaitTime(final long p0, final TimeUnit p1);
    
    FindIterable<TResult> modifiers(final Bson p0);
    
    FindIterable<TResult> projection(final Bson p0);
    
    FindIterable<TResult> sort(final Bson p0);
    
    FindIterable<TResult> noCursorTimeout(final boolean p0);
    
    FindIterable<TResult> oplogReplay(final boolean p0);
    
    FindIterable<TResult> partial(final boolean p0);
    
    FindIterable<TResult> cursorType(final CursorType p0);
    
    FindIterable<TResult> batchSize(final int p0);
    
    FindIterable<TResult> collation(final Collation p0);
}
