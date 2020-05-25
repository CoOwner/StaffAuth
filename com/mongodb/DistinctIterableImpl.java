package com.mongodb;

import org.bson.codecs.configuration.*;
import org.bson.conversions.*;
import com.mongodb.client.model.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import com.mongodb.client.*;
import org.bson.codecs.*;
import com.mongodb.operation.*;
import java.util.*;

class DistinctIterableImpl<TDocument, TResult> implements DistinctIterable<TResult>
{
    private final MongoNamespace namespace;
    private final Class<TDocument> documentClass;
    private final Class<TResult> resultClass;
    private final ReadPreference readPreference;
    private final ReadConcern readConcern;
    private final CodecRegistry codecRegistry;
    private final OperationExecutor executor;
    private final String fieldName;
    private Bson filter;
    private long maxTimeMS;
    private Collation collation;
    
    DistinctIterableImpl(final MongoNamespace namespace, final Class<TDocument> documentClass, final Class<TResult> resultClass, final CodecRegistry codecRegistry, final ReadPreference readPreference, final ReadConcern readConcern, final OperationExecutor executor, final String fieldName, final Bson filter) {
        this.namespace = Assertions.notNull("namespace", namespace);
        this.documentClass = Assertions.notNull("documentClass", documentClass);
        this.resultClass = Assertions.notNull("resultClass", resultClass);
        this.codecRegistry = Assertions.notNull("codecRegistry", codecRegistry);
        this.readPreference = Assertions.notNull("readPreference", readPreference);
        this.readConcern = Assertions.notNull("readConcern", readConcern);
        this.executor = Assertions.notNull("executor", executor);
        this.fieldName = Assertions.notNull("mapFunction", fieldName);
        this.filter = filter;
    }
    
    @Override
    public DistinctIterable<TResult> filter(final Bson filter) {
        this.filter = filter;
        return this;
    }
    
    @Override
    public DistinctIterable<TResult> maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    @Override
    public DistinctIterable<TResult> batchSize(final int batchSize) {
        return this;
    }
    
    @Override
    public DistinctIterable<TResult> collation(final Collation collation) {
        this.collation = collation;
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
        final DistinctOperation<TResult> operation = new DistinctOperation<TResult>(this.namespace, this.fieldName, this.codecRegistry.get(this.resultClass)).filter((this.filter == null) ? null : this.filter.toBsonDocument(this.documentClass, this.codecRegistry)).maxTime(this.maxTimeMS, TimeUnit.MILLISECONDS).readConcern(this.readConcern).collation(this.collation);
        return new OperationIterable<TResult>(operation, this.readPreference, this.executor);
    }
}
