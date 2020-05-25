package com.mongodb;

import com.mongodb.annotations.*;
import org.bson.types.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import java.util.*;
import com.mongodb.client.*;
import org.bson.codecs.*;
import com.mongodb.client.model.*;
import com.mongodb.operation.*;
import com.mongodb.bulk.*;
import com.mongodb.connection.*;
import org.bson.*;

@ThreadSafe
public class DBCollection
{
    public static final String ID_FIELD_NAME = "_id";
    private final String name;
    private final DB database;
    private final OperationExecutor executor;
    private final Bytes.OptionHolder optionHolder;
    private volatile ReadPreference readPreference;
    private volatile WriteConcern writeConcern;
    private volatile ReadConcern readConcern;
    private List<DBObject> hintFields;
    private DBEncoderFactory encoderFactory;
    private DBDecoderFactory decoderFactory;
    private DBCollectionObjectFactory objectFactory;
    private volatile CompoundDBObjectCodec objectCodec;
    
    DBCollection(final String name, final DB database, final OperationExecutor executor) {
        MongoNamespace.checkCollectionNameValidity(name);
        this.name = name;
        this.database = database;
        this.executor = executor;
        this.optionHolder = new Bytes.OptionHolder(database.getOptionHolder());
        this.objectFactory = new DBCollectionObjectFactory();
        this.objectCodec = new CompoundDBObjectCodec(this.getDefaultDBObjectCodec());
    }
    
    protected DBCollection(final DB database, final String name) {
        this(name, database, database.getExecutor());
    }
    
    private static BasicDBList toDBList(final BatchCursor<DBObject> source) {
        final BasicDBList dbList = new BasicDBList();
        while (source.hasNext()) {
            dbList.addAll(source.next());
        }
        return dbList;
    }
    
    public WriteResult insert(final DBObject document, final WriteConcern writeConcern) {
        return this.insert(Arrays.asList(document), writeConcern);
    }
    
    public WriteResult insert(final DBObject... documents) {
        return this.insert(Arrays.asList(documents), this.getWriteConcern());
    }
    
    public WriteResult insert(final WriteConcern writeConcern, final DBObject... documents) {
        return this.insert(documents, writeConcern);
    }
    
    public WriteResult insert(final DBObject[] documents, final WriteConcern writeConcern) {
        return this.insert(Arrays.asList(documents), writeConcern);
    }
    
    public WriteResult insert(final List<? extends DBObject> documents) {
        return this.insert(documents, this.getWriteConcern());
    }
    
    public WriteResult insert(final List<? extends DBObject> documents, final WriteConcern aWriteConcern) {
        return this.insert(documents, aWriteConcern, null);
    }
    
    public WriteResult insert(final DBObject[] documents, final WriteConcern aWriteConcern, final DBEncoder encoder) {
        return this.insert(Arrays.asList(documents), aWriteConcern, encoder);
    }
    
    public WriteResult insert(final List<? extends DBObject> documents, final WriteConcern aWriteConcern, final DBEncoder dbEncoder) {
        return this.insert(documents, new InsertOptions().writeConcern(aWriteConcern).dbEncoder(dbEncoder));
    }
    
    public WriteResult insert(final List<? extends DBObject> documents, final InsertOptions insertOptions) {
        final WriteConcern writeConcern = (insertOptions.getWriteConcern() != null) ? insertOptions.getWriteConcern() : this.getWriteConcern();
        final Encoder<DBObject> encoder = this.toEncoder(insertOptions.getDbEncoder());
        final List<InsertRequest> insertRequestList = new ArrayList<InsertRequest>(documents.size());
        for (final DBObject cur : documents) {
            if (cur.get("_id") == null) {
                cur.put("_id", new ObjectId());
            }
            insertRequestList.add(new InsertRequest(new BsonDocumentWrapper<Object>(cur, encoder)));
        }
        return this.insert(insertRequestList, writeConcern, insertOptions.isContinueOnError(), insertOptions.getBypassDocumentValidation());
    }
    
    private Encoder<DBObject> toEncoder(final DBEncoder dbEncoder) {
        return (dbEncoder != null) ? new DBEncoderAdapter(dbEncoder) : this.objectCodec;
    }
    
    private WriteResult insert(final List<InsertRequest> insertRequestList, final WriteConcern writeConcern, final boolean continueOnError, final Boolean bypassDocumentValidation) {
        return this.executeWriteOperation(new InsertOperation(this.getNamespace(), !continueOnError, writeConcern, insertRequestList).bypassDocumentValidation(bypassDocumentValidation));
    }
    
    WriteResult executeWriteOperation(final BaseWriteOperation operation) {
        return this.translateWriteResult(this.executor.execute((WriteOperation<WriteConcernResult>)operation));
    }
    
    private WriteResult translateWriteResult(final WriteConcernResult writeConcernResult) {
        if (!writeConcernResult.wasAcknowledged()) {
            return WriteResult.unacknowledged();
        }
        return this.translateWriteResult(writeConcernResult.getCount(), writeConcernResult.isUpdateOfExisting(), writeConcernResult.getUpsertedId());
    }
    
    private WriteResult translateWriteResult(final int count, final boolean isUpdateOfExisting, final BsonValue upsertedId) {
        final Object newUpsertedId = (upsertedId == null) ? null : this.getObjectCodec().decode(new BsonDocumentReader(new BsonDocument("_id", upsertedId)), DecoderContext.builder().build()).get("_id");
        return new WriteResult(count, isUpdateOfExisting, newUpsertedId);
    }
    
    public WriteResult save(final DBObject document) {
        return this.save(document, this.getWriteConcern());
    }
    
    public WriteResult save(final DBObject document, final WriteConcern writeConcern) {
        final Object id = document.get("_id");
        if (id == null) {
            return this.insert(document, writeConcern);
        }
        return this.replaceOrInsert(document, id, writeConcern);
    }
    
