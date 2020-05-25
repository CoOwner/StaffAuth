package com.mongodb.operation;

import org.bson.codecs.*;
import com.mongodb.assertions.*;
import com.mongodb.async.*;
import com.mongodb.binding.*;
import com.mongodb.internal.async.*;
import com.mongodb.*;
import com.mongodb.connection.*;
import java.util.*;
import org.bson.*;

public class ParallelCollectionScanOperation<T> implements AsyncReadOperation<List<AsyncBatchCursor<T>>>, ReadOperation<List<BatchCursor<T>>>
{
    private final MongoNamespace namespace;
    private final int numCursors;
    private int batchSize;
    private final Decoder<T> decoder;
    private ReadConcern readConcern;
    
    public ParallelCollectionScanOperation(final MongoNamespace namespace, final int numCursors, final Decoder<T> decoder) {
        this.batchSize = 0;
        this.readConcern = ReadConcern.DEFAULT;
        this.namespace = Assertions.notNull("namespace", namespace);
        Assertions.isTrue("numCursors >= 1", numCursors >= 1);
        this.numCursors = numCursors;
        this.decoder = Assertions.notNull("decoder", decoder);
    }
    
    public int getNumCursors() {
        return this.numCursors;
    }
    
    public int getBatchSize() {
        return this.batchSize;
    }
    
    public ParallelCollectionScanOperation<T> batchSize(final int batchSize) {
        Assertions.isTrue("batchSize >= 0", batchSize >= 0);
        this.batchSize = batchSize;
        return this;
    }
    
    public ReadConcern getReadConcern() {
        return this.readConcern;
    }
    
    public ParallelCollectionScanOperation<T> readConcern(final ReadConcern readConcern) {
        this.readConcern = Assertions.notNull("readConcern", readConcern);
        return this;
    }
    
    @Override
    public List<BatchCursor<T>> execute(final ReadBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnectionAndSource<List<BatchCursor<T>>>)new OperationHelper.CallableWithConnectionAndSource<List<BatchCursor<T>>>() {
            @Override
            public List<BatchCursor<T>> call(final ConnectionSource source, final Connection connection) {
                OperationHelper.validateReadConcern(connection, ParallelCollectionScanOperation.this.readConcern);
                return CommandOperationHelper.executeWrappedCommandProtocol(binding, ParallelCollectionScanOperation.this.namespace.getDatabaseName(), ParallelCollectionScanOperation.this.getCommand(), CommandResultDocumentCodec.create((Decoder<Object>)ParallelCollectionScanOperation.this.decoder, "firstBatch"), connection, ParallelCollectionScanOperation.this.transformer(source));
            }
        });
    }
    
    @Override
    public void executeAsync(final AsyncReadBinding binding, final SingleResultCallback<List<AsyncBatchCursor<T>>> callback) {
        OperationHelper.withConnection(binding, new OperationHelper.AsyncCallableWithConnectionAndSource() {
            @Override
            public void call(final AsyncConnectionSource source, final AsyncConnection connection, final Throwable t) {
                final SingleResultCallback<List<AsyncBatchCursor<T>>> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                }
                else {
                    final SingleResultCallback<List<AsyncBatchCursor<T>>> wrappedCallback = OperationHelper.releasingCallback(errHandlingCallback, source, connection);
                    OperationHelper.validateReadConcern(source, connection, ParallelCollectionScanOperation.this.readConcern, new OperationHelper.AsyncCallableWithConnectionAndSource() {
                        @Override
                        public void call(final AsyncConnectionSource source, final AsyncConnection connection, final Throwable t) {
                            if (t != null) {
                                wrappedCallback.onResult(null, t);
                            }
                            else {
                                CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, ParallelCollectionScanOperation.this.namespace.getDatabaseName(), ParallelCollectionScanOperation.this.getCommand(), CommandResultDocumentCodec.create((Decoder<Object>)ParallelCollectionScanOperation.this.decoder, "firstBatch"), connection, ParallelCollectionScanOperation.this.asyncTransformer(source, connection), (SingleResultCallback<Object>)wrappedCallback);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, List<BatchCursor<T>>> transformer(final ConnectionSource source) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, List<BatchCursor<T>>>() {
            @Override
            public List<BatchCursor<T>> apply(final BsonDocument result, final ServerAddress serverAddress) {
                final List<BatchCursor<T>> cursors = new ArrayList<BatchCursor<T>>();
                for (final BsonValue cursorValue : ParallelCollectionScanOperation.this.getCursorDocuments(result)) {
                    cursors.add(new QueryBatchCursor<T>(ParallelCollectionScanOperation.this.createQueryResult(ParallelCollectionScanOperation.this.getCursorDocument(cursorValue.asDocument()), source.getServerDescription().getAddress()), 0, ParallelCollectionScanOperation.this.getBatchSize(), ParallelCollectionScanOperation.this.decoder, source));
                }
                return cursors;
            }
        };
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, List<AsyncBatchCursor<T>>> asyncTransformer(final AsyncConnectionSource source, final AsyncConnection connection) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, List<AsyncBatchCursor<T>>>() {
            @Override
            public List<AsyncBatchCursor<T>> apply(final BsonDocument result, final ServerAddress serverAddress) {
                final List<AsyncBatchCursor<T>> cursors = new ArrayList<AsyncBatchCursor<T>>();
                for (final BsonValue cursorValue : ParallelCollectionScanOperation.this.getCursorDocuments(result)) {
                    cursors.add(new AsyncQueryBatchCursor<T>(ParallelCollectionScanOperation.this.createQueryResult(ParallelCollectionScanOperation.this.getCursorDocument(cursorValue.asDocument()), source.getServerDescription().getAddress()), 0, ParallelCollectionScanOperation.this.getBatchSize(), 0L, ParallelCollectionScanOperation.this.decoder, source, connection));
                }
                return cursors;
            }
        };
    }
    
    private BsonArray getCursorDocuments(final BsonDocument result) {
        return result.getArray("cursors");
    }
    
    private BsonDocument getCursorDocument(final BsonDocument cursorDocument) {
        return cursorDocument.getDocument("cursor");
    }
    
    private QueryResult<T> createQueryResult(final BsonDocument cursorDocument, final ServerAddress serverAddress) {
        return OperationHelper.cursorDocumentToQueryResult(cursorDocument, serverAddress);
    }
    
    private BsonDocument getCommand() {
        final BsonDocument document = new BsonDocument("parallelCollectionScan", new BsonString(this.namespace.getCollectionName())).append("numCursors", new BsonInt32(this.getNumCursors()));
        if (!this.readConcern.isServerDefault()) {
            document.put("readConcern", this.readConcern.asDocument());
        }
        return document;
    }
}
