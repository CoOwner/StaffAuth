package com.mongodb.operation;

import org.bson.*;

final class DocumentHelper
{
    private DocumentHelper() {
    }
    
    static void putIfTrue(final BsonDocument command, final String key, final boolean condition) {
        if (condition) {
            command.put(key, BsonBoolean.TRUE);
        }
    }
    
    static void putIfNotNull(final BsonDocument command, final String key, final BsonValue value) {
        if (value != null) {
            command.put(key, value);
        }
    }
    
    static void putIfNotZero(final BsonDocument command, final String key, final int value) {
        if (value != 0) {
            command.put(key, new BsonInt32(value));
        }
    }
    
    static void putIfNotZero(final BsonDocument command, final String key, final long value) {
        if (value != 0L) {
            command.put(key, new BsonInt64(value));
        }
    }
}