    private WriteResult replaceOrInsert(final DBObject obj, final Object id, final WriteConcern writeConcern) {
        final DBObject filter = new BasicDBObject("_id", id);
        final UpdateRequest replaceRequest = new UpdateRequest(this.wrap(filter), this.wrap(obj, this.objectCodec), WriteRequest.Type.REPLACE).upsert(true);
        return this.executeWriteOperation(new UpdateOperation(this.getNamespace(), false, writeConcern, Arrays.asList(replaceRequest)));
    }
    
    public WriteResult update(final DBObject query, final DBObject update, final boolean upsert, final boolean multi, final WriteConcern aWriteConcern) {
        return this.update(query, update, upsert, multi, aWriteConcern, null);
    }
    
    public WriteResult update(final DBObject query, final DBObject update, final boolean upsert, final boolean multi, final WriteConcern concern, final DBEncoder encoder) {
        return this.update(query, update, upsert, multi, concern, null, encoder);
    }
    
    public WriteResult update(final DBObject query, final DBObject update, final boolean upsert, final boolean multi, final WriteConcern concern, final Boolean bypassDocumentValidation, final DBEncoder encoder) {
        return this.update(query, update, new DBCollectionUpdateOptions().upsert(upsert).multi(multi).writeConcern(concern).bypassDocumentValidation(bypassDocumentValidation).encoder(encoder));
    }
    
    public WriteResult update(final DBObject query, final DBObject update, final boolean upsert, final boolean multi) {
        return this.update(query, update, upsert, multi, this.getWriteConcern());
    }
    
    public WriteResult update(final DBObject query, final DBObject update) {
        return this.update(query, update, false, false);
    }
    
    public WriteResult updateMulti(final DBObject query, final DBObject update) {
        return this.update(query, update, false, true);
    }
    
    public WriteResult update(final DBObject query, final DBObject update, final DBCollectionUpdateOptions options) {
        Assertions.notNull("query", query);
        Assertions.notNull("update", update);
        Assertions.notNull("options", options);
        final WriteConcern writeConcern = (options.getWriteConcern() != null) ? options.getWriteConcern() : this.getWriteConcern();
        if (!update.keySet().isEmpty() && update.keySet().iterator().next().startsWith("$")) {
            final UpdateRequest updateRequest = new UpdateRequest(this.wrap(query), this.wrap(update, options.getEncoder()), WriteRequest.Type.UPDATE).upsert(options.isUpsert()).multi(options.isMulti()).collation(options.getCollation());
            return this.executeWriteOperation(new UpdateOperation(this.getNamespace(), false, writeConcern, Collections.singletonList(updateRequest)).bypassDocumentValidation(options.getBypassDocumentValidation()));
        }
        final UpdateRequest replaceRequest = new UpdateRequest(this.wrap(query), this.wrap(update, options.getEncoder()), WriteRequest.Type.REPLACE).upsert(options.isUpsert()).multi(options.isMulti()).collation(options.getCollation());
        return this.executeWriteOperation(new UpdateOperation(this.getNamespace(), true, writeConcern, Collections.singletonList(replaceRequest)).bypassDocumentValidation(options.getBypassDocumentValidation()));
    }
    
    public WriteResult remove(final DBObject query) {
        return this.remove(query, this.getWriteConcern());
    }
    
    public WriteResult remove(final DBObject query, final WriteConcern writeConcern) {
        return this.remove(query, new DBCollectionRemoveOptions().writeConcern(writeConcern));
    }
    
    public WriteResult remove(final DBObject query, final WriteConcern writeConcern, final DBEncoder encoder) {
        return this.remove(query, new DBCollectionRemoveOptions().writeConcern(writeConcern).encoder(encoder));
    }
    
    public WriteResult remove(final DBObject query, final DBCollectionRemoveOptions options) {
        Assertions.notNull("query", query);
        Assertions.notNull("options", options);
        final WriteConcern writeConcern = (options.getWriteConcern() != null) ? options.getWriteConcern() : this.getWriteConcern();
        final DeleteRequest deleteRequest = new DeleteRequest(this.wrap(query, options.getEncoder())).collation(options.getCollation());
        return this.executeWriteOperation(new DeleteOperation(this.getNamespace(), false, writeConcern, Collections.singletonList(deleteRequest)));
    }
    
    @Deprecated
    public DBCursor find(final DBObject query, final DBObject projection, final int numToSkip, final int batchSize, final int options) {
        return new DBCursor(this, query, projection, this.getReadPreference()).batchSize(batchSize).skip(numToSkip).setOptions(options);
    }
    
    @Deprecated
    public DBCursor find(final DBObject query, final DBObject projection, final int numToSkip, final int batchSize) {
        return new DBCursor(this, query, projection, this.getReadPreference()).batchSize(batchSize).skip(numToSkip);
    }
    
    public DBCursor find(final DBObject query) {
        return new DBCursor(this, query, null, this.getReadPreference());
    }
    
    public DBCursor find(final DBObject query, final DBObject projection) {
        return new DBCursor(this, query, projection, this.getReadPreference());
    }
    
    public DBCursor find() {
        return this.find(new BasicDBObject());
    }
    
    public DBCursor find(final DBObject query, final DBCollectionFindOptions options) {
        return new DBCursor(this, query, options);
    }
    
    public DBObject findOne() {
        return this.findOne(new BasicDBObject());
    }
    
    public DBObject findOne(final DBObject query) {
        return this.findOne(query, null, null, this.getReadPreference());
    }
    
    public DBObject findOne(final DBObject query, final DBObject projection) {
        return this.findOne(query, projection, null, this.getReadPreference());
    }
    
    public DBObject findOne(final DBObject query, final DBObject projection, final DBObject sort) {
        return this.findOne(query, projection, sort, this.getReadPreference());
    }
    
    public DBObject findOne(final DBObject query, final DBObject projection, final ReadPreference readPreference) {
        return this.findOne(query, projection, null, readPreference);
    }
    
