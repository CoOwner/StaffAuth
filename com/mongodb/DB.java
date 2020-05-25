package com.mongodb;

import com.mongodb.annotations.*;
import java.util.concurrent.*;
import com.mongodb.assertions.*;
import com.mongodb.client.model.*;
import com.mongodb.operation.*;
import com.mongodb.connection.*;
import org.bson.*;
import org.bson.codecs.*;
import java.util.*;

@ThreadSafe
public class DB
{
    private final Mongo mongo;
    private final String name;
    private final OperationExecutor executor;
    private final ConcurrentHashMap<String, DBCollection> collectionCache;
    private final Bytes.OptionHolder optionHolder;
    private final Codec<DBObject> commandCodec;
    private volatile ReadPreference readPreference;
    private volatile WriteConcern writeConcern;
    private volatile ReadConcern readConcern;
    private static final Set<String> OBEDIENT_COMMANDS;
    
    DB(final Mongo mongo, final String name, final OperationExecutor executor) {
        MongoNamespace.checkDatabaseNameValidity(name);
        this.mongo = mongo;
        this.name = name;
        this.executor = executor;
        this.collectionCache = new ConcurrentHashMap<String, DBCollection>();
        this.optionHolder = new Bytes.OptionHolder(mongo.getOptionHolder());
        this.commandCodec = MongoClient.getCommandCodec();
    }
    
    public DB(final Mongo mongo, final String name) {
        this(mongo, name, mongo.createOperationExecutor());
    }
    
    public Mongo getMongo() {
        return this.mongo;
    }
    
    public void setReadPreference(final ReadPreference readPreference) {
        this.readPreference = readPreference;
    }
    
    public void setWriteConcern(final WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
    }
    
    public ReadPreference getReadPreference() {
        return (this.readPreference != null) ? this.readPreference : this.mongo.getReadPreference();
    }
    
    public WriteConcern getWriteConcern() {
        return (this.writeConcern != null) ? this.writeConcern : this.mongo.getWriteConcern();
    }
    
    public void setReadConcern(final ReadConcern readConcern) {
        this.readConcern = readConcern;
    }
    
    public ReadConcern getReadConcern() {
        return (this.readConcern != null) ? this.readConcern : this.mongo.getReadConcern();
    }
    
    protected DBCollection doGetCollection(final String name) {
        return this.getCollection(name);
    }
    
    public DBCollection getCollection(final String name) {
        DBCollection collection = this.collectionCache.get(name);
        if (collection != null) {
            return collection;
        }
        collection = new DBCollection(name, this, this.executor);
        if (this.mongo.getMongoClientOptions().getDbDecoderFactory() != DefaultDBDecoder.FACTORY) {
            collection.setDBDecoderFactory(this.mongo.getMongoClientOptions().getDbDecoderFactory());
        }
        if (this.mongo.getMongoClientOptions().getDbEncoderFactory() != DefaultDBEncoder.FACTORY) {
            collection.setDBEncoderFactory(this.mongo.getMongoClientOptions().getDbEncoderFactory());
        }
        final DBCollection old = this.collectionCache.putIfAbsent(name, collection);
        return (old != null) ? old : collection;
    }
    
    public void dropDatabase() {
        try {
            this.getExecutor().execute((WriteOperation<Object>)new DropDatabaseOperation(this.getName(), this.getWriteConcern()));
        }
        catch (MongoWriteConcernException e) {
            throw DBCollection.createWriteConcernException(e);
        }
    }
    
    public DBCollection getCollectionFromString(final String collectionName) {
        return this.getCollection(collectionName);
    }
    
    public String getName() {
        return this.name;
    }
    
    public Set<String> getCollectionNames() {
        final List<String> collectionNames = (List<String>)new OperationIterable(new ListCollectionsOperation(this.name, this.commandCodec), ReadPreference.primary(), this.executor).map((Function)new Function<DBObject, String>() {
            @Override
            public String apply(final DBObject result) {
                return (String)result.get("name");
            }
        }).into(new ArrayList());
        Collections.sort(collectionNames);
        return new LinkedHashSet<String>(collectionNames);
    }
    
