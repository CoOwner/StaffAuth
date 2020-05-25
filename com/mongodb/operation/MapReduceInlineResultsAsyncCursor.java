package com.mongodb.operation;

import com.mongodb.connection.*;
import org.bson.codecs.*;

class MapReduceInlineResultsAsyncCursor<T> extends AsyncQueryBatchCursor<T> implements MapReduceAsyncBatchCursor<T>
{
    private final MapReduceStatistics statistics;
    
    MapReduceInlineResultsAsyncCursor(final QueryResult<T> queryResult, final Decoder<T> decoder, final MapReduceStatistics statistics) {
        super(queryResult, 0, 0, decoder);
        this.statistics = statistics;
    }
    
    @Override
    public MapReduceStatistics getStatistics() {
        return this.statistics;
    }
}
