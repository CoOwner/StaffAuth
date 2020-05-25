package com.mongodb.operation;

import com.mongodb.async.*;

public interface MapReduceAsyncBatchCursor<T> extends AsyncBatchCursor<T>
{
    MapReduceStatistics getStatistics();
}
