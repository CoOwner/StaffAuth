package com.mongodb.operation;

import com.mongodb.assertions.*;
import com.mongodb.bulk.*;
import java.util.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import java.util.concurrent.*;
import com.mongodb.connection.*;
import org.bson.*;
import com.mongodb.*;

public class CreateIndexesOperation implements AsyncWriteOperation<Void>, WriteOperation<Void>
{
    private final MongoNamespace namespace;
    private final List<IndexRequest> requests;
    private final WriteConcern writeConcern;
    private final MongoNamespace systemIndexes;
    
    @Deprecated
    public CreateIndexesOperation(final MongoNamespace namespace, final List<IndexRequest> requests) {
        this(namespace, requests, null);
    }
    
    public CreateIndexesOperation(final MongoNamespace namespace, final List<IndexRequest> requests, final WriteConcern writeConcern) {
        this.namespace = Assertions.notNull("namespace", namespace);
        this.systemIndexes = new MongoNamespace(namespace.getDatabaseName(), "system.indexes");
        this.requests = Assertions.notNull("indexRequests", requests);
        this.writeConcern = writeConcern;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public List<IndexRequest> getRequests() {
        return this.requests;
    }
    
    public List<String> getIndexNames() {
        final List<String> indexNames = new ArrayList<String>(this.requests.size());
        for (final IndexRequest request : this.requests) {
            if (request.getName() != null) {
                indexNames.add(request.getName());
            }
            else {
                indexNames.add(IndexHelper.generateIndexName(request.getKeys()));
            }
        }
        return indexNames;
    }
    
    @Override
    public Void execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<Void>)new OperationHelper.CallableWithConnection<Void>() {
            @Override
            public Void call(final Connection connection) {
                if (OperationHelper.serverIsAtLeastVersionTwoDotSix(connection.getDescription())) {
                    try {
                        OperationHelper.validateIndexRequestCollations(connection, CreateIndexesOperation.this.requests);
                        CommandOperationHelper.executeWrappedCommandProtocol(binding, CreateIndexesOperation.this.namespace.getDatabaseName(), CreateIndexesOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer());
                        return null;
                    }
                    catch (MongoCommandException e) {
                        throw CreateIndexesOperation.this.checkForDuplicateKeyError(e);
                    }
                }
                if (CreateIndexesOperation.this.requests.size() > 1) {
                    throw new MongoInternalException("Creation of multiple indexes simultaneously not supported until MongoDB 2.6");
                }
                connection.insert(CreateIndexesOperation.this.systemIndexes, true, WriteConcern.ACKNOWLEDGED, Arrays.asList(new InsertRequest(CreateIndexesOperation.this.getIndex(CreateIndexesOperation.this.requests.get(0)))));
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
                    if (OperationHelper.serverIsAtLeastVersionTwoDotSix(connection.getDescription())) {
                        OperationHelper.validateIndexRequestCollations(connection, CreateIndexesOperation.this.requests, new OperationHelper.AsyncCallableWithConnection() {
                            @Override
                            public void call(final AsyncConnection connection, final Throwable t) {
                                if (t != null) {
                                    wrappedCallback.onResult(null, t);
                                }
                                else {
                                    CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, CreateIndexesOperation.this.namespace.getDatabaseName(), CreateIndexesOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer(), new SingleResultCallback<Void>() {
                                        @Override
                                        public void onResult(final Void result, final Throwable t) {
                                            wrappedCallback.onResult(null, CreateIndexesOperation.this.translateException(t));
                                        }
                                    });
                                }
                            }
                        });
                    }
                    else if (CreateIndexesOperation.this.requests.size() > 1) {
                        wrappedCallback.onResult(null, new MongoInternalException("Creation of multiple indexes simultaneously not supported until MongoDB 2.6"));
                    }
                    else {
                        connection.insertAsync(CreateIndexesOperation.this.systemIndexes, true, WriteConcern.ACKNOWLEDGED, Arrays.asList(new InsertRequest(CreateIndexesOperation.this.getIndex(CreateIndexesOperation.this.requests.get(0)))), new SingleResultCallback<WriteConcernResult>() {
                            @Override
                            public void onResult(final WriteConcernResult result, final Throwable t) {
                                wrappedCallback.onResult(null, CreateIndexesOperation.this.translateException(t));
                            }
                        });
                    }
                }
            }
        });
    }
    
    private BsonDocument getIndex(final IndexRequest request) {
        final BsonDocument index = new BsonDocument();
        index.append("key", request.getKeys());
        index.append("name", new BsonString((request.getName() != null) ? request.getName() : IndexHelper.generateIndexName(request.getKeys())));
        index.append("ns", new BsonString(this.namespace.getFullName()));
        if (request.isBackground()) {
            index.append("background", BsonBoolean.TRUE);
        }
        if (request.isUnique()) {
            index.append("unique", BsonBoolean.TRUE);
        }
        if (request.isSparse()) {
            index.append("sparse", BsonBoolean.TRUE);
        }
        if (request.getExpireAfter(TimeUnit.SECONDS) != null) {
            index.append("expireAfterSeconds", new BsonInt64(request.getExpireAfter(TimeUnit.SECONDS)));
        }
        if (request.getVersion() != null) {
            index.append("v", new BsonInt32(request.getVersion()));
        }
        if (request.getWeights() != null) {
            index.append("weights", request.getWeights());
        }
        if (request.getDefaultLanguage() != null) {
            index.append("default_language", new BsonString(request.getDefaultLanguage()));
        }
        if (request.getLanguageOverride() != null) {
            index.append("language_override", new BsonString(request.getLanguageOverride()));
        }
        if (request.getTextVersion() != null) {
            index.append("textIndexVersion", new BsonInt32(request.getTextVersion()));
        }
        if (request.getSphereVersion() != null) {
            index.append("2dsphereIndexVersion", new BsonInt32(request.getSphereVersion()));
        }
        if (request.getBits() != null) {
            index.append("bits", new BsonInt32(request.getBits()));
        }
        if (request.getMin() != null) {
            index.append("min", new BsonDouble(request.getMin()));
        }
        if (request.getMax() != null) {
            index.append("max", new BsonDouble(request.getMax()));
        }
        if (request.getBucketSize() != null) {
            index.append("bucketSize", new BsonDouble(request.getBucketSize()));
        }
        if (request.getDropDups()) {
            index.append("dropDups", BsonBoolean.TRUE);
        }
        if (request.getStorageEngine() != null) {
            index.append("storageEngine", request.getStorageEngine());
        }
        if (request.getPartialFilterExpression() != null) {
            index.append("partialFilterExpression", request.getPartialFilterExpression());
        }
        if (request.getCollation() != null) {
            index.append("collation", request.getCollation().asDocument());
        }
        return index;
    }
    
    private BsonDocument getCommand(final ConnectionDescription description) {
        final BsonDocument command = new BsonDocument("createIndexes", new BsonString(this.namespace.getCollectionName()));
        final List<BsonDocument> values = new ArrayList<BsonDocument>();
        for (final IndexRequest request : this.requests) {
            values.add(this.getIndex(request));
        }
        command.put("indexes", new BsonArray(values));
        WriteConcernHelper.appendWriteConcernToCommand(this.writeConcern, command, description);
        return command;
    }
    
    private MongoException translateException(final Throwable t) {
        return (t instanceof MongoCommandException) ? this.checkForDuplicateKeyError((MongoCommandException)t) : MongoException.fromThrowable(t);
    }
    
    private MongoException checkForDuplicateKeyError(final MongoCommandException e) {
        if (ErrorCategory.fromErrorCode(e.getCode()) == ErrorCategory.DUPLICATE_KEY) {
            return new DuplicateKeyException(e.getResponse(), e.getServerAddress(), WriteConcernResult.acknowledged(0, false, null));
        }
        return e;
    }
}
