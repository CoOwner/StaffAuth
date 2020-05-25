package com.mongodb;

import java.util.*;
import org.bson.codecs.*;
import org.bson.*;
import com.mongodb.bulk.*;

final class BulkWriteHelper
{
    static com.mongodb.BulkWriteResult translateBulkWriteResult(final BulkWriteResult bulkWriteResult, final Decoder<DBObject> decoder) {
        if (bulkWriteResult.wasAcknowledged()) {
            final Integer modifiedCount = bulkWriteResult.isModifiedCountAvailable() ? Integer.valueOf(bulkWriteResult.getModifiedCount()) : null;
            return new AcknowledgedBulkWriteResult(bulkWriteResult.getInsertedCount(), bulkWriteResult.getMatchedCount(), bulkWriteResult.getDeletedCount(), modifiedCount, translateBulkWriteUpserts(bulkWriteResult.getUpserts(), decoder));
        }
        return new UnacknowledgedBulkWriteResult();
    }
    
    static List<com.mongodb.BulkWriteUpsert> translateBulkWriteUpserts(final List<BulkWriteUpsert> upserts, final Decoder<DBObject> decoder) {
        final List<com.mongodb.BulkWriteUpsert> retVal = new ArrayList<com.mongodb.BulkWriteUpsert>(upserts.size());
        for (final BulkWriteUpsert cur : upserts) {
            retVal.add(new com.mongodb.BulkWriteUpsert(cur.getIndex(), getUpsertedId(cur, decoder)));
        }
        return retVal;
    }
    
    private static Object getUpsertedId(final BulkWriteUpsert cur, final Decoder<DBObject> decoder) {
        return decoder.decode(new BsonDocumentReader(new BsonDocument("_id", cur.getId())), DecoderContext.builder().build()).get("_id");
    }
    
    static BulkWriteException translateBulkWriteException(final MongoBulkWriteException e, final Decoder<DBObject> decoder) {
        return new BulkWriteException(translateBulkWriteResult(e.getWriteResult(), decoder), translateWriteErrors(e.getWriteErrors()), translateWriteConcernError(e.getWriteConcernError()), e.getServerAddress());
    }
    
    static com.mongodb.WriteConcernError translateWriteConcernError(final WriteConcernError writeConcernError) {
        return (writeConcernError == null) ? null : new com.mongodb.WriteConcernError(writeConcernError.getCode(), writeConcernError.getMessage(), DBObjects.toDBObject(writeConcernError.getDetails()));
    }
    
    static List<com.mongodb.BulkWriteError> translateWriteErrors(final List<BulkWriteError> errors) {
        final List<com.mongodb.BulkWriteError> retVal = new ArrayList<com.mongodb.BulkWriteError>(errors.size());
        for (final BulkWriteError cur : errors) {
            retVal.add(new com.mongodb.BulkWriteError(cur.getCode(), cur.getMessage(), DBObjects.toDBObject(cur.getDetails()), cur.getIndex()));
        }
        return retVal;
    }
    
    static List<com.mongodb.bulk.WriteRequest> translateWriteRequestsToNew(final List<WriteRequest> writeRequests) {
        final List<com.mongodb.bulk.WriteRequest> retVal = new ArrayList<com.mongodb.bulk.WriteRequest>(writeRequests.size());
        for (final WriteRequest cur : writeRequests) {
            retVal.add(cur.toNew());
        }
        return retVal;
    }
    
    private BulkWriteHelper() {
    }
}
