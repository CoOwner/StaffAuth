package com.mongodb.operation;

import com.mongodb.assertions.*;
import java.util.concurrent.*;
import com.mongodb.async.*;
import com.mongodb.binding.*;
import com.mongodb.internal.async.*;
import com.mongodb.connection.*;
import org.bson.codecs.*;
import org.bson.*;
import java.util.*;
import com.mongodb.*;

public class ListCollectionsOperation<T> implements AsyncReadOperation<AsyncBatchCursor<T>>, ReadOperation<BatchCursor<T>>
{
    private final String databaseName;
    private final Decoder<T> decoder;
    private BsonDocument filter;
    private int batchSize;
    private long maxTimeMS;
    
    public ListCollectionsOperation(final String databaseName, final Decoder<T> decoder) {
        this.databaseName = Assertions.notNull("databaseName", databaseName);
        this.decoder = Assertions.notNull("decoder", decoder);
    }
    
    public BsonDocument getFilter() {
        return this.filter;
    }
    
    public ListCollectionsOperation<T> filter(final BsonDocument filter) {
        this.filter = filter;
        return this;
    }
    
    public Integer getBatchSize() {
        return this.batchSize;
    }
    
    public ListCollectionsOperation<T> batchSize(final int batchSize) {
        this.batchSize = batchSize;
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public ListCollectionsOperation<T> maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    @Override
    public BatchCursor<T> execute(final ReadBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnectionAndSource<BatchCursor<T>>)new OperationHelper.CallableWithConnectionAndSource<BatchCursor<T>>() {
            @Override
            public BatchCursor<T> call(final ConnectionSource source, final Connection connection) {
                if (OperationHelper.serverIsAtLeastVersionThreeDotZero(connection.getDescription())) {
                    try {
                        return CommandOperationHelper.executeWrappedCommandProtocol(binding, ListCollectionsOperation.this.databaseName, ListCollectionsOperation.this.getCommand(), ListCollectionsOperation.this.createCommandDecoder(), connection, ListCollectionsOperation.this.commandTransformer(source));
                    }
                    catch (MongoCommandException e) {
                        return CommandOperationHelper.rethrowIfNotNamespaceError(e, (BatchCursor<T>)OperationHelper.createEmptyBatchCursor(ListCollectionsOperation.this.createNamespace(), (Decoder<Object>)ListCollectionsOperation.this.decoder, source.getServerDescription().getAddress(), ListCollectionsOperation.this.batchSize));
                    }
                }
                return new ProjectingBatchCursor((BatchCursor)new QueryBatchCursor(connection.query(ListCollectionsOperation.this.getNamespace(), ListCollectionsOperation.this.asQueryDocument(connection.getDescription(), binding.getReadPreference()), null, 0, 0, ListCollectionsOperation.this.batchSize, binding.getReadPreference().isSlaveOk(), false, false, false, false, false, (Decoder<Object>)new BsonDocumentCodec()), 0, ListCollectionsOperation.this.batchSize, new BsonDocumentCodec(), source));
            }
        });
    }
    
    @Override
    public void executeAsync(final AsyncReadBinding binding, final SingleResultCallback<AsyncBatchCursor<T>> callback) {
        OperationHelper.withConnection(binding, new OperationHelper.AsyncCallableWithConnectionAndSource() {
            @Override
            public void call(final AsyncConnectionSource source, final AsyncConnection connection, final Throwable t) {
                final SingleResultCallback<AsyncBatchCursor<T>> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                }
                else {
                    final SingleResultCallback<AsyncBatchCursor<T>> wrappedCallback = OperationHelper.releasingCallback(errHandlingCallback, source, connection);
                    if (OperationHelper.serverIsAtLeastVersionThreeDotZero(connection.getDescription())) {
                        CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, ListCollectionsOperation.this.databaseName, ListCollectionsOperation.this.getCommand(), ListCollectionsOperation.this.createCommandDecoder(), connection, ListCollectionsOperation.this.asyncTransformer(source, connection), (SingleResultCallback<Object>)new SingleResultCallback<AsyncBatchCursor<T>>() {
                            @Override
                            public void onResult(final AsyncBatchCursor<T> result, final Throwable t) {
                                if (t != null && !CommandOperationHelper.isNamespaceError(t)) {
                                    wrappedCallback.onResult(null, t);
                                }
                                else {
                                    wrappedCallback.onResult((result != null) ? result : ListCollectionsOperation.this.emptyAsyncCursor(source), null);
                                }
                            }
                        });
                    }
                    else {
                        connection.queryAsync(ListCollectionsOperation.this.getNamespace(), ListCollectionsOperation.this.asQueryDocument(connection.getDescription(), binding.getReadPreference()), null, 0, 0, ListCollectionsOperation.this.batchSize, binding.getReadPreference().isSlaveOk(), false, false, false, false, false, (Decoder<Object>)new BsonDocumentCodec(), (SingleResultCallback<QueryResult<Object>>)new SingleResultCallback<QueryResult<BsonDocument>>() {
                            @Override
                            public void onResult(final QueryResult<BsonDocument> result, final Throwable t) {
                                if (t != null) {
                                    wrappedCallback.onResult(null, t);
                                }
                                else {
                                    wrappedCallback.onResult(new ProjectingAsyncBatchCursor((AsyncBatchCursor)new AsyncQueryBatchCursor(result, 0, ListCollectionsOperation.this.batchSize, 0L, new BsonDocumentCodec(), source, connection)), null);
                                }
                            }
                        });
                    }
                }
            }
        });
    }
    
    private AsyncBatchCursor<T> emptyAsyncCursor(final AsyncConnectionSource source) {
        return OperationHelper.createEmptyAsyncBatchCursor(this.createNamespace(), this.decoder, source.getServerDescription().getAddress(), this.batchSize);
    }
    
    private MongoNamespace createNamespace() {
        return new MongoNamespace(this.databaseName, "$cmd.listCollections");
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, AsyncBatchCursor<T>> asyncTransformer(final AsyncConnectionSource source, final AsyncConnection connection) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, AsyncBatchCursor<T>>() {
            @Override
            public AsyncBatchCursor<T> apply(final BsonDocument result, final ServerAddress serverAddress) {
                return OperationHelper.cursorDocumentToAsyncBatchCursor(result.getDocument("cursor"), ListCollectionsOperation.this.decoder, source, connection, ListCollectionsOperation.this.batchSize);
            }
        };
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, BatchCursor<T>> commandTransformer(final ConnectionSource source) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, BatchCursor<T>>() {
            @Override
            public BatchCursor<T> apply(final BsonDocument result, final ServerAddress serverAddress) {
                return OperationHelper.cursorDocumentToBatchCursor(result.getDocument("cursor"), ListCollectionsOperation.this.decoder, source, ListCollectionsOperation.this.batchSize);
            }
        };
    }
    
    private MongoNamespace getNamespace() {
        return new MongoNamespace(this.databaseName, "system.namespaces");
    }
    
    private BsonDocument getCommand() {
        final BsonDocument command = new BsonDocument("listCollections", new BsonInt32(1)).append("cursor", CursorHelper.getCursorDocumentFromBatchSize(this.batchSize));
        if (this.filter != null) {
            command.append("filter", this.filter);
        }
        if (this.maxTimeMS > 0L) {
            command.put("maxTimeMS", new BsonInt64(this.maxTimeMS));
        }
        return command;
    }
    
    private BsonDocument asQueryDocument(final ConnectionDescription connectionDescription, final ReadPreference readPreference) {
        final BsonDocument document = new BsonDocument();
        BsonDocument transformedFilter = null;
        if (this.filter != null) {
            if (this.filter.containsKey("name")) {
                if (!this.filter.isString("name")) {
                    throw new IllegalArgumentException("When filtering collections on MongoDB versions < 3.0 the name field must be a string");
                }
                transformedFilter = new BsonDocument();
                transformedFilter.putAll(this.filter);
                transformedFilter.put("name", new BsonString(String.format("%s.%s", this.databaseName, this.filter.getString("name").getValue())));
            }
            else {
                transformedFilter = this.filter;
            }
        }
        final BsonDocument indexExcludingRegex = new BsonDocument("name", new BsonRegularExpression("^[^$]*$"));
        BsonDocument bsonDocument;
        if (transformedFilter == null) {
            bsonDocument = indexExcludingRegex;
        }
        else {
            final String key;
            final BsonArray value;
            bsonDocument = new BsonDocument(key, value);
            key = "$and";
            value = new BsonArray(Arrays.asList(indexExcludingRegex, transformedFilter));
        }
        final BsonDocument query = bsonDocument;
        document.put("$query", query);
        if (connectionDescription.getServerType() == ServerType.SHARD_ROUTER && !readPreference.equals(ReadPreference.primary())) {
            document.put("$readPreference", readPreference.toDocument());
        }
        if (this.maxTimeMS > 0L) {
            document.put("$maxTimeMS", new BsonInt64(this.maxTimeMS));
        }
        return document;
    }
    
    private Codec<BsonDocument> createCommandDecoder() {
        return CommandResultDocumentCodec.create(this.decoder, "firstBatch");
    }
    
    private List<T> projectFromFullNamespaceToCollectionName(final List<BsonDocument> unstripped) {
        if (unstripped == null) {
            return null;
        }
        final List<T> stripped = new ArrayList<T>(unstripped.size());
        final String prefix = this.databaseName + ".";
        for (final BsonDocument cur : unstripped) {
            final String name = cur.getString("name").getValue();
            final String collectionName = name.substring(prefix.length());
            cur.put("name", new BsonString(collectionName));
            stripped.add(this.decoder.decode(new BsonDocumentReader(cur), DecoderContext.builder().build()));
        }
        return stripped;
    }
    
    private final class ProjectingBatchCursor implements BatchCursor<T>
    {
        private final BatchCursor<BsonDocument> delegate;
        
        private ProjectingBatchCursor(final BatchCursor<BsonDocument> delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public void remove() {
            this.delegate.remove();
        }
        
        @Override
        public void close() {
            this.delegate.close();
        }
        
        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }
        
        @Override
        public List<T> next() {
            return (List<T>)ListCollectionsOperation.this.projectFromFullNamespaceToCollectionName(this.delegate.next());
        }
        
        @Override
        public void setBatchSize(final int batchSize) {
            this.delegate.setBatchSize(batchSize);
        }
        
        @Override
        public int getBatchSize() {
            return this.delegate.getBatchSize();
        }
        
        @Override
        public List<T> tryNext() {
            return (List<T>)ListCollectionsOperation.this.projectFromFullNamespaceToCollectionName(this.delegate.tryNext());
        }
        
        @Override
        public ServerCursor getServerCursor() {
            return this.delegate.getServerCursor();
        }
        
        @Override
        public ServerAddress getServerAddress() {
            return this.delegate.getServerAddress();
        }
    }
    
    private final class ProjectingAsyncBatchCursor implements AsyncBatchCursor<T>
    {
        private final AsyncBatchCursor<BsonDocument> delegate;
        
        private ProjectingAsyncBatchCursor(final AsyncBatchCursor<BsonDocument> delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public void next(final SingleResultCallback<List<T>> callback) {
            this.delegate.next(new SingleResultCallback<List<BsonDocument>>() {
                @Override
                public void onResult(final List<BsonDocument> result, final Throwable t) {
                    if (t != null) {
                        callback.onResult(null, t);
                    }
                    else {
                        callback.onResult(ListCollectionsOperation.this.projectFromFullNamespaceToCollectionName(result), null);
                    }
                }
            });
        }
        
        @Override
        public void setBatchSize(final int batchSize) {
            this.delegate.setBatchSize(batchSize);
        }
        
        @Override
        public int getBatchSize() {
            return this.delegate.getBatchSize();
        }
        
        @Override
        public boolean isClosed() {
            return this.delegate.isClosed();
        }
        
        @Override
        public void close() {
            this.delegate.close();
        }
    }
}
