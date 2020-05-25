package com.mongodb;

import org.bson.codecs.configuration.*;
import org.bson.conversions.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import com.mongodb.client.model.*;
import org.bson.codecs.*;
import org.bson.*;
import com.mongodb.client.*;
import com.mongodb.operation.*;
import java.util.*;

class AggregateIterableImpl<TDocument, TResult> implements AggregateIterable<TResult>
{
    private final MongoNamespace namespace;
    private final Class<TDocument> documentClass;
    private final Class<TResult> resultClass;
    private final ReadPreference readPreference;
    private final ReadConcern readConcern;
    private final WriteConcern writeConcern;
    private final CodecRegistry codecRegistry;
    private final OperationExecutor executor;
    private final List<? extends Bson> pipeline;
    private Boolean allowDiskUse;
    private Integer batchSize;
    private long maxTimeMS;
    private Boolean useCursor;
    private Boolean bypassDocumentValidation;
    private Collation collation;
    
    AggregateIterableImpl(final MongoNamespace namespace, final Class<TDocument> documentClass, final Class<TResult> resultClass, final CodecRegistry codecRegistry, final ReadPreference readPreference, final ReadConcern readConcern, final WriteConcern writeConcern, final OperationExecutor executor, final List<? extends Bson> pipeline) {
        this.namespace = Assertions.notNull("namespace", namespace);
        this.documentClass = Assertions.notNull("documentClass", documentClass);
        this.resultClass = Assertions.notNull("resultClass", resultClass);
        this.codecRegistry = Assertions.notNull("codecRegistry", codecRegistry);
        this.readPreference = Assertions.notNull("readPreference", readPreference);
        this.readConcern = Assertions.notNull("readConcern", readConcern);
        this.writeConcern = Assertions.notNull("writeConcern", writeConcern);
        this.executor = Assertions.notNull("executor", executor);
        this.pipeline = Assertions.notNull("pipeline", pipeline);
    }
    
    @Override
    public void toCollection() {
        final List<BsonDocument> aggregateList = this.createBsonDocumentList(this.pipeline);
        if (this.getOutCollection(aggregateList) == null) {
            throw new IllegalStateException("The last stage of the aggregation pipeline must be $out");
        }
        this.executor.execute((WriteOperation<Object>)this.createAggregateToCollectionOperation(aggregateList));
    }
    
    @Override
    public AggregateIterable<TResult> allowDiskUse(final Boolean allowDiskUse) {
        this.allowDiskUse = allowDiskUse;
        return this;
    }
    
    @Override
    public AggregateIterable<TResult> batchSize(final int batchSize) {
        this.batchSize = batchSize;
        return this;
    }
    
    @Override
    public AggregateIterable<TResult> maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    @Override
    public AggregateIterable<TResult> useCursor(final Boolean useCursor) {
        this.useCursor = useCursor;
        return this;
    }
    
    @Override
    public AggregateIterable<TResult> bypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
        return this;
    }
    
    @Override
    public AggregateIterable<TResult> collation(final Collation collation) {
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
        final List<BsonDocument> aggregateList = this.createBsonDocumentList(this.pipeline);
        final BsonValue outCollection = this.getOutCollection(aggregateList);
        if (outCollection != null) {
            this.executor.execute((WriteOperation<Object>)this.createAggregateToCollectionOperation(aggregateList));
            final FindIterable<TResult> findOperation = new FindIterableImpl<Object, TResult>(new MongoNamespace(this.namespace.getDatabaseName(), outCollection.asString().getValue()), this.documentClass, this.resultClass, this.codecRegistry, this.readPreference, this.readConcern, this.executor, new BsonDocument(), new FindOptions().collation(this.collation));
            if (this.batchSize != null) {
                findOperation.batchSize((int)this.batchSize);
            }
            return findOperation;
        }
        return new OperationIterable<TResult>(new AggregateOperation<TResult>(this.namespace, aggregateList, this.codecRegistry.get(this.resultClass)).maxTime(this.maxTimeMS, TimeUnit.MILLISECONDS).allowDiskUse(this.allowDiskUse).batchSize(this.batchSize).useCursor(this.useCursor).readConcern(this.readConcern).collation(this.collation), this.readPreference, this.executor);
    }
    
    private BsonValue getOutCollection(final List<BsonDocument> aggregateList) {
        return (aggregateList.size() == 0) ? null : aggregateList.get(aggregateList.size() - 1).get("$out");
    }
    
    private AggregateToCollectionOperation createAggregateToCollectionOperation(final List<BsonDocument> aggregateList) {
        return new AggregateToCollectionOperation(this.namespace, aggregateList, this.writeConcern).maxTime(this.maxTimeMS, TimeUnit.MILLISECONDS).allowDiskUse(this.allowDiskUse).bypassDocumentValidation(this.bypassDocumentValidation).collation(this.collation);
    }
    
    private List<BsonDocument> createBsonDocumentList(final List<? extends Bson> pipeline) {
        final List<BsonDocument> aggregateList = new ArrayList<BsonDocument>(pipeline.size());
        for (final Bson obj : pipeline) {
            if (obj == null) {
                throw new IllegalArgumentException("pipeline can not contain a null value");
            }
            aggregateList.add(obj.toBsonDocument(this.documentClass, this.codecRegistry));
        }
        return aggregateList;
    }
}
