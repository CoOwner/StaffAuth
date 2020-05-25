package com.mongodb.operation;

import com.mongodb.client.model.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.connection.*;
import org.bson.codecs.*;
import com.mongodb.*;
import org.bson.*;
import java.util.*;

public class MapReduceToCollectionOperation implements AsyncWriteOperation<MapReduceStatistics>, WriteOperation<MapReduceStatistics>
{
    private final MongoNamespace namespace;
    private final BsonJavaScript mapFunction;
    private final BsonJavaScript reduceFunction;
    private final String collectionName;
    private final WriteConcern writeConcern;
    private BsonJavaScript finalizeFunction;
    private BsonDocument scope;
    private BsonDocument filter;
    private BsonDocument sort;
    private int limit;
    private boolean jsMode;
    private boolean verbose;
    private long maxTimeMS;
    private String action;
    private String databaseName;
    private boolean sharded;
    private boolean nonAtomic;
    private Boolean bypassDocumentValidation;
    private Collation collation;
    private static final List<String> VALID_ACTIONS;
    
    public MapReduceToCollectionOperation(final MongoNamespace namespace, final BsonJavaScript mapFunction, final BsonJavaScript reduceFunction, final String collectionName) {
        this(namespace, mapFunction, reduceFunction, collectionName, null);
    }
    
    public MapReduceToCollectionOperation(final MongoNamespace namespace, final BsonJavaScript mapFunction, final BsonJavaScript reduceFunction, final String collectionName, final WriteConcern writeConcern) {
        this.action = "replace";
        this.namespace = Assertions.notNull("namespace", namespace);
        this.mapFunction = Assertions.notNull("mapFunction", mapFunction);
        this.reduceFunction = Assertions.notNull("reduceFunction", reduceFunction);
        this.collectionName = Assertions.notNull("collectionName", collectionName);
        this.writeConcern = writeConcern;
    }
    
    public MongoNamespace getNamespace() {
        return this.namespace;
    }
    
    public BsonJavaScript getMapFunction() {
        return this.mapFunction;
    }
    
    public BsonJavaScript getReduceFunction() {
        return this.reduceFunction;
    }
    
    public String getCollectionName() {
        return this.collectionName;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public BsonJavaScript getFinalizeFunction() {
        return this.finalizeFunction;
    }
    
    public MapReduceToCollectionOperation finalizeFunction(final BsonJavaScript finalizeFunction) {
        this.finalizeFunction = finalizeFunction;
        return this;
    }
    
    public BsonDocument getScope() {
        return this.scope;
    }
    
    public MapReduceToCollectionOperation scope(final BsonDocument scope) {
        this.scope = scope;
        return this;
    }
    
    public BsonDocument getFilter() {
        return this.filter;
    }
    
    public MapReduceToCollectionOperation filter(final BsonDocument filter) {
        this.filter = filter;
        return this;
    }
    
    public BsonDocument getSort() {
        return this.sort;
    }
    
    public MapReduceToCollectionOperation sort(final BsonDocument sort) {
        this.sort = sort;
        return this;
    }
    
    public int getLimit() {
        return this.limit;
    }
    
    public MapReduceToCollectionOperation limit(final int limit) {
        this.limit = limit;
        return this;
    }
    
    public boolean isJsMode() {
        return this.jsMode;
    }
    
    public MapReduceToCollectionOperation jsMode(final boolean jsMode) {
        this.jsMode = jsMode;
        return this;
    }
    
    public boolean isVerbose() {
        return this.verbose;
    }
    
    public MapReduceToCollectionOperation verbose(final boolean verbose) {
        this.verbose = verbose;
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public MapReduceToCollectionOperation maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public String getAction() {
        return this.action;
    }
    
    public MapReduceToCollectionOperation action(final String action) {
        Assertions.notNull("action", action);
        Assertions.isTrue("action must be one of: \"replace\", \"merge\", \"reduce\"", MapReduceToCollectionOperation.VALID_ACTIONS.contains(action));
        this.action = action;
        return this;
    }
    
    public String getDatabaseName() {
        return this.databaseName;
    }
    
    public MapReduceToCollectionOperation databaseName(final String databaseName) {
        this.databaseName = databaseName;
        return this;
    }
    
    public boolean isSharded() {
        return this.sharded;
    }
    
    public MapReduceToCollectionOperation sharded(final boolean sharded) {
        this.sharded = sharded;
        return this;
    }
    
    public boolean isNonAtomic() {
        return this.nonAtomic;
    }
    
    public MapReduceToCollectionOperation nonAtomic(final boolean nonAtomic) {
        this.nonAtomic = nonAtomic;
        return this;
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public MapReduceToCollectionOperation bypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public MapReduceToCollectionOperation collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    @Override
    public MapReduceStatistics execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<MapReduceStatistics>)new OperationHelper.CallableWithConnection<MapReduceStatistics>() {
            @Override
            public MapReduceStatistics call(final Connection connection) {
                OperationHelper.validateCollation(connection, MapReduceToCollectionOperation.this.collation);
                return CommandOperationHelper.executeWrappedCommandProtocol(binding, MapReduceToCollectionOperation.this.namespace.getDatabaseName(), MapReduceToCollectionOperation.this.getCommand(connection.getDescription()), connection, MapReduceToCollectionOperation.this.transformer());
            }
        });
    }
    
