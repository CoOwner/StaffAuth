package com.mongodb.operation;

import com.mongodb.connection.*;
import org.bson.*;
import com.mongodb.*;
import com.mongodb.bulk.*;

final class WriteConcernHelper
{
    static void appendWriteConcernToCommand(final WriteConcern writeConcern, final BsonDocument commandDocument, final ConnectionDescription description) {
        if (writeConcern != null && !writeConcern.isServerDefault() && OperationHelper.serverIsAtLeastVersionThreeDotFour(description)) {
            commandDocument.put("writeConcern", writeConcern.asDocument());
        }
    }
    
    static CommandOperationHelper.CommandTransformer<BsonDocument, Void> writeConcernErrorTransformer() {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, Void>() {
            @Override
            public Void apply(final BsonDocument result, final ServerAddress serverAddress) {
                WriteConcernHelper.throwOnWriteConcernError(result, serverAddress);
                return null;
            }
        };
    }
    
    static void throwOnWriteConcernError(final BsonDocument result, final ServerAddress serverAddress) {
        if (hasWriteConcernError(result)) {
            throw createWriteConcernError(result, serverAddress);
        }
    }
    
    static boolean hasWriteConcernError(final BsonDocument result) {
        return result.containsKey("writeConcernError");
    }
    
    static MongoWriteConcernException createWriteConcernError(final BsonDocument result, final ServerAddress serverAddress) {
        return new MongoWriteConcernException(createWriteConcernError(result.getDocument("writeConcernError")), WriteConcernResult.acknowledged(0, false, null), serverAddress);
    }
    
    static WriteConcernError createWriteConcernError(final BsonDocument writeConcernErrorDocument) {
        return new WriteConcernError(writeConcernErrorDocument.getNumber("code").intValue(), writeConcernErrorDocument.getString("errmsg").getValue(), writeConcernErrorDocument.getDocument("errInfo", new BsonDocument()));
    }
    
    private WriteConcernHelper() {
    }
}
