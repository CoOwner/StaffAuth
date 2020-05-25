package com.mongodb.operation;

import com.mongodb.*;
import java.util.*;
import com.mongodb.client.model.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.connection.*;
import com.mongodb.internal.async.*;
import org.bson.*;

class AggregateExplainOperation implements AsyncReadOperation<BsonDocument>, ReadOperation<BsonDocument>
{
    private final MongoNamespace namespace;
    private final List<BsonDocument> pipeline;
    private Boolean allowDiskUse;
    private long maxTimeMS;
    private Collation collation;
    
    public AggregateExplainOperation(final MongoNamespace namespace, final List<BsonDocument> pipeline) {
        this.namespace = Assertions.notNull("namespace", namespace);
        this.pipeline = Assertions.notNull("pipeline", pipeline);
    }
    
    public AggregateExplainOperation allowDiskUse(final Boolean allowDiskUse) {
        this.allowDiskUse = allowDiskUse;
        return this;
    }
    
    public AggregateExplainOperation maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public AggregateExplainOperation collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    @Override
    public BsonDocument execute(final ReadBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<BsonDocument>)new OperationHelper.CallableWithConnection<BsonDocument>() {
            @Override
            public BsonDocument call(final Connection connection) {
                OperationHelper.validateCollation(connection, AggregateExplainOperation.this.collation);
                return CommandOperationHelper.executeWrappedCommandProtocol(binding, AggregateExplainOperation.this.namespace.getDatabaseName(), AggregateExplainOperation.this.getCommand(), connection);
            }
        });
    }
    
    @Override
    public void executeAsync(final AsyncReadBinding binding, final SingleResultCallback<BsonDocument> callback) {
        OperationHelper.withConnection(binding, new OperationHelper.AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                final SingleResultCallback<BsonDocument> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                }
                else {
                    final SingleResultCallback<BsonDocument> wrappedCallback = OperationHelper.releasingCallback(errHandlingCallback, connection);
                    OperationHelper.validateCollation(connection, AggregateExplainOperation.this.collation, new OperationHelper.AsyncCallableWithConnection() {
                        @Override
                        public void call(final AsyncConnection connection, final Throwable t) {
                            if (t != null) {
                                wrappedCallback.onResult(null, t);
                            }
                            else {
                                CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, AggregateExplainOperation.this.namespace.getDatabaseName(), AggregateExplainOperation.this.getCommand(), connection, (CommandOperationHelper.CommandTransformer<BsonDocument, Object>)new CommandOperationHelper.IdentityTransformer(), wrappedCallback);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private BsonDocument getCommand() {
        final BsonDocument commandDocument = new BsonDocument("aggregate", new BsonString(this.namespace.getCollectionName()));
        commandDocument.put("pipeline", new BsonArray(this.pipeline));
        commandDocument.put("explain", BsonBoolean.TRUE);
        if (this.maxTimeMS > 0L) {
            commandDocument.put("maxTimeMS", new BsonInt64(this.maxTimeMS));
        }
        if (this.allowDiskUse != null) {
            commandDocument.put("allowDiskUse", BsonBoolean.valueOf(this.allowDiskUse));
        }
        if (this.collation != null) {
            commandDocument.put("collation", this.collation.asDocument());
        }
        return commandDocument;
    }
}
