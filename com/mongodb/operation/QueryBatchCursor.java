package com.mongodb.operation;

import org.bson.codecs.*;
import com.mongodb.binding.*;
import com.mongodb.connection.*;
import com.mongodb.assertions.*;
import com.mongodb.internal.validator.*;
import com.mongodb.*;
import org.bson.*;
import java.util.*;

class QueryBatchCursor<T> implements BatchCursor<T>
{
    private final MongoNamespace namespace;
    private final int limit;
    private final Decoder<T> decoder;
    private final ConnectionSource connectionSource;
    private final long maxTimeMS;
    private int batchSize;
    private ServerCursor serverCursor;
    private List<T> nextBatch;
    private int count;
    private boolean closed;
    
    QueryBatchCursor(final QueryResult<T> firstQueryResult, final int limit, final int batchSize, final Decoder<T> decoder) {
        this(firstQueryResult, limit, batchSize, decoder, null);
    }
    
    QueryBatchCursor(final QueryResult<T> firstQueryResult, final int limit, final int batchSize, final Decoder<T> decoder, final ConnectionSource connectionSource) {
        this(firstQueryResult, limit, batchSize, 0L, decoder, connectionSource, null);
    }
    
    QueryBatchCursor(final QueryResult<T> firstQueryResult, final int limit, final int batchSize, final long maxTimeMS, final Decoder<T> decoder, final ConnectionSource connectionSource, final Connection connection) {
        Assertions.isTrueArgument("maxTimeMS >= 0", maxTimeMS >= 0L);
        this.maxTimeMS = maxTimeMS;
        this.namespace = firstQueryResult.getNamespace();
        this.limit = limit;
        this.batchSize = batchSize;
        this.decoder = Assertions.notNull("decoder", decoder);
        if (firstQueryResult.getCursor() != null) {
            Assertions.notNull("connectionSource", connectionSource);
        }
        if (connectionSource != null) {
            this.connectionSource = connectionSource.retain();
        }
        else {
            this.connectionSource = null;
        }
        this.initFromQueryResult(firstQueryResult);
        if (this.limitReached()) {
            this.killCursor(connection);
        }
    }
    
    @Override
    public boolean hasNext() {
        if (this.closed) {
            throw new IllegalStateException("Cursor has been closed");
        }
        if (this.nextBatch != null) {
            return true;
        }
        if (this.limitReached()) {
            return false;
        }
        while (this.serverCursor != null) {
            this.getMore();
            if (this.nextBatch != null) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<T> next() {
        if (this.closed) {
            throw new IllegalStateException("Iterator has been closed");
        }
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        final List<T> retVal = this.nextBatch;
        this.nextBatch = null;
        return retVal;
    }
    
    @Override
    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }
    
    @Override
    public int getBatchSize() {
        return this.batchSize;
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
    
    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            try {
                this.killCursor();
            }
            finally {
                if (this.connectionSource != null) {
                    this.connectionSource.release();
                }
            }
        }
    }
    
    @Override
    public List<T> tryNext() {
        if (this.closed) {
            throw new IllegalStateException("Cursor has been closed");
        }
        if (!this.tryHasNext()) {
            return null;
        }
        return this.next();
    }
    
    boolean tryHasNext() {
        if (this.nextBatch != null) {
            return true;
        }
        if (this.limitReached()) {
            return false;
        }
        if (this.serverCursor != null) {
            this.getMore();
        }
        return this.nextBatch != null;
    }
    
    @Override
    public ServerCursor getServerCursor() {
        if (this.closed) {
            throw new IllegalStateException("Iterator has been closed");
        }
        return this.serverCursor;
    }
    
    @Override
    public ServerAddress getServerAddress() {
        if (this.closed) {
            throw new IllegalStateException("Iterator has been closed");
        }
        return this.connectionSource.getServerDescription().getAddress();
    }
    
    private void getMore() {
        final Connection connection = this.connectionSource.getConnection();
        try {
            Label_0116: {
                if (OperationHelper.serverIsAtLeastVersionThreeDotTwo(connection.getDescription())) {
                    try {
                        this.initFromCommandResult(connection.command(this.namespace.getDatabaseName(), this.asGetMoreCommandDocument(), false, new NoOpFieldNameValidator(), CommandResultDocumentCodec.create(this.decoder, "nextBatch")));
                        break Label_0116;
                    }
                    catch (MongoCommandException e) {
                        throw QueryHelper.translateCommandException(e, this.serverCursor);
                    }
                }
                this.initFromQueryResult(connection.getMore(this.namespace, this.serverCursor.getId(), CursorHelper.getNumberToReturn(this.limit, this.batchSize, this.count), this.decoder));
            }
            if (this.limitReached()) {
                this.killCursor(connection);
            }
        }
        finally {
            connection.release();
        }
    }
    
    private BsonDocument asGetMoreCommandDocument() {
        final BsonDocument document = new BsonDocument("getMore", new BsonInt64(this.serverCursor.getId())).append("collection", new BsonString(this.namespace.getCollectionName()));
        final int batchSizeForGetMoreCommand = Math.abs(CursorHelper.getNumberToReturn(this.limit, this.batchSize, this.count));
        if (batchSizeForGetMoreCommand != 0) {
            document.append("batchSize", new BsonInt32(batchSizeForGetMoreCommand));
        }
        if (this.maxTimeMS != 0L) {
            document.append("maxTimeMS", new BsonInt64(this.maxTimeMS));
        }
        return document;
    }
    
    private void initFromQueryResult(final QueryResult<T> queryResult) {
        this.serverCursor = queryResult.getCursor();
        this.nextBatch = (queryResult.getResults().isEmpty() ? null : queryResult.getResults());
        this.count += queryResult.getResults().size();
    }
    
    private void initFromCommandResult(final BsonDocument getMoreCommandResultDocument) {
        final QueryResult<T> queryResult = OperationHelper.getMoreCursorDocumentToQueryResult(getMoreCommandResultDocument.getDocument("cursor"), this.connectionSource.getServerDescription().getAddress());
        this.initFromQueryResult(queryResult);
    }
    
    private boolean limitReached() {
        return Math.abs(this.limit) != 0 && this.count >= Math.abs(this.limit);
    }
    
    private void killCursor() {
        if (this.serverCursor != null) {
            final Connection connection = this.connectionSource.getConnection();
            try {
                this.killCursor(connection);
            }
            finally {
                connection.release();
            }
        }
    }
    
    private void killCursor(final Connection connection) {
        if (this.serverCursor != null) {
            Assertions.notNull("connection", connection);
            connection.killCursor(this.namespace, Collections.singletonList(this.serverCursor.getId()));
            this.serverCursor = null;
        }
    }
}