    public DBObject findOne(final DBObject query, final DBObject projection, final DBObject sort, final ReadPreference readPreference) {
        return this.findOne((query != null) ? query : new BasicDBObject(), new DBCollectionFindOptions().projection(projection).sort(sort).readPreference(readPreference));
    }
    
    public DBObject findOne(final Object id) {
        return this.findOne(new BasicDBObject("_id", id), new DBCollectionFindOptions());
    }
    
    public DBObject findOne(final Object id, final DBObject projection) {
        return this.findOne(new BasicDBObject("_id", id), new DBCollectionFindOptions().projection(projection));
    }
    
    public DBObject findOne(final DBObject query, final DBCollectionFindOptions findOptions) {
        return this.find(query, findOptions).one();
    }
    
    public long count() {
        return this.getCount(new BasicDBObject(), new DBCollectionCountOptions());
    }
    
    public long count(final DBObject query) {
        return this.getCount(query, new DBCollectionCountOptions());
    }
    
    public long count(final DBObject query, final ReadPreference readPreference) {
        return this.getCount(query, null, readPreference);
    }
    
    public long count(final DBObject query, final DBCollectionCountOptions options) {
        return this.getCount(query, options);
    }
    
    public long getCount() {
        return this.getCount(new BasicDBObject(), new DBCollectionCountOptions());
    }
    
    public long getCount(final ReadPreference readPreference) {
        return this.getCount(new BasicDBObject(), null, readPreference);
    }
    
    public long getCount(final DBObject query) {
        return this.getCount(query, new DBCollectionCountOptions());
    }
    
    public long getCount(final DBObject query, final DBObject projection) {
        return this.getCount(query, projection, 0L, 0L);
    }
    
    public long getCount(final DBObject query, final DBObject projection, final ReadPreference readPreference) {
        return this.getCount(query, projection, 0L, 0L, readPreference);
    }
    
    public long getCount(final DBObject query, final DBObject projection, final long limit, final long skip) {
        return this.getCount(query, projection, limit, skip, this.getReadPreference());
    }
    
    public long getCount(final DBObject query, final DBObject projection, final long limit, final long skip, final ReadPreference readPreference) {
        return this.getCount(query, new DBCollectionCountOptions().limit(limit).skip(skip).readPreference(readPreference));
    }
    
    public long getCount(final DBObject query, final DBCollectionCountOptions options) {
        Assertions.notNull("countOptions", options);
        final CountOperation operation = new CountOperation(this.getNamespace()).readConcern((options.getReadConcern() != null) ? options.getReadConcern() : this.getReadConcern()).skip(options.getSkip()).limit(options.getLimit()).maxTime(options.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS).collation(options.getCollation());
        if (query != null) {
            operation.filter(this.wrap(query));
        }
        if (options.getHint() != null) {
            operation.hint(this.wrap(options.getHint()));
        }
        else if (options.getHintString() != null) {
            operation.hint(new BsonString(options.getHintString()));
        }
        return this.executor.execute((ReadOperation<Long>)operation, (options.getReadPreference() != null) ? options.getReadPreference() : this.getReadPreference());
    }
    
    public DBCollection rename(final String newName) {
        return this.rename(newName, false);
    }
    
    public DBCollection rename(final String newName, final boolean dropTarget) {
        try {
            this.executor.execute((WriteOperation<Object>)new RenameCollectionOperation(this.getNamespace(), new MongoNamespace(this.getNamespace().getDatabaseName(), newName), this.getWriteConcern()).dropTarget(dropTarget));
            return this.getDB().getCollection(newName);
        }
        catch (MongoWriteConcernException e) {
            throw createWriteConcernException(e);
        }
    }
    
    public DBObject group(final DBObject key, final DBObject cond, final DBObject initial, final String reduce) {
        return this.group(key, cond, initial, reduce, null);
    }
    
    public DBObject group(final DBObject key, final DBObject cond, final DBObject initial, final String reduce, final String finalize) {
        return this.group(key, cond, initial, reduce, finalize, this.getReadPreference());
    }
    
    public DBObject group(final DBObject key, final DBObject cond, final DBObject initial, final String reduce, final String finalize, final ReadPreference readPreference) {
        return this.group(new GroupCommand(this, key, cond, initial, reduce, finalize), readPreference);
    }
    
    public DBObject group(final GroupCommand cmd) {
        return this.group(cmd, this.getReadPreference());
    }
    
    public DBObject group(final GroupCommand cmd, final ReadPreference readPreference) {
        return toDBList(this.executor.execute((ReadOperation<BatchCursor<DBObject>>)cmd.toOperation(this.getNamespace(), this.getDefaultDBObjectCodec()), readPreference));
    }
    
    public List distinct(final String fieldName) {
        return this.distinct(fieldName, this.getReadPreference());
    }
    
    public List distinct(final String fieldName, final ReadPreference readPreference) {
        return this.distinct(fieldName, new BasicDBObject(), readPreference);
    }
    
    public List distinct(final String fieldName, final DBObject query) {
        return this.distinct(fieldName, query, this.getReadPreference());
    }
    
    public List distinct(final String fieldName, final DBObject query, final ReadPreference readPreference) {
        return this.distinct(fieldName, new DBCollectionDistinctOptions().filter(query).readPreference(readPreference));
    }
    
    public List distinct(final String fieldName, final DBCollectionDistinctOptions options) {
        Assertions.notNull("fieldName", fieldName);
        return (ArrayList)new OperationIterable(new DistinctOperation(this.getNamespace(), fieldName, new BsonValueCodec()).readConcern((options.getReadConcern() != null) ? options.getReadConcern() : this.getReadConcern()).filter(this.wrapAllowNull(options.getFilter())).collation(options.getCollation()), (options.getReadPreference() != null) ? options.getReadPreference() : this.getReadPreference(), this.executor).map((Function)new Function<BsonValue, Object>() {
            @Override
            public Object apply(final BsonValue bsonValue) {
                if (bsonValue == null) {
                    return null;
                }
                final BsonDocument document = new BsonDocument("value", bsonValue);
                final DBObject obj = DBCollection.this.getDefaultDBObjectCodec().decode((BsonReader)new BsonDocumentReader(document), DecoderContext.builder().build());
                return obj.get("value");
            }
        }).into(new ArrayList());
    }
    
