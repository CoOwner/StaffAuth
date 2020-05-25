package com.mongodb.operation;

import com.mongodb.connection.*;
import org.bson.codecs.*;
import com.mongodb.binding.*;

class MapReduceInlineResultsCursor<T> extends QueryBatchCursor<T> implements MapReduceBatchCursor<T>
{
    private final MapReduceStatistics statistics;
    
    MapReduceInlineResultsCursor(final QueryResult<T> queryResult, final Decoder<T> decoder, final ConnectionSource connectionSource, final MapReduceStatistics statistics) {
        super(queryResult, 0, 0, decoder, connectionSource);
        this.statistics = statistics;
    }
    
    @Override
    public MapReduceStatistics getStatistics() {
        return this.statistics;
    }
}
