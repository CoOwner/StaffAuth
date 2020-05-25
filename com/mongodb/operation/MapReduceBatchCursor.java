package com.mongodb.operation;

public interface MapReduceBatchCursor<T> extends BatchCursor<T>
{
    MapReduceStatistics getStatistics();
}
