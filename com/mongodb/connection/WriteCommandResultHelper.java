package com.mongodb.connection;

import com.mongodb.*;
import com.mongodb.bulk.*;
import java.util.*;
import org.bson.*;

final class WriteCommandResultHelper
{
    static boolean hasError(final BsonDocument result) {
        return result.get("writeErrors") != null || result.get("writeConcernError") != null;
    }
    
    static BulkWriteResult getBulkWriteResult(final WriteRequest.Type type, final BsonDocument result) {
        final int count = getCount(result);
        final List<BulkWriteUpsert> upsertedItems = getUpsertedItems(result);
        return BulkWriteResult.acknowledged(type, count - upsertedItems.size(), getModifiedCount(type, result), upsertedItems);
    }
    
    static MongoBulkWriteException getBulkWriteException(final WriteRequest.Type type, final BsonDocument result, final ServerAddress serverAddress) {
        if (!hasError(result)) {
            throw new MongoInternalException("This method should not have been called");
        }
        return new MongoBulkWriteException(getBulkWriteResult(type, result), getWriteErrors(result), getWriteConcernError(result), serverAddress);
    }
    
    private static List<BulkWriteError> getWriteErrors(final BsonDocument result) {
        final List<BulkWriteError> writeErrors = new ArrayList<BulkWriteError>();
        final BsonArray writeErrorsDocuments = (BsonArray)result.get("writeErrors");
        if (writeErrorsDocuments != null) {
            for (final BsonValue cur : writeErrorsDocuments) {
                final BsonDocument curDocument = (BsonDocument)cur;
                writeErrors.add(new BulkWriteError(curDocument.getNumber("code").intValue(), curDocument.getString("errmsg").getValue(), curDocument.getDocument("errInfo", new BsonDocument()), curDocument.getNumber("index").intValue()));
            }
        }
        return writeErrors;
    }
    
    private static WriteConcernError getWriteConcernError(final BsonDocument result) {
        final BsonDocument writeConcernErrorDocument = (BsonDocument)result.get("writeConcernError");
        if (writeConcernErrorDocument == null) {
            return null;
        }
        return new WriteConcernError(writeConcernErrorDocument.getNumber("code").intValue(), writeConcernErrorDocument.getString("errmsg").getValue(), writeConcernErrorDocument.getDocument("errInfo", new BsonDocument()));
    }
    
    private static List<BulkWriteUpsert> getUpsertedItems(final BsonDocument result) {
        final BsonValue upsertedValue = result.get("upserted");
        if (upsertedValue == null) {
            return Collections.emptyList();
        }
        final List<BulkWriteUpsert> bulkWriteUpsertList = new ArrayList<BulkWriteUpsert>();
        for (final BsonValue upsertedItem : (BsonArray)upsertedValue) {
            final BsonDocument upsertedItemDocument = (BsonDocument)upsertedItem;
            bulkWriteUpsertList.add(new BulkWriteUpsert(upsertedItemDocument.getNumber("index").intValue(), upsertedItemDocument.get("_id")));
        }
        return bulkWriteUpsertList;
    }
    
    private static int getCount(final BsonDocument result) {
        return result.getNumber("n").intValue();
    }
    
    private static Integer getModifiedCount(final WriteRequest.Type type, final BsonDocument result) {
        final BsonNumber modifiedCount = result.getNumber("nModified", (type == WriteRequest.Type.UPDATE || type == WriteRequest.Type.REPLACE) ? null : new BsonInt32(0));
        return (modifiedCount == null) ? null : Integer.valueOf(modifiedCount.intValue());
    }
    
    private WriteCommandResultHelper() {
    }
}
