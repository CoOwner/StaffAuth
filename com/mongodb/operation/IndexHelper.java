package com.mongodb.operation;

import java.util.*;
import org.bson.*;

final class IndexHelper
{
    static String generateIndexName(final BsonDocument index) {
        final StringBuilder indexName = new StringBuilder();
        for (final String keyNames : index.keySet()) {
            if (indexName.length() != 0) {
                indexName.append('_');
            }
            indexName.append(keyNames).append('_');
            final BsonValue ascOrDescValue = index.get(keyNames);
            if (ascOrDescValue instanceof BsonNumber) {
                indexName.append(((BsonNumber)ascOrDescValue).intValue());
            }
            else {
                if (!(ascOrDescValue instanceof BsonString)) {
                    continue;
                }
                indexName.append(((BsonString)ascOrDescValue).getValue().replace(' ', '_'));
            }
        }
        return indexName.toString();
    }
    
    private IndexHelper() {
    }
}