    public DBCollection createCollection(final String collectionName, final DBObject options) {
        if (options != null) {
            try {
                this.executor.execute((WriteOperation<Object>)this.getCreateCollectionOperation(collectionName, options));
            }
            catch (MongoWriteConcernException e) {
                throw DBCollection.createWriteConcernException(e);
            }
        }
        return this.getCollection(collectionName);
    }
    
    public DBCollection createView(final String viewName, final String viewOn, final List<? extends DBObject> pipeline) {
        return this.createView(viewName, viewOn, pipeline, new DBCreateViewOptions());
    }
    
    public DBCollection createView(final String viewName, final String viewOn, final List<? extends DBObject> pipeline, final DBCreateViewOptions options) {
        try {
            Assertions.notNull("options", options);
            final DBCollection view = this.getCollection(viewName);
            this.executor.execute((WriteOperation<Object>)new CreateViewOperation(this.name, viewName, viewOn, view.preparePipeline(pipeline), this.writeConcern).collation(options.getCollation()));
            return view;
        }
        catch (MongoWriteConcernException e) {
            throw DBCollection.createWriteConcernException(e);
        }
    }
    
    private CreateCollectionOperation getCreateCollectionOperation(final String collectionName, final DBObject options) {
        if (options.get("size") != null && !(options.get("size") instanceof Number)) {
            throw new IllegalArgumentException("'size' should be Number");
        }
        if (options.get("max") != null && !(options.get("max") instanceof Number)) {
            throw new IllegalArgumentException("'max' should be Number");
        }
        if (options.get("capped") != null && !(options.get("capped") instanceof Boolean)) {
            throw new IllegalArgumentException("'capped' should be Boolean");
        }
        if (options.get("autoIndexId") != null && !(options.get("autoIndexId") instanceof Boolean)) {
            throw new IllegalArgumentException("'autoIndexId' should be Boolean");
        }
        if (options.get("storageEngine") != null && !(options.get("storageEngine") instanceof DBObject)) {
            throw new IllegalArgumentException("'storageEngine' should be DBObject");
        }
        if (options.get("indexOptionDefaults") != null && !(options.get("indexOptionDefaults") instanceof DBObject)) {
            throw new IllegalArgumentException("'indexOptionDefaults' should be DBObject");
        }
        if (options.get("validator") != null && !(options.get("validator") instanceof DBObject)) {
            throw new IllegalArgumentException("'validator' should be DBObject");
        }
        if (options.get("validationLevel") != null && !(options.get("validationLevel") instanceof String)) {
            throw new IllegalArgumentException("'validationLevel' should be String");
        }
        if (options.get("validationAction") != null && !(options.get("validationAction") instanceof String)) {
            throw new IllegalArgumentException("'validationAction' should be String");
        }
        boolean capped = false;
        boolean autoIndex = true;
        long sizeInBytes = 0L;
        long maxDocuments = 0L;
        Boolean usePowerOfTwoSizes = null;
        BsonDocument storageEngineOptions = null;
        BsonDocument indexOptionDefaults = null;
        BsonDocument validator = null;
        ValidationLevel validationLevel = null;
        ValidationAction validationAction = null;
        if (options.get("capped") != null) {
            capped = (boolean)options.get("capped");
        }
        if (options.get("size") != null) {
            sizeInBytes = ((Number)options.get("size")).longValue();
        }
        if (options.get("autoIndexId") != null) {
            autoIndex = (boolean)options.get("autoIndexId");
        }
        if (options.get("max") != null) {
            maxDocuments = ((Number)options.get("max")).longValue();
        }
        if (options.get("usePowerOfTwoSizes") != null) {
            usePowerOfTwoSizes = (Boolean)options.get("usePowerOfTwoSizes");
        }
        if (options.get("storageEngine") != null) {
            storageEngineOptions = this.wrap((DBObject)options.get("storageEngine"));
        }
        if (options.get("indexOptionDefaults") != null) {
            indexOptionDefaults = this.wrap((DBObject)options.get("indexOptionDefaults"));
        }
        if (options.get("validator") != null) {
            validator = this.wrap((DBObject)options.get("validator"));
        }
        if (options.get("validationLevel") != null) {
            validationLevel = ValidationLevel.fromString((String)options.get("validationLevel"));
        }
        if (options.get("validationAction") != null) {
            validationAction = ValidationAction.fromString((String)options.get("validationAction"));
        }
        final Collation collation = DBObjectCollationHelper.createCollationFromOptions(options);
        return new CreateCollectionOperation(this.getName(), collectionName, this.getWriteConcern()).capped(capped).collation(collation).sizeInBytes(sizeInBytes).autoIndex(autoIndex).maxDocuments(maxDocuments).usePowerOf2Sizes(usePowerOfTwoSizes).storageEngineOptions(storageEngineOptions).indexOptionDefaults(indexOptionDefaults).validator(validator).validationLevel(validationLevel).validationAction(validationAction);
    }
    
