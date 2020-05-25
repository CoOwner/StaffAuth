package com.mongodb.connection;

import com.mongodb.assertions.*;
import com.mongodb.bulk.*;
import com.mongodb.internal.connection.*;
import com.mongodb.*;
import java.util.*;

public class BulkWriteBatchCombiner
{
    private final ServerAddress serverAddress;
    private final boolean ordered;
    private final WriteConcern writeConcern;
    private int insertedCount;
    private int matchedCount;
    private int deletedCount;
    private Integer modifiedCount;
    private final Set<BulkWriteUpsert> writeUpserts;
    private final Set<BulkWriteError> writeErrors;
    private final List<WriteConcernError> writeConcernErrors;
    
    public BulkWriteBatchCombiner(final ServerAddress serverAddress, final boolean ordered, final WriteConcern writeConcern) {
        this.modifiedCount = 0;
        this.writeUpserts = new TreeSet<BulkWriteUpsert>(new Comparator<BulkWriteUpsert>() {
            @Override
            public int compare(final BulkWriteUpsert o1, final BulkWriteUpsert o2) {
                return (o1.getIndex() < o2.getIndex()) ? -1 : ((o1.getIndex() == o2.getIndex()) ? 0 : 1);
            }
        });
        this.writeErrors = new TreeSet<BulkWriteError>(new Comparator<BulkWriteError>() {
            @Override
            public int compare(final BulkWriteError o1, final BulkWriteError o2) {
                return (o1.getIndex() < o2.getIndex()) ? -1 : ((o1.getIndex() == o2.getIndex()) ? 0 : 1);
            }
        });
        this.writeConcernErrors = new ArrayList<WriteConcernError>();
        this.writeConcern = Assertions.notNull("writeConcern", writeConcern);
        this.ordered = ordered;
        this.serverAddress = Assertions.notNull("serverAddress", serverAddress);
    }
    
    public void addResult(final BulkWriteResult result, final IndexMap indexMap) {
        this.insertedCount += result.getInsertedCount();
        this.matchedCount += result.getMatchedCount();
        this.deletedCount += result.getDeletedCount();
        if (result.isModifiedCountAvailable() && this.modifiedCount != null) {
            this.modifiedCount += result.getModifiedCount();
        }
        else {
            this.modifiedCount = null;
        }
        this.mergeUpserts(result.getUpserts(), indexMap);
    }
    
    public void addErrorResult(final MongoBulkWriteException exception, final IndexMap indexMap) {
        this.addResult(exception.getWriteResult(), indexMap);
        this.mergeWriteErrors(exception.getWriteErrors(), indexMap);
        this.mergeWriteConcernError(exception.getWriteConcernError());
    }
    
    public void addWriteErrorResult(final BulkWriteError writeError, final IndexMap indexMap) {
        Assertions.notNull("writeError", writeError);
        this.mergeWriteErrors(Arrays.asList(writeError), indexMap);
    }
    
    public void addWriteConcernErrorResult(final WriteConcernError writeConcernError) {
        Assertions.notNull("writeConcernError", writeConcernError);
        this.mergeWriteConcernError(writeConcernError);
    }
    
    public void addErrorResult(final List<BulkWriteError> writeErrors, final WriteConcernError writeConcernError, final IndexMap indexMap) {
        this.mergeWriteErrors(writeErrors, indexMap);
        this.mergeWriteConcernError(writeConcernError);
    }
    
    public BulkWriteResult getResult() {
        this.throwOnError();
        return this.createResult();
    }
    
    public boolean shouldStopSendingMoreBatches() {
        return this.ordered && this.hasWriteErrors();
    }
    
    public boolean hasErrors() {
        return this.hasWriteErrors() || this.hasWriteConcernErrors();
    }
    
    public MongoBulkWriteException getError() {
        return this.hasErrors() ? new MongoBulkWriteException(this.createResult(), new ArrayList<BulkWriteError>(this.writeErrors), this.writeConcernErrors.isEmpty() ? null : this.writeConcernErrors.get(this.writeConcernErrors.size() - 1), this.serverAddress) : null;
    }
    
    private void mergeWriteConcernError(final WriteConcernError writeConcernError) {
        if (writeConcernError != null) {
            if (this.writeConcernErrors.isEmpty()) {
                this.writeConcernErrors.add(writeConcernError);
            }
            else if (!writeConcernError.equals(this.writeConcernErrors.get(this.writeConcernErrors.size() - 1))) {
                this.writeConcernErrors.add(writeConcernError);
            }
        }
    }
    
    private void mergeWriteErrors(final List<BulkWriteError> newWriteErrors, final IndexMap indexMap) {
        for (final BulkWriteError cur : newWriteErrors) {
            this.writeErrors.add(new BulkWriteError(cur.getCode(), cur.getMessage(), cur.getDetails(), indexMap.map(cur.getIndex())));
        }
    }
    
    private void mergeUpserts(final List<BulkWriteUpsert> upserts, final IndexMap indexMap) {
        for (final BulkWriteUpsert bulkWriteUpsert : upserts) {
            this.writeUpserts.add(new BulkWriteUpsert(indexMap.map(bulkWriteUpsert.getIndex()), bulkWriteUpsert.getId()));
        }
    }
    
    private void throwOnError() {
        if (this.hasErrors()) {
            throw this.getError();
        }
    }
    
    private BulkWriteResult createResult() {
        return this.writeConcern.isAcknowledged() ? BulkWriteResult.acknowledged(this.insertedCount, this.matchedCount, this.deletedCount, this.modifiedCount, new ArrayList<BulkWriteUpsert>(this.writeUpserts)) : BulkWriteResult.unacknowledged();
    }
    
    private boolean hasWriteErrors() {
        return !this.writeErrors.isEmpty();
    }
    
    private boolean hasWriteConcernErrors() {
        return !this.writeConcernErrors.isEmpty();
    }
}
