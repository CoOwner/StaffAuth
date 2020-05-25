package com.mongodb.operation;

import com.mongodb.*;

final class QueryHelper
{
    static MongoQueryException translateCommandException(final MongoCommandException commandException, final ServerCursor cursor) {
        if (commandException.getErrorCode() == 43) {
            return new MongoCursorNotFoundException(cursor.getId(), cursor.getAddress());
        }
        return new MongoQueryException(cursor.getAddress(), commandException.getErrorCode(), commandException.getErrorMessage());
    }
    
    private QueryHelper() {
    }
}
