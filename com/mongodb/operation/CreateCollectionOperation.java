package com.mongodb.operation;

import com.mongodb.*;
import com.mongodb.client.model.*;
import com.mongodb.assertions.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.connection.*;
import org.bson.*;

public class CreateCollectionOperation implements AsyncWriteOperation<Void>, WriteOperation<Void>
{
    private final String databaseName;
    private final String collectionName;
    private final WriteConcern writeConcern;
    private boolean capped;
    private long sizeInBytes;
    private boolean autoIndex;
    private long maxDocuments;
    private Boolean usePowerOf2Sizes;
    private BsonDocument storageEngineOptions;
    private BsonDocument indexOptionDefaults;
    private BsonDocument validator;
    private ValidationLevel validationLevel;
    private ValidationAction validationAction;
    private Collation collation;
    
    public CreateCollectionOperation(final String databaseName, final String collectionName) {
        this(databaseName, collectionName, null);
    }
    
    public CreateCollectionOperation(final String databaseName, final String collectionName, final WriteConcern writeConcern) {
        this.capped = false;
        this.sizeInBytes = 0L;
        this.autoIndex = true;
        this.maxDocuments = 0L;
        this.usePowerOf2Sizes = null;
        this.validationLevel = null;
        this.validationAction = null;
        this.collation = null;
        this.databaseName = Assertions.notNull("databaseName", databaseName);
        this.collectionName = Assertions.notNull("collectionName", collectionName);
        this.writeConcern = writeConcern;
    }
    
    public String getCollectionName() {
        return this.collectionName;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public boolean isAutoIndex() {
        return this.autoIndex;
    }
    
    public CreateCollectionOperation autoIndex(final boolean autoIndex) {
        this.autoIndex = autoIndex;
        return this;
    }
    
    public long getMaxDocuments() {
        return this.maxDocuments;
    }
    
    public CreateCollectionOperation maxDocuments(final long maxDocuments) {
        this.maxDocuments = maxDocuments;
        return this;
    }
    
    public boolean isCapped() {
        return this.capped;
    }
    
    public CreateCollectionOperation capped(final boolean capped) {
        this.capped = capped;
        return this;
    }
    
    public long getSizeInBytes() {
        return this.sizeInBytes;
    }
    
    public CreateCollectionOperation sizeInBytes(final long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
        return this;
    }
    
    @Deprecated
    public Boolean isUsePowerOf2Sizes() {
        return this.usePowerOf2Sizes;
    }
    
    @Deprecated
    public CreateCollectionOperation usePowerOf2Sizes(final Boolean usePowerOf2Sizes) {
        this.usePowerOf2Sizes = usePowerOf2Sizes;
        return this;
    }
    
    public BsonDocument getStorageEngineOptions() {
        return this.storageEngineOptions;
    }
    
    public CreateCollectionOperation storageEngineOptions(final BsonDocument storageEngineOptions) {
        this.storageEngineOptions = storageEngineOptions;
        return this;
    }
    
    public BsonDocument getIndexOptionDefaults() {
        return this.indexOptionDefaults;
    }
    
    public CreateCollectionOperation indexOptionDefaults(final BsonDocument indexOptionDefaults) {
        this.indexOptionDefaults = indexOptionDefaults;
        return this;
    }
    
    public BsonDocument getValidator() {
        return this.validator;
    }
    
    public CreateCollectionOperation validator(final BsonDocument validator) {
        this.validator = validator;
        return this;
    }
    
    public ValidationLevel getValidationLevel() {
        return this.validationLevel;
    }
    
    public CreateCollectionOperation validationLevel(final ValidationLevel validationLevel) {
        this.validationLevel = validationLevel;
        return this;
    }
    
    public ValidationAction getValidationAction() {
        return this.validationAction;
    }
    
    public CreateCollectionOperation validationAction(final ValidationAction validationAction) {
        this.validationAction = validationAction;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public CreateCollectionOperation collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    @Override
    public Void execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<Void>)new OperationHelper.CallableWithConnection<Void>() {
            @Override
            public Void call(final Connection connection) {
                OperationHelper.validateCollation(connection, CreateCollectionOperation.this.collation);
                CommandOperationHelper.executeWrappedCommandProtocol(binding, CreateCollectionOperation.this.databaseName, CreateCollectionOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer());
                return null;
            }
        });
    }
    
    @Override
    public void executeAsync(final AsyncWriteBinding binding, final SingleResultCallback<Void> callback) {
        OperationHelper.withConnection(binding, new OperationHelper.AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                final SingleResultCallback<Void> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                }
                else {
                    final SingleResultCallback<Void> wrappedCallback = OperationHelper.releasingCallback(errHandlingCallback, connection);
                    OperationHelper.validateCollation(connection, CreateCollectionOperation.this.collation, new OperationHelper.AsyncCallableWithConnection() {
                        @Override
                        public void call(final AsyncConnection connection, final Throwable t) {
                            if (t != null) {
                                wrappedCallback.onResult(null, t);
                            }
                            else {
                                CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, CreateCollectionOperation.this.databaseName, CreateCollectionOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer(), wrappedCallback);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private BsonDocument getCommand(final ConnectionDescription description) {
        final BsonDocument document = new BsonDocument("create", new BsonString(this.collectionName));
        document.put("autoIndexId", BsonBoolean.valueOf(this.autoIndex));
        document.put("capped", BsonBoolean.valueOf(this.capped));
        if (this.capped) {
            DocumentHelper.putIfNotZero(document, "size", this.sizeInBytes);
            DocumentHelper.putIfNotZero(document, "max", this.maxDocuments);
        }
        if (this.usePowerOf2Sizes != null) {
            document.put("flags", new BsonInt32(1));
        }
        if (this.storageEngineOptions != null) {
            document.put("storageEngine", this.storageEngineOptions);
        }
        if (this.indexOptionDefaults != null) {
            document.put("indexOptionDefaults", this.indexOptionDefaults);
        }
        if (this.validator != null) {
            document.put("validator", this.validator);
        }
        if (this.validationLevel != null) {
            document.put("validationLevel", new BsonString(this.validationLevel.getValue()));
        }
        if (this.validationAction != null) {
            document.put("validationAction", new BsonString(this.validationAction.getValue()));
        }
        WriteConcernHelper.appendWriteConcernToCommand(this.writeConcern, document, description);
        if (this.collation != null) {
            document.put("collation", this.collation.asDocument());
        }
        return document;
    }
}