    public MapReduceOutput mapReduce(final String map, final String reduce, final String outputTarget, final DBObject query) {
        final MapReduceCommand command = new MapReduceCommand(this, map, reduce, outputTarget, MapReduceCommand.OutputType.REDUCE, query);
        return this.mapReduce(command);
    }
    
    public MapReduceOutput mapReduce(final String map, final String reduce, final String outputTarget, final MapReduceCommand.OutputType outputType, final DBObject query) {
        final MapReduceCommand command = new MapReduceCommand(this, map, reduce, outputTarget, outputType, query);
        return this.mapReduce(command);
    }
    
    public MapReduceOutput mapReduce(final String map, final String reduce, final String outputTarget, final MapReduceCommand.OutputType outputType, final DBObject query, final ReadPreference readPreference) {
        final MapReduceCommand command = new MapReduceCommand(this, map, reduce, outputTarget, outputType, query);
        command.setReadPreference(readPreference);
        return this.mapReduce(command);
    }
    
    public MapReduceOutput mapReduce(final MapReduceCommand command) {
        final ReadPreference readPreference = (command.getReadPreference() == null) ? this.getReadPreference() : command.getReadPreference();
        if (command.getOutputType() == MapReduceCommand.OutputType.INLINE) {
            final MapReduceWithInlineResultsOperation<DBObject> operation = new MapReduceWithInlineResultsOperation<DBObject>(this.getNamespace(), new BsonJavaScript(command.getMap()), new BsonJavaScript(command.getReduce()), this.getDefaultDBObjectCodec()).readConcern(this.getReadConcern()).filter(this.wrapAllowNull(command.getQuery())).limit(command.getLimit()).maxTime(command.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS).jsMode(command.getJsMode() != null && command.getJsMode()).sort(this.wrapAllowNull(command.getSort())).verbose(command.isVerbose()).collation(command.getCollation());
            if (command.getScope() != null) {
                operation.scope(this.wrap(new BasicDBObject(command.getScope())));
            }
            if (command.getFinalize() != null) {
                operation.finalizeFunction(new BsonJavaScript(command.getFinalize()));
            }
            final MapReduceBatchCursor<DBObject> executionResult = this.executor.execute((ReadOperation<MapReduceBatchCursor<DBObject>>)operation, readPreference);
            return new MapReduceOutput(command.toDBObject(), executionResult);
        }
        String action = null;
        switch (command.getOutputType()) {
            case REPLACE: {
                action = "replace";
                break;
            }
            case MERGE: {
                action = "merge";
                break;
            }
            case REDUCE: {
                action = "reduce";
                break;
            }
            default: {
                throw new IllegalArgumentException("Unexpected output type");
            }
        }
        final MapReduceToCollectionOperation operation2 = new MapReduceToCollectionOperation(this.getNamespace(), new BsonJavaScript(command.getMap()), new BsonJavaScript(command.getReduce()), command.getOutputTarget(), this.getWriteConcern()).filter(this.wrapAllowNull(command.getQuery())).limit(command.getLimit()).maxTime(command.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS).jsMode(command.getJsMode() != null && command.getJsMode()).sort(this.wrapAllowNull(command.getSort())).verbose(command.isVerbose()).action(action).databaseName(command.getOutputDB()).bypassDocumentValidation(command.getBypassDocumentValidation()).collation(command.getCollation());
        if (command.getScope() != null) {
            operation2.scope(this.wrap(new BasicDBObject(command.getScope())));
        }
        if (command.getFinalize() != null) {
            operation2.finalizeFunction(new BsonJavaScript(command.getFinalize()));
        }
        try {
            final MapReduceStatistics mapReduceStatistics = this.executor.execute((WriteOperation<MapReduceStatistics>)operation2);
            final DBCollection mapReduceOutputCollection = this.getMapReduceOutputCollection(command);
            final DBCursor executionResult2 = mapReduceOutputCollection.find();
            return new MapReduceOutput(command.toDBObject(), executionResult2, mapReduceStatistics, mapReduceOutputCollection);
        }
        catch (MongoWriteConcernException e) {
            throw createWriteConcernException(e);
        }
    }
    
    private DBCollection getMapReduceOutputCollection(final MapReduceCommand command) {
        final String requestedDatabaseName = command.getOutputDB();
        final DB database = (requestedDatabaseName != null) ? this.getDB().getSisterDB(requestedDatabaseName) : this.getDB();
        return database.getCollection(command.getOutputTarget());
    }
    
    @Deprecated
    public AggregationOutput aggregate(final DBObject firstOp, final DBObject... additionalOps) {
        final List<DBObject> pipeline = new ArrayList<DBObject>();
        pipeline.add(firstOp);
        Collections.addAll(pipeline, additionalOps);
        return this.aggregate(pipeline);
    }
    
    public AggregationOutput aggregate(final List<? extends DBObject> pipeline) {
        return this.aggregate(pipeline, this.getReadPreference());
    }
    
    public AggregationOutput aggregate(final List<? extends DBObject> pipeline, final ReadPreference readPreference) {
        final Cursor cursor = this.aggregate(pipeline, AggregationOptions.builder().outputMode(AggregationOptions.OutputMode.INLINE).build(), readPreference, false);
        if (cursor == null) {
            return new AggregationOutput(Collections.emptyList());
        }
        final List<DBObject> results = new ArrayList<DBObject>();
        while (cursor.hasNext()) {
            results.add(cursor.next());
        }
        return new AggregationOutput(results);
    }
    
    public Cursor aggregate(final List<? extends DBObject> pipeline, final AggregationOptions options) {
        return this.aggregate(pipeline, options, this.getReadPreference());
    }
    
