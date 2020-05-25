package com.mongodb.operation;

import org.bson.codecs.*;
import com.mongodb.*;
import com.mongodb.client.model.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.connection.*;
import org.bson.*;

public class FindAndDeleteOperation<T> implements AsyncWriteOperation<T>, WriteOperation<T>
{
    private final MongoNamespace namespace;
    private final Decoder<T> decoder;
    private BsonDocument filter;
    private BsonDocument projection;
    private BsonDocument sort;
    private long maxTimeMS;
    private WriteConcern writeConcern;
    private Collation collation;
    
    @Deprecated
    public FindAndDeleteOperation(final MongoNamespace namespace, final Decoder<T> decoder) {
        this(namespace, WriteConcern.ACKNOWLEDGED, decoder);
    }
    
    public FindAndDeleteOperation(final MongoNamespace namespace, final WriteConcern writeConcern, final Decoder<T> decoder) {
        this.namespace = Assertions.notNull("namespace", namespace);
        this.writeConcern = Assertions.notNull("writeConcern", writeConcern);
        this.decoder = Assertions.notNull("decoder", decoder);
    }
    
    public MongoNamespace getNamespace() {
        return this.namespace;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public Decoder<T> getDecoder() {
        return this.decoder;
    }
    
    public BsonDocument getFilter() {
        return this.filter;
    }
    
    public FindAndDeleteOperation<T> filter(final BsonDocument filter) {
        this.filter = filter;
        return this;
    }
    
    public BsonDocument getProjection() {
        return this.projection;
    }
    
    public FindAndDeleteOperation<T> projection(final BsonDocument projection) {
        this.projection = projection;
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public FindAndDeleteOperation<T> maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public BsonDocument getSort() {
        return this.sort;
    }
    
    public FindAndDeleteOperation<T> sort(final BsonDocument sort) {
        this.sort = sort;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public FindAndDeleteOperation<T> collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    @Override
    public T execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<T>)new OperationHelper.CallableWithConnection<T>() {
            @Override
            public T call(final Connection connection) {
                OperationHelper.validateCollation(connection, FindAndDeleteOperation.this.collation);
                return CommandOperationHelper.executeWrappedCommandProtocol(binding, FindAndDeleteOperation.this.namespace.getDatabaseName(), FindAndDeleteOperation.this.getCommand(connection.getDescription()), CommandResultDocumentCodec.create((Decoder<Object>)FindAndDeleteOperation.this.decoder, "value"), connection, FindAndModifyHelper.transformer());
            }
        });
    }
    
    @Override
    public void executeAsync(final AsyncWriteBinding binding, final SingleResultCallback<T> callback) {
        OperationHelper.withConnection(binding, new OperationHelper.AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                final SingleResultCallback<T> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                }
                else {
                    final SingleResultCallback<T> wrappedCallback = OperationHelper.releasingCallback(errHandlingCallback, connection);
                    OperationHelper.validateCollation(connection, FindAndDeleteOperation.this.collation, new OperationHelper.AsyncCallableWithConnection() {
                        @Override
                        public void call(final AsyncConnection connection, final Throwable t) {
                            if (t != null) {
                                wrappedCallback.onResult(null, t);
                            }
                            else {
                                CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, FindAndDeleteOperation.this.namespace.getDatabaseName(), FindAndDeleteOperation.this.getCommand(connection.getDescription()), CommandResultDocumentCodec.create((Decoder<Object>)FindAndDeleteOperation.this.decoder, "value"), connection, FindAndModifyHelper.transformer(), wrappedCallback);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private BsonDocument getCommand(final ConnectionDescription description) {
        final BsonDocument commandDocument = new BsonDocument("findandmodify", new BsonString(this.namespace.getCollectionName()));
        DocumentHelper.putIfNotNull(commandDocument, "query", this.getFilter());
        DocumentHelper.putIfNotNull(commandDocument, "fields", this.getProjection());
        DocumentHelper.putIfNotNull(commandDocument, "sort", this.getSort());
        DocumentHelper.putIfNotZero(commandDocument, "maxTimeMS", this.getMaxTime(TimeUnit.MILLISECONDS));
        commandDocument.put("remove", BsonBoolean.TRUE);
        if (OperationHelper.serverIsAtLeastVersionThreeDotTwo(description) && this.writeConcern.isAcknowledged() && !this.writeConcern.isServerDefault()) {
            commandDocument.put("writeConcern", this.writeConcern.asDocument());
        }
        if (this.collation != null) {
            commandDocument.put("collation", this.collation.asDocument());
        }
        return commandDocument;
    }
}
