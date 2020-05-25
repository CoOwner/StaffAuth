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
import com.mongodb.internal.validator.*;
import java.util.*;

public class FindAndReplaceOperation<T> implements AsyncWriteOperation<T>, WriteOperation<T>
{
    private final MongoNamespace namespace;
    private final Decoder<T> decoder;
    private final BsonDocument replacement;
    private final WriteConcern writeConcern;
    private BsonDocument filter;
    private BsonDocument projection;
    private BsonDocument sort;
    private long maxTimeMS;
    private boolean returnOriginal;
    private boolean upsert;
    private Boolean bypassDocumentValidation;
    private Collation collation;
    
    @Deprecated
    public FindAndReplaceOperation(final MongoNamespace namespace, final Decoder<T> decoder, final BsonDocument replacement) {
        this(namespace, WriteConcern.ACKNOWLEDGED, decoder, replacement);
    }
    
    public FindAndReplaceOperation(final MongoNamespace namespace, final WriteConcern writeConcern, final Decoder<T> decoder, final BsonDocument replacement) {
        this.returnOriginal = true;
        this.namespace = Assertions.notNull("namespace", namespace);
        this.writeConcern = Assertions.notNull("writeConcern", writeConcern);
        this.decoder = Assertions.notNull("decoder", decoder);
        this.replacement = Assertions.notNull("replacement", replacement);
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
    
    public BsonDocument getReplacement() {
        return this.replacement;
    }
    
    public BsonDocument getFilter() {
        return this.filter;
    }
    
    public FindAndReplaceOperation<T> filter(final BsonDocument filter) {
        this.filter = filter;
        return this;
    }
    
    public BsonDocument getProjection() {
        return this.projection;
    }
    
    public FindAndReplaceOperation<T> projection(final BsonDocument projection) {
        this.projection = projection;
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public FindAndReplaceOperation<T> maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public BsonDocument getSort() {
        return this.sort;
    }
    
    public FindAndReplaceOperation<T> sort(final BsonDocument sort) {
        this.sort = sort;
        return this;
    }
    
    public boolean isReturnOriginal() {
        return this.returnOriginal;
    }
    
    public FindAndReplaceOperation<T> returnOriginal(final boolean returnOriginal) {
        this.returnOriginal = returnOriginal;
        return this;
    }
    
    public boolean isUpsert() {
        return this.upsert;
    }
    
    public FindAndReplaceOperation<T> upsert(final boolean upsert) {
        this.upsert = upsert;
        return this;
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public FindAndReplaceOperation<T> bypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public FindAndReplaceOperation<T> collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    @Override
    public T execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<T>)new OperationHelper.CallableWithConnection<T>() {
            @Override
            public T call(final Connection connection) {
                OperationHelper.validateCollation(connection, FindAndReplaceOperation.this.collation);
                return CommandOperationHelper.executeWrappedCommandProtocol(binding, FindAndReplaceOperation.this.namespace.getDatabaseName(), FindAndReplaceOperation.this.getCommand(connection.getDescription()), FindAndReplaceOperation.this.getValidator(), CommandResultDocumentCodec.create((Decoder<Object>)FindAndReplaceOperation.this.decoder, "value"), connection, FindAndModifyHelper.transformer());
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
                    OperationHelper.validateCollation(connection, FindAndReplaceOperation.this.collation, new OperationHelper.AsyncCallableWithConnection() {
                        @Override
                        public void call(final AsyncConnection connection, final Throwable t) {
                            if (t != null) {
                                wrappedCallback.onResult(null, t);
                            }
                            else {
                                CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, FindAndReplaceOperation.this.namespace.getDatabaseName(), FindAndReplaceOperation.this.getCommand(connection.getDescription()), FindAndReplaceOperation.this.getValidator(), CommandResultDocumentCodec.create((Decoder<Object>)FindAndReplaceOperation.this.decoder, "value"), connection, FindAndModifyHelper.transformer(), wrappedCallback);
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
        DocumentHelper.putIfTrue(commandDocument, "new", !this.isReturnOriginal());
        DocumentHelper.putIfTrue(commandDocument, "upsert", this.isUpsert());
        DocumentHelper.putIfNotZero(commandDocument, "maxTimeMS", this.getMaxTime(TimeUnit.MILLISECONDS));
        commandDocument.put("update", this.getReplacement());
        if (this.bypassDocumentValidation != null && OperationHelper.serverIsAtLeastVersionThreeDotTwo(description)) {
            commandDocument.put("bypassDocumentValidation", BsonBoolean.valueOf(this.bypassDocumentValidation));
        }
        if (OperationHelper.serverIsAtLeastVersionThreeDotTwo(description) && this.writeConcern.isAcknowledged() && !this.writeConcern.isServerDefault()) {
            commandDocument.put("writeConcern", this.writeConcern.asDocument());
        }
        if (this.collation != null) {
            commandDocument.put("collation", this.collation.asDocument());
        }
        return commandDocument;
    }
    
    private FieldNameValidator getValidator() {
        final Map<String, FieldNameValidator> map = new HashMap<String, FieldNameValidator>();
        map.put("update", new CollectibleDocumentFieldNameValidator());
        return new MappedFieldNameValidator(new NoOpFieldNameValidator(), map);
    }
}
