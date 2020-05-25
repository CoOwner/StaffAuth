package com.mongodb.operation;

import org.bson.codecs.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import com.mongodb.async.*;
import com.mongodb.binding.*;
import com.mongodb.internal.async.*;
import com.mongodb.connection.*;
import com.mongodb.*;
import org.bson.*;

public class ListDatabasesOperation<T> implements AsyncReadOperation<AsyncBatchCursor<T>>, ReadOperation<BatchCursor<T>>
{
    private final Decoder<T> decoder;
    private long maxTimeMS;
    
    public ListDatabasesOperation(final Decoder<T> decoder) {
        this.decoder = Assertions.notNull("decoder", decoder);
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public ListDatabasesOperation<T> maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    @Override
    public BatchCursor<T> execute(final ReadBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnectionAndSource<BatchCursor<T>>)new OperationHelper.CallableWithConnectionAndSource<BatchCursor<T>>() {
            @Override
            public BatchCursor<T> call(final ConnectionSource source, final Connection connection) {
                return CommandOperationHelper.executeWrappedCommandProtocol(binding, "admin", ListDatabasesOperation.this.getCommand(), CommandResultDocumentCodec.create((Decoder<Object>)ListDatabasesOperation.this.decoder, "databases"), connection, ListDatabasesOperation.this.transformer(source, connection));
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
                    CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, "admin", ListDatabasesOperation.this.getCommand(), CommandResultDocumentCodec.create((Decoder<Object>)ListDatabasesOperation.this.decoder, "databases"), connection, ListDatabasesOperation.this.asyncTransformer(source, connection), (SingleResultCallback<Object>)OperationHelper.releasingCallback((SingleResultCallback<T>)errHandlingCallback, connection));
                }
            }
        });
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, BatchCursor<T>> transformer(final ConnectionSource source, final Connection connection) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, BatchCursor<T>>() {
            @Override
            public BatchCursor<T> apply(final BsonDocument result, final ServerAddress serverAddress) {
                return new QueryBatchCursor<T>(ListDatabasesOperation.this.createQueryResult(result, connection.getDescription()), 0, 0, ListDatabasesOperation.this.decoder, source);
            }
        };
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, AsyncBatchCursor<T>> asyncTransformer(final AsyncConnectionSource source, final AsyncConnection connection) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, AsyncBatchCursor<T>>() {
            @Override
            public AsyncBatchCursor<T> apply(final BsonDocument result, final ServerAddress serverAddress) {
                return new AsyncQueryBatchCursor<T>(ListDatabasesOperation.this.createQueryResult(result, connection.getDescription()), 0, 0, 0L, ListDatabasesOperation.this.decoder, source, connection);
            }
        };
    }
    
    private QueryResult<T> createQueryResult(final BsonDocument result, final ConnectionDescription description) {
        return new QueryResult<T>(null, BsonDocumentWrapperHelper.toList(result, "databases"), 0L, description.getServerAddress());
    }
    
    private BsonDocument getCommand() {
        final BsonDocument command = new BsonDocument("listDatabases", new BsonInt32(1));
        if (this.maxTimeMS > 0L) {
            command.put("maxTimeMS", new BsonInt64(this.maxTimeMS));
        }
        return command;
    }
}