    public Cursor aggregate(final List<? extends DBObject> pipeline, final AggregationOptions options, final ReadPreference readPreference) {
        return this.aggregate(pipeline, options, readPreference, true);
    }
    
    private Cursor aggregate(final List<? extends DBObject> pipeline, final AggregationOptions options, final ReadPreference readPreference, final boolean returnCursorForOutCollection) {
        if (options == null) {
            throw new IllegalArgumentException("options can not be null");
        }
        final List<BsonDocument> stages = this.preparePipeline(pipeline);
        final BsonValue outCollection = stages.get(stages.size() - 1).get("$out");
        if (outCollection != null) {
            final AggregateToCollectionOperation operation = new AggregateToCollectionOperation(this.getNamespace(), stages, this.getWriteConcern()).maxTime(options.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS).allowDiskUse(options.getAllowDiskUse()).bypassDocumentValidation(options.getBypassDocumentValidation()).collation(options.getCollation());
            try {
                this.executor.execute((WriteOperation<Object>)operation);
                if (returnCursorForOutCollection) {
                    return new DBCursor(this.database.getCollection(outCollection.asString().getValue()), new BasicDBObject(), new DBCollectionFindOptions().readPreference(ReadPreference.primary()).collation(options.getCollation()));
                }
                return null;
            }
            catch (MongoWriteConcernException e) {
                throw createWriteConcernException(e);
            }
        }
        final AggregateOperation<DBObject> operation2 = new AggregateOperation<DBObject>(this.getNamespace(), stages, this.getDefaultDBObjectCodec()).readConcern(this.getReadConcern()).maxTime(options.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS).allowDiskUse(options.getAllowDiskUse()).batchSize(options.getBatchSize()).useCursor(options.getOutputMode() == AggregationOptions.OutputMode.CURSOR).collation(options.getCollation());
        final BatchCursor<DBObject> cursor = this.executor.execute((ReadOperation<BatchCursor<DBObject>>)operation2, readPreference);
        return new MongoCursorAdapter(new MongoBatchCursorAdapter<DBObject>(cursor));
    }
    
    public CommandResult explainAggregate(final List<? extends DBObject> pipeline, final AggregationOptions options) {
        final AggregateOperation<BsonDocument> operation = new AggregateOperation<BsonDocument>(this.getNamespace(), this.preparePipeline(pipeline), new BsonDocumentCodec()).maxTime(options.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS).allowDiskUse(options.getAllowDiskUse()).collation(options.getCollation());
        return new CommandResult(this.executor.execute(operation.asExplainableOperation(ExplainVerbosity.QUERY_PLANNER), ReadPreference.primaryPreferred()));
    }
    
    List<BsonDocument> preparePipeline(final List<? extends DBObject> pipeline) {
        final List<BsonDocument> stages = new ArrayList<BsonDocument>();
        for (final DBObject op : pipeline) {
            stages.add(this.wrap(op));
        }
        return stages;
    }
    