    @Override
    public void executeAsync(final AsyncWriteBinding binding, final SingleResultCallback<MapReduceStatistics> callback) {
        OperationHelper.withConnection(binding, new OperationHelper.AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                final SingleResultCallback<MapReduceStatistics> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                }
                else {
                    final SingleResultCallback<MapReduceStatistics> wrappedCallback = OperationHelper.releasingCallback(errHandlingCallback, connection);
                    OperationHelper.validateCollation(connection, MapReduceToCollectionOperation.this.collation, new OperationHelper.AsyncCallableWithConnection() {
                        @Override
                        public void call(final AsyncConnection connection, final Throwable t) {
                            if (t != null) {
                                wrappedCallback.onResult(null, t);
                            }
                            else {
                                CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, MapReduceToCollectionOperation.this.namespace.getDatabaseName(), MapReduceToCollectionOperation.this.getCommand(connection.getDescription()), connection, MapReduceToCollectionOperation.this.transformer(), (SingleResultCallback<Object>)wrappedCallback);
                            }
                        }
                    });
                }
            }
        });
    }
    
    public ReadOperation<BsonDocument> asExplainableOperation(final ExplainVerbosity explainVerbosity) {
        return this.createExplainableOperation(explainVerbosity);
    }
    
    public AsyncReadOperation<BsonDocument> asExplainableOperationAsync(final ExplainVerbosity explainVerbosity) {
        return this.createExplainableOperation(explainVerbosity);
    }
    
    private CommandReadOperation<BsonDocument> createExplainableOperation(final ExplainVerbosity explainVerbosity) {
        return new CommandReadOperation<BsonDocument>(this.namespace.getDatabaseName(), ExplainHelper.asExplainCommand(this.getCommand(null), explainVerbosity), new BsonDocumentCodec());
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, MapReduceStatistics> transformer() {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, MapReduceStatistics>() {
            @Override
            public MapReduceStatistics apply(final BsonDocument result, final ServerAddress serverAddress) {
                WriteConcernHelper.throwOnWriteConcernError(result, serverAddress);
                return MapReduceHelper.createStatistics(result);
            }
        };
    }
    
    private BsonDocument getCommand(final ConnectionDescription description) {
        final BsonDocument outputDocument = new BsonDocument(this.getAction(), new BsonString(this.getCollectionName()));
        outputDocument.append("sharded", BsonBoolean.valueOf(this.isSharded()));
        outputDocument.append("nonAtomic", BsonBoolean.valueOf(this.isNonAtomic()));
        if (this.getDatabaseName() != null) {
            outputDocument.put("db", new BsonString(this.getDatabaseName()));
        }
        final BsonDocument commandDocument = new BsonDocument("mapreduce", new BsonString(this.namespace.getCollectionName())).append("map", this.getMapFunction()).append("reduce", this.getReduceFunction()).append("out", outputDocument).append("query", asValueOrNull(this.getFilter())).append("sort", asValueOrNull(this.getSort())).append("finalize", asValueOrNull(this.getFinalizeFunction())).append("scope", asValueOrNull(this.getScope())).append("verbose", BsonBoolean.valueOf(this.isVerbose()));
        DocumentHelper.putIfNotZero(commandDocument, "limit", this.getLimit());
        DocumentHelper.putIfNotZero(commandDocument, "maxTimeMS", this.getMaxTime(TimeUnit.MILLISECONDS));
        DocumentHelper.putIfTrue(commandDocument, "jsMode", this.isJsMode());
        if (this.bypassDocumentValidation != null && description != null && OperationHelper.serverIsAtLeastVersionThreeDotTwo(description)) {
            commandDocument.put("bypassDocumentValidation", BsonBoolean.valueOf(this.bypassDocumentValidation));
        }
        if (description != null) {
            WriteConcernHelper.appendWriteConcernToCommand(this.writeConcern, commandDocument, description);
        }
        if (this.collation != null) {
            commandDocument.put("collation", this.collation.asDocument());
        }
        return commandDocument;
    }
    
    private static BsonValue asValueOrNull(final BsonValue value) {
        return (value == null) ? BsonNull.VALUE : value;
    }
    
    static {
        VALID_ACTIONS = Arrays.asList("replace", "merge", "reduce");
    }
}
