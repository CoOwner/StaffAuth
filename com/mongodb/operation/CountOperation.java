package com.mongodb.operation;

import com.mongodb.client.model.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import org.bson.codecs.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.connection.*;
import com.mongodb.internal.async.*;
import com.mongodb.*;
import org.bson.*;

public class CountOperation implements AsyncReadOperation<Long>, ReadOperation<Long>
{
    private final MongoNamespace namespace;
    private BsonDocument filter;
    private BsonValue hint;
    private long skip;
    private long limit;
    private long maxTimeMS;
    private ReadConcern readConcern;
    private Collation collation;
    
    public CountOperation(final MongoNamespace namespace) {
        this.readConcern = ReadConcern.DEFAULT;
        this.namespace = Assertions.notNull("namespace", namespace);
    }
    
    public BsonDocument getFilter() {
        return this.filter;
    }
    
    public CountOperation filter(final BsonDocument filter) {
        this.filter = filter;
        return this;
    }
    
    public BsonValue getHint() {
        return this.hint;
    }
    
    public CountOperation hint(final BsonValue hint) {
        this.hint = hint;
        return this;
    }
    
    public long getLimit() {
        return this.limit;
    }
    
    public CountOperation limit(final long limit) {
        this.limit = limit;
        return this;
    }
    
    public long getSkip() {
        return this.skip;
    }
    
    public CountOperation skip(final long skip) {
        this.skip = skip;
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public CountOperation maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public ReadConcern getReadConcern() {
        return this.readConcern;
    }
    
    public CountOperation readConcern(final ReadConcern readConcern) {
        this.readConcern = Assertions.notNull("readConcern", readConcern);
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public CountOperation collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    @Override
    public Long execute(final ReadBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<Long>)new OperationHelper.CallableWithConnection<Long>() {
            @Override
            public Long call(final Connection connection) {
                OperationHelper.validateReadConcernAndCollation(connection, CountOperation.this.readConcern, CountOperation.this.collation);
                return CommandOperationHelper.executeWrappedCommandProtocol(binding, CountOperation.this.namespace.getDatabaseName(), CountOperation.this.getCommand(), new BsonDocumentCodec(), connection, CountOperation.this.transformer());
            }
        });
    }
    
    @Override
    public void executeAsync(final AsyncReadBinding binding, final SingleResultCallback<Long> callback) {
        OperationHelper.withConnection(binding, new OperationHelper.AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                final SingleResultCallback<Long> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                }
                else {
                    final SingleResultCallback<Long> wrappedCallback = OperationHelper.releasingCallback(errHandlingCallback, connection);
                    OperationHelper.validateReadConcernAndCollation(connection, CountOperation.this.readConcern, CountOperation.this.collation, new OperationHelper.AsyncCallableWithConnection() {
                        @Override
                        public void call(final AsyncConnection connection, final Throwable t) {
                            if (t != null) {
                                wrappedCallback.onResult(null, t);
                            }
                            else {
                                CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, CountOperation.this.namespace.getDatabaseName(), CountOperation.this.getCommand(), new BsonDocumentCodec(), connection, CountOperation.this.transformer(), (SingleResultCallback<Object>)wrappedCallback);
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
        return new CommandReadOperation<BsonDocument>(this.namespace.getDatabaseName(), ExplainHelper.asExplainCommand(this.getCommand(), explainVerbosity), new BsonDocumentCodec());
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, Long> transformer() {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, Long>() {
            @Override
            public Long apply(final BsonDocument result, final ServerAddress serverAddress) {
                return result.getNumber("n").longValue();
            }
        };
    }
    
    private BsonDocument getCommand() {
        final BsonDocument document = new BsonDocument("count", new BsonString(this.namespace.getCollectionName()));
        DocumentHelper.putIfNotNull(document, "query", this.filter);
        DocumentHelper.putIfNotZero(document, "limit", this.limit);
        DocumentHelper.putIfNotZero(document, "skip", this.skip);
        DocumentHelper.putIfNotNull(document, "hint", this.hint);
        DocumentHelper.putIfNotZero(document, "maxTimeMS", this.maxTimeMS);
        if (!this.readConcern.isServerDefault()) {
            document.put("readConcern", this.readConcern.asDocument());
        }
        if (this.collation != null) {
            document.put("collation", this.collation.asDocument());
        }
        return document;
    }
}
