package com.mongodb;

import org.bson.codecs.configuration.*;
import org.bson.conversions.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import com.mongodb.client.*;
import com.mongodb.operation.*;
import org.bson.codecs.*;
import org.bson.*;
import java.util.*;

final class ListCollectionsIterableImpl<TResult> implements ListCollectionsIterable<TResult>
{
    private final String databaseName;
    private final Class<TResult> resultClass;
    private final ReadPreference readPreference;
    private final CodecRegistry codecRegistry;
    private final OperationExecutor executor;
    private Bson filter;
    private int batchSize;
    private long maxTimeMS;
    
    ListCollectionsIterableImpl(final String databaseName, final Class<TResult> resultClass, final CodecRegistry codecRegistry, final ReadPreference readPreference, final OperationExecutor executor) {
        this.databaseName = Assertions.notNull("databaseName", databaseName);
        this.resultClass = Assertions.notNull("resultClass", resultClass);
        this.codecRegistry = Assertions.notNull("codecRegistry", codecRegistry);
        this.readPreference = Assertions.notNull("readPreference", readPreference);
        this.executor = Assertions.notNull("executor", executor);
    }
    
    @Override
    public ListCollectionsIterable<TResult> filter(final Bson filter) {
        Assertions.notNull("filter", filter);
        this.filter = filter;
        return this;
    }
    
    @Override
    public ListCollectionsIterable<TResult> maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    @Override
    public ListCollectionsIterable<TResult> batchSize(final int batchSize) {
        this.batchSize = batchSize;
        return this;
    }
    
    @Override
    public MongoCursor<TResult> iterator() {
        return this.execute().iterator();
    }
    
    @Override
    public TResult first() {
        return this.execute().first();
    }
    
    @Override
    public <U> MongoIterable<U> map(final Function<TResult, U> mapper) {
        return new MappingIterable<Object, U>(this, mapper);
    }
    
    @Override
    public void forEach(final Block<? super TResult> block) {
        this.execute().forEach(block);
    }
    
    @Override
    public <A extends Collection<? super TResult>> A into(final A target) {
        return this.execute().into(target);
    }
    
    private MongoIterable<TResult> execute() {
        return new OperationIterable<TResult>(this.createListCollectionsOperation(), this.readPreference, this.executor);
    }
    
    private ListCollectionsOperation<TResult> createListCollectionsOperation() {
        return new ListCollectionsOperation<TResult>(this.databaseName, this.codecRegistry.get(this.resultClass)).filter(this.toBsonDocument(this.filter)).batchSize(this.batchSize).maxTime(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    private BsonDocument toBsonDocument(final Bson document) {
        return (document == null) ? null : document.toBsonDocument(BsonDocument.class, this.codecRegistry);
    }
}
