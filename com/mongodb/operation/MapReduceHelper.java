package com.mongodb.operation;

import org.bson.*;

final class MapReduceHelper
{
    static MapReduceStatistics createStatistics(final BsonDocument result) {
        return new MapReduceStatistics(getInputCount(result), getOutputCount(result), getEmitCount(result), getDuration(result));
    }
    
    private static int getInputCount(final BsonDocument result) {
        return result.getDocument("counts").getNumber("input").intValue();
    }
    
    private static int getOutputCount(final BsonDocument result) {
        return result.getDocument("counts").getNumber("output").intValue();
    }
    
    private static int getEmitCount(final BsonDocument result) {
        return result.getDocument("counts").getNumber("emit").intValue();
    }
    
    private static int getDuration(final BsonDocument result) {
        return result.getNumber("timeMillis").intValue();
    }
    
    private MapReduceHelper() {
    }
}
