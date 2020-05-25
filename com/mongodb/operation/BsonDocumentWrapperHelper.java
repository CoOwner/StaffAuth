package com.mongodb.operation;

import java.util.*;
import org.bson.*;

final class BsonDocumentWrapperHelper
{
    static <T> List<T> toList(final BsonDocument result, final String fieldContainingWrappedArray) {
        return ((BsonArrayWrapper)result.getArray(fieldContainingWrappedArray)).getWrappedArray();
    }
    
    static <T> T toDocument(final BsonDocument document) {
        if (document == null) {
            return null;
        }
        return ((BsonDocumentWrapper)document).getWrappedDocument();
    }
    
    private BsonDocumentWrapperHelper() {
    }
}