    public CommandResult command(final String command) {
        return this.command(new BasicDBObject(command, Boolean.TRUE), this.getReadPreference());
    }
    
    public CommandResult command(final DBObject command) {
        return this.command(command, this.getReadPreference());
    }
    
    public CommandResult command(final DBObject command, final DBEncoder encoder) {
        return this.command(command, this.getReadPreference(), encoder);
    }
    
    public CommandResult command(final DBObject command, final ReadPreference readPreference, final DBEncoder encoder) {
        try {
            return this.executeCommand(this.wrap(command, encoder), this.getCommandReadPreference(command, readPreference));
        }
        catch (MongoCommandException ex) {
            return new CommandResult(ex.getResponse(), ex.getServerAddress());
        }
    }
    
    public CommandResult command(final DBObject command, final ReadPreference readPreference) {
        return this.command(command, readPreference, null);
    }
    
    public CommandResult command(final String command, final ReadPreference readPreference) {
        return this.command(new BasicDBObject(command, true), readPreference);
    }
    
    public DB getSisterDB(final String name) {
        return this.mongo.getDB(name);
    }
    
    public boolean collectionExists(final String collectionName) {
        final Set<String> collectionNames = this.getCollectionNames();
        for (final String name : collectionNames) {
            if (name.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }
    
    public CommandResult doEval(final String code, final Object... args) {
        final DBObject commandDocument = new BasicDBObject("$eval", code).append("args", Arrays.asList(args));
        return this.executeCommand(this.wrap(commandDocument));
    }
    
    public Object eval(final String code, final Object... args) {
        final CommandResult result = this.doEval(code, args);
        result.throwOnError();
        return result.get("retval");
    }
    
    public CommandResult getStats() {
        final BsonDocument commandDocument = new BsonDocument("dbStats", new BsonInt32(1)).append("scale", new BsonInt32(1));
        return this.executeCommand(commandDocument);
    }
    
    @Deprecated
    public WriteResult addUser(final String userName, final char[] password) {
        return this.addUser(userName, password, false);
    }
    
    @Deprecated
    public WriteResult addUser(final String userName, final char[] password, final boolean readOnly) {
        final MongoCredential credential = MongoCredential.createMongoCRCredential(userName, this.getName(), password);
        boolean userExists = false;
        try {
            userExists = this.executor.execute((ReadOperation<Boolean>)new UserExistsOperation(this.getName(), userName), ReadPreference.primary());
        }
        catch (MongoCommandException e) {
            if (e.getCode() != 13) {
                throw e;
            }
        }
        try {
            if (userExists) {
                this.executor.execute((WriteOperation<Object>)new UpdateUserOperation(credential, readOnly, this.getWriteConcern()));
                return new WriteResult(1, true, null);
            }
            this.executor.execute((WriteOperation<Object>)new CreateUserOperation(credential, readOnly, this.getWriteConcern()));
            return new WriteResult(1, false, null);
        }
        catch (MongoWriteConcernException e2) {
            throw DBCollection.createWriteConcernException(e2);
        }
    }
    
    @Deprecated
    public WriteResult removeUser(final String userName) {
        try {
            this.executor.execute((WriteOperation<Object>)new DropUserOperation(this.getName(), userName, this.getWriteConcern()));
            return new WriteResult(1, true, null);
        }
        catch (MongoWriteConcernException e) {
            throw DBCollection.createWriteConcernException(e);
        }
    }
    
    @Deprecated
    public void slaveOk() {
        this.addOption(4);
    }
    
    public void addOption(final int option) {
        this.optionHolder.add(option);
    }
    
    public void setOptions(final int options) {
        this.optionHolder.set(options);
    }
    
    public void resetOptions() {
        this.optionHolder.reset();
    }
    
    public int getOptions() {
        return this.optionHolder.get();
    }
    
    @Override
    public String toString() {
        return "DB{name='" + this.name + '\'' + '}';
    }
    
    CommandResult executeCommand(final BsonDocument commandDocument) {
        return new CommandResult(this.executor.execute(new CommandWriteOperation<BsonDocument>(this.getName(), commandDocument, new BsonDocumentCodec())));
    }
    
    CommandResult executeCommand(final BsonDocument commandDocument, final ReadPreference readPreference) {
        return new CommandResult(this.executor.execute(new CommandReadOperation<BsonDocument>(this.getName(), commandDocument, new BsonDocumentCodec()), readPreference));
    }
    
    OperationExecutor getExecutor() {
        return this.executor;
    }
    
    Bytes.OptionHolder getOptionHolder() {
        return this.optionHolder;
    }
    
    BufferProvider getBufferPool() {
        return this.getMongo().getBufferProvider();
    }
    
    private BsonDocument wrap(final DBObject document) {
        return new BsonDocumentWrapper<Object>(document, this.commandCodec);
    }
    
    private BsonDocument wrap(final DBObject document, final DBEncoder encoder) {
        if (encoder == null) {
            return this.wrap(document);
        }
        return new BsonDocumentWrapper<Object>(document, new DBEncoderAdapter(encoder));
    }
    
    ReadPreference getCommandReadPreference(final DBObject command, final ReadPreference requestedPreference) {
        final String comString = command.keySet().iterator().next().toLowerCase();
        final boolean primaryRequired = !DB.OBEDIENT_COMMANDS.contains(comString);
        if (primaryRequired) {
            return ReadPreference.primary();
        }
        if (requestedPreference == null) {
            return ReadPreference.primary();
        }
        return requestedPreference;
    }
    
    static {
        (OBEDIENT_COMMANDS = new HashSet<String>()).add("aggregate");
        DB.OBEDIENT_COMMANDS.add("collstats");
        DB.OBEDIENT_COMMANDS.add("count");
        DB.OBEDIENT_COMMANDS.add("dbstats");
        DB.OBEDIENT_COMMANDS.add("distinct");
        DB.OBEDIENT_COMMANDS.add("geonear");
        DB.OBEDIENT_COMMANDS.add("geosearch");
        DB.OBEDIENT_COMMANDS.add("geowalk");
        DB.OBEDIENT_COMMANDS.add("group");
        DB.OBEDIENT_COMMANDS.add("listcollections");
        DB.OBEDIENT_COMMANDS.add("listindexes");
        DB.OBEDIENT_COMMANDS.add("parallelcollectionscan");
        DB.OBEDIENT_COMMANDS.add("text");
    }
}
