package com.mongodb.client;

import com.mongodb.*;
import java.util.*;

public interface MongoIterable<TResult> extends Iterable<TResult>
{
    MongoCursor<TResult> iterator();
    
    TResult first();
    
     <U> MongoIterable<U> map(final Function<TResult, U> p0);
    
    void forEach(final Block<? super TResult> p0);
    
     <A extends Collection<? super TResult>> A into(final A p0);
    
    MongoIterable<TResult> batchSize(final int p0);
}
