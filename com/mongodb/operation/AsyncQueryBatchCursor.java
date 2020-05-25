package com.mongodb.operation;

import org.bson.codecs.*;
import com.mongodb.binding.*;
import com.mongodb.connection.*;
import com.mongodb.assertions.*;
import com.mongodb.async.*;
import com.mongodb.internal.validator.*;
import org.bson.*;
import java.util.*;
import com.mongodb.internal.async.*;
import com.mongodb.*;

class AsyncQueryBatchCursor<T> implements AsyncBatchCursor<T>
{
    private final MongoNamespace namespace;
    private final int limit;
    private final Decoder<T> decoder;
    private final long maxTimeMS;
    private volatile AsyncConnectionSource connectionSource;
    private volatile QueryResult<T> firstBatch;
    private volatile int batchSize;
    private volatile ServerCursor cursor;
    private volatile int count;
    private volatile boolean closed;
    
    AsyncQueryBatchCursor(final QueryResult<T> firstBatch, final int limit, final int batchSize, final Decoder<T> decoder) {
        this(firstBatch, limit, batchSize, 0L, decoder, null, null);
    }
    
    AsyncQueryBatchCursor(final QueryResult<T> firstBatch, final int limit, final int batchSize, final long maxTimeMS, final Decoder<T> decoder, final AsyncConnectionSource connectionSource, final AsyncConnection connection) {
        Assertions.isTrueArgument("maxTimeMS >= 0", maxTimeMS >= 0L);
        this.maxTimeMS = maxTimeMS;
        this.namespace = firstBatch.getNamespace();
        this.firstBatch = firstBatch;
        this.limit = limit;
        this.batchSize = batchSize;
        this.decoder = decoder;
        this.cursor = firstBatch.getCursor();
        if (this.cursor != null) {
            Assertions.notNull("connectionSource", connectionSource);
            Assertions.notNull("connection", connection);
        }
        if (connectionSource != null) {
            this.connectionSource = connectionSource.retain();
        }
        else {
            this.connectionSource = null;
        }
        this.count += firstBatch.getResults().size();
        if (this.limitReached()) {
            this.killCursor(connection);
        }
    }
    
    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.killCursor(null);
        }
    }
    
    @Override
    public void next(final SingleResultCallback<List<T>> callback) {
        Assertions.isTrue("open", !this.closed);
        if (this.firstBatch != null && !this.firstBatch.getResults().isEmpty()) {
            final List<T> results = this.firstBatch.getResults();
            this.firstBatch = null;
            callback.onResult(results, null);
        }
        else if (this.cursor == null) {
            this.close();
            callback.onResult(null, null);
        }
        else {
            this.getMore(callback);
        }
    }
    
    @Override
    public void setBatchSize(final int batchSize) {
        Assertions.isTrue("open", !this.closed);
        this.batchSize = batchSize;
    }
    
    @Override
    public int getBatchSize() {
        Assertions.isTrue("open", !this.closed);
        return this.batchSize;
    }
    
    @Override
    public boolean isClosed() {
        return this.closed;
    }
    
    ServerCursor getServerCursor() {
        return this.cursor;
    }
    
    private boolean limitReached() {
        return Math.abs(this.limit) != 0 && this.count >= Math.abs(this.limit);
    }
    
    private void getMore(final SingleResultCallback<List<T>> callback) {
        this.connectionSource.getConnection(new SingleResultCallback<AsyncConnection>() {
            @Override
            public void onResult(final AsyncConnection connection, final Throwable t) {
                if (t != null) {
                    callback.onResult(null, t);
                }
                else {
                    AsyncQueryBatchCursor.this.getMore(connection, callback);
                }
            }
        });
    }
    
    private void getMore(final AsyncConnection connection, final SingleResultCallback<List<T>> callback) {
        if (OperationHelper.serverIsAtLeastVersionThreeDotTwo(connection.getDescription())) {
            connection.commandAsync(this.namespace.getDatabaseName(), this.asGetMoreCommandDocument(), false, new NoOpFieldNameValidator(), CommandResultDocumentCodec.create(this.decoder, "nextBatch"), new CommandResultSingleResultCallback(connection, callback));
        }
        else {
            connection.getMoreAsync(this.namespace, this.cursor.getId(), CursorHelper.getNumberToReturn(this.limit, this.batchSize, this.count), this.decoder, new QueryResultSingleResultCallback(connection, callback));
        }
    }
    
    private BsonDocument asGetMoreCommandDocument() {
        final BsonDocument document = new BsonDocument("getMore", new BsonInt64(this.cursor.getId())).append("collection", new BsonString(this.namespace.getCollectionName()));
        final int batchSizeForGetMoreCommand = Math.abs(CursorHelper.getNumberToReturn(this.limit, this.batchSize, this.count));
        if (batchSizeForGetMoreCommand != 0) {
            document.append("batchSize", new BsonInt32(batchSizeForGetMoreCommand));
        }
        if (this.maxTimeMS != 0L) {
            document.append("maxTimeMS", new BsonInt64(this.maxTimeMS));
        }
        return document;
    }
    
    private void killCursor(final AsyncConnection connection) {
        if (this.cursor != null) {
            final ServerCursor localCursor = this.cursor;
            final AsyncConnectionSource localConnectionSource = this.connectionSource;
            this.cursor = null;
            this.connectionSource = null;
            if (connection != null) {
                connection.retain();
                this.killCursorAsynchronouslyAndReleaseConnectionAndSource(connection, localCursor, localConnectionSource);
            }
            else {
                localConnectionSource.getConnection(new SingleResultCallback<AsyncConnection>() {
                    @Override
                    public void onResult(final AsyncConnection connection, final Throwable connectionException) {
                        if (connectionException == null) {
                            AsyncQueryBatchCursor.this.killCursorAsynchronouslyAndReleaseConnectionAndSource(connection, localCursor, localConnectionSource);
                        }
                    }
                });
            }
        }
        else if (this.connectionSource != null) {
            this.connectionSource.release();
            this.connectionSource = null;
        }
    }
    
    private void killCursorAsynchronouslyAndReleaseConnectionAndSource(final AsyncConnection connection, final ServerCursor localCursor, final AsyncConnectionSource localConnectionSource) {
        connection.killCursorAsync(this.namespace, Collections.singletonList(localCursor.getId()), new SingleResultCallback<Void>() {
            @Override
            public void onResult(final Void result, final Throwable t) {
                connection.release();
                localConnectionSource.release();
            }
        });
    }
    
    private void handleGetMoreQueryResult(final AsyncConnection connection, final SingleResultCallback<List<T>> callback, final QueryResult<T> result) {
        if (result.getResults().isEmpty() && result.getCursor() != null) {
            this.getMore(connection, callback);
        }
        else {
            this.cursor = result.getCursor();
            this.count += result.getResults().size();
            if (this.limitReached()) {
                this.killCursor(connection);
            }
            connection.release();
            if (result.getResults().isEmpty()) {
                callback.onResult(null, null);
            }
            else {
                callback.onResult(result.getResults(), null);
            }
        }
    }
    
    private class CommandResultSingleResultCallback implements SingleResultCallback<BsonDocument>
    {
        private final AsyncConnection connection;
        private final SingleResultCallback<List<T>> callback;
        
        public CommandResultSingleResultCallback(final AsyncConnection connection, final SingleResultCallback<List<T>> callback) {
            this.connection = connection;
            this.callback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
        }
        
        @Override
        public void onResult(final BsonDocument result, final Throwable t) {
            if (t != null) {
                final Throwable translatedException = (t instanceof MongoCommandException) ? QueryHelper.translateCommandException((MongoCommandException)t, AsyncQueryBatchCursor.this.cursor) : t;
                this.connection.release();
                AsyncQueryBatchCursor.this.close();
                this.callback.onResult(null, translatedException);
            }
            else {
                final QueryResult<T> queryResult = OperationHelper.getMoreCursorDocumentToQueryResult(result.getDocument("cursor"), AsyncQueryBatchCursor.this.connectionSource.getServerDescription().getAddress());
                AsyncQueryBatchCursor.this.handleGetMoreQueryResult(this.connection, this.callback, queryResult);
            }
        }
    }
    
    private class QueryResultSingleResultCallback implements SingleResultCallback<QueryResult<T>>
    {
        private final AsyncConnection connection;
        private final SingleResultCallback<List<T>> callback;
        
        public QueryResultSingleResultCallback(final AsyncConnection connection, final SingleResultCallback<List<T>> callback) {
            this.connection = connection;
            this.callback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
        }
        
        @Override
        public void onResult(final QueryResult<T> result, final Throwable t) {
            if (t != null) {
                this.connection.release();
                AsyncQueryBatchCursor.this.close();
                this.callback.onResult(null, t);
            }
            else {
                AsyncQueryBatchCursor.this.handleGetMoreQueryResult(this.connection, this.callback, result);
            }
        }
    }
}