    public List<Cursor> parallelScan(final ParallelScanOptions options) {
        final List<Cursor> cursors = new ArrayList<Cursor>();
        final ParallelCollectionScanOperation<DBObject> operation = new ParallelCollectionScanOperation<DBObject>(this.getNamespace(), options.getNumCursors(), this.objectCodec).readConcern(this.getReadConcern()).batchSize(options.getBatchSize());
        final List<BatchCursor<DBObject>> mongoCursors = this.executor.execute((ReadOperation<List<BatchCursor<DBObject>>>)operation, (options.getReadPreference() != null) ? options.getReadPreference() : this.getReadPreference());
        for (final BatchCursor<DBObject> mongoCursor : mongoCursors) {
            cursors.add(new MongoCursorAdapter(new MongoBatchCursorAdapter<DBObject>(mongoCursor)));
        }
        return cursors;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getFullName() {
        return this.getNamespace().getFullName();
    }
    
    public DBCollection getCollection(final String name) {
        return this.database.getCollection(this.getName() + "." + name);
    }
    
    public void createIndex(final String name) {
        this.createIndex(new BasicDBObject(name, 1));
    }
    
    public void createIndex(final DBObject keys, final String name) {
        this.createIndex(keys, name, false);
    }
    
    public void createIndex(final DBObject keys, final String name, final boolean unique) {
        final DBObject options = new BasicDBObject();
        if (name != null && name.length() > 0) {
            options.put("name", name);
        }
        if (unique) {
            options.put("unique", Boolean.TRUE);
        }
        this.createIndex(keys, options);
    }
    
    public void createIndex(final DBObject keys) {
        this.createIndex(keys, new BasicDBObject());
    }
    
    public void createIndex(final DBObject keys, final DBObject options) {
        try {
            this.executor.execute((WriteOperation<Object>)this.createIndexOperation(keys, options));
        }
        catch (MongoWriteConcernException e) {
            throw createWriteConcernException(e);
        }
    }
    
    public List<DBObject> getHintFields() {
        return this.hintFields;
    }
    
    public void setHintFields(final List<? extends DBObject> indexes) {
        this.hintFields = new ArrayList<DBObject>(indexes);
    }
    
    public DBObject findAndModify(final DBObject query, final DBObject sort, final DBObject update) {
        return this.findAndModify(query, null, sort, false, update, false, false);
    }
    
    public DBObject findAndModify(final DBObject query, final DBObject update) {
        return this.findAndModify(query, null, null, false, update, false, false);
    }
    
    public DBObject findAndRemove(final DBObject query) {
        return this.findAndModify(query, null, null, true, null, false, false);
    }
    
    public DBObject findAndModify(final DBObject query, final DBObject fields, final DBObject sort, final boolean remove, final DBObject update, final boolean returnNew, final boolean upsert) {
        return this.findAndModify(query, fields, sort, remove, update, returnNew, upsert, 0L, TimeUnit.MILLISECONDS);
    }
    
    public DBObject findAndModify(final DBObject query, final DBObject fields, final DBObject sort, final boolean remove, final DBObject update, final boolean returnNew, final boolean upsert, final WriteConcern writeConcern) {
        return this.findAndModify(query, fields, sort, remove, update, returnNew, upsert, 0L, TimeUnit.MILLISECONDS, writeConcern);
    }
    
    public DBObject findAndModify(final DBObject query, final DBObject fields, final DBObject sort, final boolean remove, final DBObject update, final boolean returnNew, final boolean upsert, final long maxTime, final TimeUnit maxTimeUnit) {
        return this.findAndModify(query, fields, sort, remove, update, returnNew, upsert, maxTime, maxTimeUnit, this.getWriteConcern());
    }
    
    public DBObject findAndModify(final DBObject query, final DBObject fields, final DBObject sort, final boolean remove, final DBObject update, final boolean returnNew, final boolean upsert, final long maxTime, final TimeUnit maxTimeUnit, final WriteConcern writeConcern) {
        return this.findAndModify((query != null) ? query : new BasicDBObject(), new DBCollectionFindAndModifyOptions().projection(fields).sort(sort).remove(remove).update(update).returnNew(returnNew).upsert(upsert).maxTime(maxTime, maxTimeUnit).writeConcern(writeConcern));
    }
    
    public DBObject findAndModify(final DBObject query, final DBObject fields, final DBObject sort, final boolean remove, final DBObject update, final boolean returnNew, final boolean upsert, final boolean bypassDocumentValidation, final long maxTime, final TimeUnit maxTimeUnit) {
        return this.findAndModify(query, fields, sort, remove, update, returnNew, upsert, bypassDocumentValidation, maxTime, maxTimeUnit, this.getWriteConcern());
    }
    
    public DBObject findAndModify(final DBObject query, final DBObject fields, final DBObject sort, final boolean remove, final DBObject update, final boolean returnNew, final boolean upsert, final boolean bypassDocumentValidation, final long maxTime, final TimeUnit maxTimeUnit, final WriteConcern writeConcern) {
        return this.findAndModify((query != null) ? query : new BasicDBObject(), new DBCollectionFindAndModifyOptions().projection(fields).sort(sort).remove(remove).update(update).returnNew(returnNew).upsert(upsert).bypassDocumentValidation(bypassDocumentValidation).maxTime(maxTime, maxTimeUnit).writeConcern(writeConcern));
    }
    
    public DBObject findAndModify(final DBObject query, final DBCollectionFindAndModifyOptions options) {
        Assertions.notNull("query", query);
        Assertions.notNull("options", options);
        final WriteConcern writeConcern = (options.getWriteConcern() != null) ? options.getWriteConcern() : this.getWriteConcern();
        WriteOperation<DBObject> operation;
        if (options.isRemove()) {
            operation = (WriteOperation<DBObject>)new FindAndDeleteOperation(this.getNamespace(), writeConcern, this.objectCodec).filter(this.wrapAllowNull(query)).projection(this.wrapAllowNull(options.getProjection())).sort(this.wrapAllowNull(options.getSort())).maxTime(options.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS).collation(options.getCollation());
        }
        else {
            Assertions.notNull("options#getUpdate", options.getUpdate());
            if (!options.getUpdate().keySet().isEmpty() && options.getUpdate().keySet().iterator().next().charAt(0) == '$') {
                operation = (WriteOperation<DBObject>)new FindAndUpdateOperation(this.getNamespace(), writeConcern, this.objectCodec, this.wrap(options.getUpdate())).filter(this.wrap(query)).projection(this.wrapAllowNull(options.getProjection())).sort(this.wrapAllowNull(options.getSort())).returnOriginal(!options.returnNew()).upsert(options.isUpsert()).maxTime(options.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS).bypassDocumentValidation(options.getBypassDocumentValidation()).collation(options.getCollation());
            }
            else {
                operation = (WriteOperation<DBObject>)new FindAndReplaceOperation(this.getNamespace(), writeConcern, this.objectCodec, this.wrap(options.getUpdate())).filter(this.wrap(query)).projection(this.wrapAllowNull(options.getProjection())).sort(this.wrapAllowNull(options.getSort())).returnOriginal(!options.returnNew()).upsert(options.isUpsert()).maxTime(options.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS).bypassDocumentValidation(options.getBypassDocumentValidation()).collation(options.getCollation());
            }
        }
        try {
            return this.executor.execute(operation);
        }
        catch (MongoWriteConcernException e) {
            throw createWriteConcernException(e);
        }
    }
    
    public DB getDB() {
        return this.database;
    }
    
    public WriteConcern getWriteConcern() {
        if (this.writeConcern != null) {
            return this.writeConcern;
        }
        return this.database.getWriteConcern();
    }
    
    public void setWriteConcern(final WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
    }
    
    public ReadPreference getReadPreference() {
        if (this.readPreference != null) {
            return this.readPreference;
        }
        return this.database.getReadPreference();
    }
    
    public void setReadPreference(final ReadPreference preference) {
        this.readPreference = preference;
    }
    
    public void setReadConcern(final ReadConcern readConcern) {
        this.readConcern = readConcern;
    }
    
    public ReadConcern getReadConcern() {
        if (this.readConcern != null) {
            return this.readConcern;
        }
        return this.database.getReadConcern();
    }
    
    @Deprecated
    public void slaveOk() {
        this.addOption(4);
    }
    
    public void addOption(final int option) {
        this.optionHolder.add(option);
    }
    
    public void resetOptions() {
        this.optionHolder.reset();
    }
    
    public int getOptions() {
        return this.optionHolder.get();
    }
    
    public void setOptions(final int options) {
        this.optionHolder.set(options);
    }
    
    public void drop() {
        try {
            this.executor.execute((WriteOperation<Object>)new DropCollectionOperation(this.getNamespace(), this.getWriteConcern()));
        }
        catch (MongoWriteConcernException e) {
            throw createWriteConcernException(e);
        }
    }
    
    public synchronized DBDecoderFactory getDBDecoderFactory() {
        return this.decoderFactory;
    }
    
    public synchronized void setDBDecoderFactory(final DBDecoderFactory factory) {
        this.decoderFactory = factory;
        final Decoder<DBObject> decoder = (Decoder<DBObject>)((factory == null || factory == DefaultDBDecoder.FACTORY) ? this.getDefaultDBObjectCodec() : new DBDecoderAdapter(factory.create(), this, this.getBufferPool()));
        this.objectCodec = new CompoundDBObjectCodec(this.objectCodec.getEncoder(), decoder);
    }
    
    public synchronized DBEncoderFactory getDBEncoderFactory() {
        return this.encoderFactory;
    }
    
    public synchronized void setDBEncoderFactory(final DBEncoderFactory factory) {
        this.encoderFactory = factory;
        final Encoder<DBObject> encoder = (Encoder<DBObject>)((factory == null || factory == DefaultDBEncoder.FACTORY) ? this.getDefaultDBObjectCodec() : new DBEncoderFactoryAdapter(this.encoderFactory));
        this.objectCodec = new CompoundDBObjectCodec(encoder, this.objectCodec.getDecoder());
    }
    
    public List<DBObject> getIndexInfo() {
        return new OperationIterable((ReadOperation<? extends BatchCursor<Object>>)new ListIndexesOperation(this.getNamespace(), (Decoder<Object>)this.getDefaultDBObjectCodec()), ReadPreference.primary(), this.executor).into(new ArrayList<DBObject>());
    }
    
    public void dropIndex(final DBObject index) {
        this.dropIndex(this.getIndexNameFromIndexFields(index));
    }
    
    public void dropIndex(final String indexName) {
        try {
            this.executor.execute((WriteOperation<Object>)new DropIndexOperation(this.getNamespace(), indexName, this.getWriteConcern()));
        }
        catch (MongoWriteConcernException e) {
            throw createWriteConcernException(e);
        }
    }
    
    public void dropIndexes() {
        this.dropIndex("*");
    }
    
    public void dropIndexes(final String indexName) {
        this.dropIndex(indexName);
    }
    
    public CommandResult getStats() {
        return this.getDB().executeCommand(new BsonDocument("collStats", new BsonString(this.getName())), this.getReadPreference());
    }
    
    public boolean isCapped() {
        final CommandResult commandResult = this.getStats();
        final Object cappedField = commandResult.get("capped");
        return cappedField != null && (cappedField.equals(1) || cappedField.equals(true));
    }
    
    public Class getObjectClass() {
        return this.objectFactory.getClassForPath(Collections.emptyList());
    }
    
    public void setObjectClass(final Class<? extends DBObject> aClass) {
        this.setObjectFactory(this.objectFactory.update(aClass));
    }
    
    public void setInternalClass(final String path, final Class<? extends DBObject> aClass) {
        this.setObjectFactory(this.objectFactory.update(aClass, Arrays.asList(path.split("\\."))));
    }
    
    protected Class<? extends DBObject> getInternalClass(final String path) {
        return this.objectFactory.getClassForPath(Arrays.asList(path.split("\\.")));
    }
    
    @Override
    public String toString() {
        return "DBCollection{database=" + this.database + ", name='" + this.name + '\'' + '}';
    }
    
    synchronized DBObjectFactory getObjectFactory() {
        return this.objectFactory;
    }
    
    synchronized void setObjectFactory(final DBCollectionObjectFactory factory) {
        this.objectFactory = factory;
        this.objectCodec = new CompoundDBObjectCodec(this.objectCodec.getEncoder(), this.getDefaultDBObjectCodec());
    }
    
    public BulkWriteOperation initializeOrderedBulkOperation() {
        return new BulkWriteOperation(true, this);
    }
    
    public BulkWriteOperation initializeUnorderedBulkOperation() {
        return new BulkWriteOperation(false, this);
    }
    
    BulkWriteResult executeBulkWriteOperation(final boolean ordered, final Boolean bypassDocumentValidation, final List<com.mongodb.WriteRequest> writeRequests) {
        return this.executeBulkWriteOperation(ordered, bypassDocumentValidation, writeRequests, this.getWriteConcern());
    }
    
    BulkWriteResult executeBulkWriteOperation(final boolean ordered, final Boolean bypassDocumentValidation, final List<com.mongodb.WriteRequest> writeRequests, final WriteConcern writeConcern) {
        try {
            return BulkWriteHelper.translateBulkWriteResult(this.executor.execute((WriteOperation<com.mongodb.bulk.BulkWriteResult>)new MixedBulkWriteOperation(this.getNamespace(), BulkWriteHelper.translateWriteRequestsToNew(writeRequests), ordered, writeConcern).bypassDocumentValidation(bypassDocumentValidation)), this.getObjectCodec());
        }
        catch (MongoBulkWriteException e) {
            throw BulkWriteHelper.translateBulkWriteException(e, MongoClient.getDefaultCodecRegistry().get(DBObject.class));
        }
    }
    
    DBObjectCodec getDefaultDBObjectCodec() {
        return new DBObjectCodec(MongoClient.getDefaultCodecRegistry(), DBObjectCodec.getDefaultBsonTypeClassMap(), this.getObjectFactory());
    }
    
    private <T> T convertOptionsToType(final DBObject options, final String field, final Class<T> clazz) {
        return this.convertToType(clazz, options.get(field), String.format("'%s' should be of class %s", field, clazz.getSimpleName()));
    }
    
    private <T> T convertToType(final Class<T> clazz, final Object value, final String errorMessage) {
        Object transformedValue = value;
        if (clazz == Boolean.class) {
            if (value instanceof Boolean) {
                transformedValue = value;
            }
            else if (value instanceof Number) {
                transformedValue = (((Number)value).doubleValue() != 0.0);
            }
        }
        else if (clazz == Double.class) {
            if (value instanceof Number) {
                transformedValue = ((Number)value).doubleValue();
            }
        }
        else if (clazz == Integer.class) {
            if (value instanceof Number) {
                transformedValue = ((Number)value).intValue();
            }
        }
        else if (clazz == Long.class && value instanceof Number) {
            transformedValue = ((Number)value).longValue();
        }
        if (!clazz.isAssignableFrom(transformedValue.getClass())) {
            throw new IllegalArgumentException(errorMessage);
        }
        return (T)transformedValue;
    }
    
    private CreateIndexesOperation createIndexOperation(final DBObject key, final DBObject options) {
        final IndexRequest request = new IndexRequest(this.wrap(key));
        if (options.containsField("name")) {
            request.name(this.convertOptionsToType(options, "name", String.class));
        }
        if (options.containsField("background")) {
            request.background(this.convertOptionsToType(options, "background", Boolean.class));
        }
        if (options.containsField("unique")) {
            request.unique(this.convertOptionsToType(options, "unique", Boolean.class));
        }
        if (options.containsField("sparse")) {
            request.sparse(this.convertOptionsToType(options, "sparse", Boolean.class));
        }
        if (options.containsField("expireAfterSeconds")) {
            request.expireAfter(this.convertOptionsToType(options, "expireAfterSeconds", Long.class), TimeUnit.SECONDS);
        }
        if (options.containsField("v")) {
            request.version(this.convertOptionsToType(options, "v", Integer.class));
        }
        if (options.containsField("weights")) {
            request.weights(this.wrap(this.convertOptionsToType(options, "weights", DBObject.class)));
        }
        if (options.containsField("default_language")) {
            request.defaultLanguage(this.convertOptionsToType(options, "default_language", String.class));
        }
        if (options.containsField("language_override")) {
            request.languageOverride(this.convertOptionsToType(options, "language_override", String.class));
        }
        if (options.containsField("textIndexVersion")) {
            request.textVersion(this.convertOptionsToType(options, "textIndexVersion", Integer.class));
        }
        if (options.containsField("2dsphereIndexVersion")) {
            request.sphereVersion(this.convertOptionsToType(options, "2dsphereIndexVersion", Integer.class));
        }
        if (options.containsField("bits")) {
            request.bits(this.convertOptionsToType(options, "bits", Integer.class));
        }
        if (options.containsField("min")) {
            request.min(this.convertOptionsToType(options, "min", Double.class));
        }
        if (options.containsField("max")) {
            request.max(this.convertOptionsToType(options, "max", Double.class));
        }
        if (options.containsField("bucketSize")) {
            request.bucketSize(this.convertOptionsToType(options, "bucketSize", Double.class));
        }
        if (options.containsField("dropDups")) {
            request.dropDups(this.convertOptionsToType(options, "dropDups", Boolean.class));
        }
        if (options.containsField("storageEngine")) {
            request.storageEngine(this.wrap(this.convertOptionsToType(options, "storageEngine", DBObject.class)));
        }
        if (options.containsField("partialFilterExpression")) {
            request.partialFilterExpression(this.wrap(this.convertOptionsToType(options, "partialFilterExpression", DBObject.class)));
        }
        if (options.containsField("collation")) {
            request.collation(DBObjectCollationHelper.createCollationFromOptions(options));
        }
        return new CreateIndexesOperation(this.getNamespace(), Collections.singletonList(request), this.writeConcern);
    }
    
    private String getIndexNameFromIndexFields(final DBObject index) {
        final StringBuilder indexName = new StringBuilder();
        for (final String keyNames : index.keySet()) {
            if (indexName.length() != 0) {
                indexName.append('_');
            }
            indexName.append(keyNames).append('_');
            final Object keyType = index.get(keyNames);
            if (keyType instanceof Integer) {
                final List<Integer> validIndexTypes = Arrays.asList(1, -1);
                if (!validIndexTypes.contains(keyType)) {
                    throw new UnsupportedOperationException("Unsupported index type: " + keyType);
                }
                indexName.append(keyType);
            }
            else {
                if (!(keyType instanceof String)) {
                    continue;
                }
                final List<String> validIndexTypes2 = Arrays.asList("2d", "2dsphere", "text", "geoHaystack", "hashed");
                if (!validIndexTypes2.contains(keyType)) {
                    throw new UnsupportedOperationException("Unsupported index type: " + keyType);
                }
                indexName.append(((String)keyType).replace(' ', '_'));
            }
        }
        return indexName.toString();
    }
    
    Codec<DBObject> getObjectCodec() {
        return this.objectCodec;
    }
    
    OperationExecutor getExecutor() {
        return this.executor;
    }
    
    MongoNamespace getNamespace() {
        return new MongoNamespace(this.getDB().getName(), this.getName());
    }
    
    BufferProvider getBufferPool() {
        return this.getDB().getBufferPool();
    }
    
    BsonDocument wrapAllowNull(final DBObject document) {
        if (document == null) {
            return null;
        }
        return this.wrap(document);
    }
    
    BsonDocument wrap(final DBObject document) {
        return new BsonDocumentWrapper<Object>(document, this.getDefaultDBObjectCodec());
    }
    
    BsonDocument wrap(final DBObject document, final DBEncoder encoder) {
        if (encoder == null) {
            return this.wrap(document);
        }
        return new BsonDocumentWrapper<Object>(document, new DBEncoderAdapter(encoder));
    }
    
    BsonDocument wrap(final DBObject document, final Encoder<DBObject> encoder) {
        if (encoder == null) {
            return this.wrap(document);
        }
        return new BsonDocumentWrapper<Object>(document, encoder);
    }
    
    static WriteConcernException createWriteConcernException(final MongoWriteConcernException e) {
        return new WriteConcernException(new BsonDocument("code", new BsonInt32(e.getWriteConcernError().getCode())).append("errmsg", new BsonString(e.getWriteConcernError().getMessage())), e.getServerAddress(), e.getWriteResult());
    }
}
