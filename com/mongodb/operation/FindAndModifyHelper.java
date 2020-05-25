package com.mongodb.operation;

import com.mongodb.bulk.*;
import com.mongodb.*;
import org.bson.*;

final class FindAndModifyHelper
{
    static <T> CommandOperationHelper.CommandTransformer<BsonDocument, T> transformer() {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, T>() {
            @Override
            public T apply(final BsonDocument result, final ServerAddress serverAddress) {
                if (result.containsKey("writeConcernError")) {
                    throw new MongoWriteConcernException(createWriteConcernError(result.getDocument("writeConcernError")), createWriteConcernResult(result.getDocument("lastErrorObject", new BsonDocument())), serverAddress);
                }
                if (!result.isDocument("value")) {
                    return null;
                }
                return BsonDocumentWrapperHelper.toDocument(result.getDocument("value", null));
            }
        };
    }
    
    private static WriteConcernError createWriteConcernError(final BsonDocument writeConcernErrorDocument) {
        return new WriteConcernError(writeConcernErrorDocument.getNumber("code").intValue(), writeConcernErrorDocument.getString("errmsg").getValue(), writeConcernErrorDocument.getDocument("errInfo", new BsonDocument()));
    }
    
    private static WriteConcernResult createWriteConcernResult(final BsonDocument result) {
        final BsonBoolean updatedExisting = result.getBoolean("updatedExisting", BsonBoolean.FALSE);
        return WriteConcernResult.acknowledged(result.getNumber("n", new BsonInt32(0)).intValue(), updatedExisting.getValue(), result.get("upserted"));
    }
    
    private FindAndModifyHelper() {
    }
}
