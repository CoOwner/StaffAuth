package com.mongodb;

import org.bson.codecs.configuration.*;
import com.mongodb.assertions.*;
import org.bson.conversions.*;
import org.bson.codecs.*;
import com.mongodb.client.*;
import org.bson.*;
import com.mongodb.client.model.*;
import com.mongodb.operation.*;
import java.util.*;

class MongoDatabaseImpl implements MongoDatabase
{
    private final String name;
    private final ReadPreference readPreference;
    private final CodecRegistry codecRegistry;
    private final WriteConcern writeConcern;
    private final ReadConcern readConcern;
    private final OperationExecutor executor;
    
    MongoDatabaseImpl(final String name, final CodecRegistry codecRegistry, final ReadPreference readPreference, final WriteConcern writeConcern, final ReadConcern readConcern, final OperationExecutor executor) {
        MongoNamespace.checkDatabaseNameValidity(name);
        this.name = Assertions.notNull("name", name);
        this.codecRegistry = Assertions.notNull("codecRegistry", codecRegistry);
        this.readPreference = Assertions.notNull("readPreference", readPreference);
        this.writeConcern = Assertions.notNull("writeConcern", writeConcern);
        this.readConcern = Assertions.notNull("readConcern", readConcern);
        this.executor = Assertions.notNull("executor", executor);
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public CodecRegistry getCodecRegistry() {
        return this.codecRegistry;
    }
    
    @Override
    public ReadPreference getReadPreference() {
        return this.readPreference;
    }
    
    @Override
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    @Override
    public ReadConcern getReadConcern() {
        return this.readConcern;
    }
    
    @Override
    public MongoDatabase withCodecRegistry(final CodecRegistry codecRegistry) {
        return new MongoDatabaseImpl(this.name, codecRegistry, this.readPreference, this.writeConcern, this.readConcern, this.executor);
    }
    
    @Override
    public MongoDatabase withReadPreference(final ReadPreference readPreference) {
        return new MongoDatabaseImpl(this.name, this.codecRegistry, readPreference, this.writeConcern, this.readConcern, this.executor);
    }
    
    @Override
    public MongoDatabase withWriteConcern(final WriteConcern writeConcern) {
        return new MongoDatabaseImpl(this.name, this.codecRegistry, this.readPreference, writeConcern, this.readConcern, this.executor);
    }
    
    @Override
    public MongoDatabase withReadConcern(final ReadConcern readConcern) {
        return new MongoDatabaseImpl(this.name, this.codecRegistry, this.readPreference, this.writeConcern, readConcern, this.executor);
    }
    
    @Override
    public MongoCollection<Document> getCollection(final String collectionName) {
        return this.getCollection(collectionName, Document.class);
    }
    
    @Override
    public <TDocument> MongoCollection<TDocument> getCollection(final String collectionName, final Class<TDocument> documentClass) {
        return new MongoCollectionImpl<TDocument>(new MongoNamespace(this.name, collectionName), documentClass, this.codecRegistry, this.readPreference, this.writeConcern, this.readConcern, this.executor);
    }
    
    @Override
    public Document runCommand(final Bson command) {
        return this.runCommand(command, Document.class);
    }
    
    @Override
    public Document runCommand(final Bson command, final ReadPreference readPreference) {
        return this.runCommand(command, readPreference, Document.class);
    }
    
    @Override
    public <TResult> TResult runCommand(final Bson command, final Class<TResult> resultClass) {
        return this.runCommand(command, ReadPreference.primary(), resultClass);
    }
    
    @Override
    public <TResult> TResult runCommand(final Bson command, final ReadPreference readPreference, final Class<TResult> resultClass) {
        Assertions.notNull("readPreference", readPreference);
        return this.executor.execute(new CommandReadOperation<TResult>(this.getName(), this.toBsonDocument(command), this.codecRegistry.get(resultClass)), readPreference);
    }
    
    @Override
    public void drop() {
        this.executor.execute((WriteOperation<Object>)new DropDatabaseOperation(this.name, this.getWriteConcern()));
    }
    
    @Override
    public MongoIterable<String> listCollectionNames() {
        return new ListCollectionsIterableImpl<BsonDocument>(this.name, BsonDocument.class, MongoClient.getDefaultCodecRegistry(), ReadPreference.primary(), this.executor).map((Function<BsonDocument, String>)new Function<BsonDocument, String>() {
            @Override
            public String apply(final BsonDocument result) {
                return result.getString("name").getValue();
            }
        });
    }
    
    @Override
    public ListCollectionsIterable<Document> listCollections() {
        return this.listCollections(Document.class);
    }
    
    @Override
    public <TResult> ListCollectionsIterable<TResult> listCollections(final Class<TResult> resultClass) {
        return new ListCollectionsIterableImpl<TResult>(this.name, resultClass, this.codecRegistry, ReadPreference.primary(), this.executor);
    }
    
    @Override
    public void createCollection(final String collectionName) {
        this.createCollection(collectionName, new CreateCollectionOptions());
    }
    
    @Override
    public void createCollection(final String collectionName, final CreateCollectionOptions createCollectionOptions) {
        final CreateCollectionOperation operation = new CreateCollectionOperation(this.name, collectionName, this.writeConcern).collation(createCollectionOptions.getCollation()).capped(createCollectionOptions.isCapped()).sizeInBytes(createCollectionOptions.getSizeInBytes()).autoIndex(createCollectionOptions.isAutoIndex()).maxDocuments(createCollectionOptions.getMaxDocuments()).usePowerOf2Sizes(createCollectionOptions.isUsePowerOf2Sizes()).storageEngineOptions(this.toBsonDocument(createCollectionOptions.getStorageEngineOptions()));
        final IndexOptionDefaults indexOptionDefaults = createCollectionOptions.getIndexOptionDefaults();
        if (indexOptionDefaults.getStorageEngine() != null) {
            operation.indexOptionDefaults(new BsonDocument("storageEngine", this.toBsonDocument(indexOptionDefaults.getStorageEngine())));
        }
        final ValidationOptions validationOptions = createCollectionOptions.getValidationOptions();
        if (validationOptions.getValidator() != null) {
            operation.validator(this.toBsonDocument(validationOptions.getValidator()));
        }
        if (validationOptions.getValidationLevel() != null) {
            operation.validationLevel(validationOptions.getValidationLevel());
        }
        if (validationOptions.getValidationAction() != null) {
            operation.validationAction(validationOptions.getValidationAction());
        }
        this.executor.execute((WriteOperation<Object>)operation);
    }
    
    @Override
    public void createView(final String viewName, final String viewOn, final List<? extends Bson> pipeline) {
        this.createView(viewName, viewOn, pipeline, new CreateViewOptions());
    }
    
    @Override
    public void createView(final String viewName, final String viewOn, final List<? extends Bson> pipeline, final CreateViewOptions createViewOptions) {
        Assertions.notNull("createViewOptions", createViewOptions);
        this.executor.execute((WriteOperation<Object>)new CreateViewOperation(this.name, viewName, viewOn, this.createBsonDocumentList(pipeline), this.writeConcern).collation(createViewOptions.getCollation()));
    }
    
    private List<BsonDocument> createBsonDocumentList(final List<? extends Bson> pipeline) {
        Assertions.notNull("pipeline", pipeline);
        final List<BsonDocument> bsonDocumentPipeline = new ArrayList<BsonDocument>(pipeline.size());
        for (final Bson obj : pipeline) {
            if (obj == null) {
                throw new IllegalArgumentException("pipeline can not contain a null value");
            }
            bsonDocumentPipeline.add(obj.toBsonDocument(BsonDocument.class, this.codecRegistry));
        }
        return bsonDocumentPipeline;
    }
    
    private BsonDocument toBsonDocument(final Bson document) {
        return (document == null) ? null : document.toBsonDocument(BsonDocument.class, this.codecRegistry);
    }
}
