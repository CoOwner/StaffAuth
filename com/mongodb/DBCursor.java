package com.mongodb;

import com.mongodb.annotations.*;
import org.bson.codecs.*;
import com.mongodb.client.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import org.bson.*;
import com.mongodb.client.model.*;
import com.mongodb.operation.*;
import java.util.*;

@NotThreadSafe
public class DBCursor implements Cursor, Iterable<DBObject>
{
    private final DBCollection collection;
    private final DBObject filter;
    private final DBCollectionFindOptions findOptions;
    private final OperationExecutor executor;
    private int options;
    private DBDecoderFactory decoderFactory;
    private Decoder<DBObject> decoder;
    private IteratorOrArray iteratorOrArray;
    private DBObject currentObject;
    private int numSeen;
    private boolean closed;
    private final List<DBObject> all;
    private MongoCursor<DBObject> cursor;
    private OptionalFinalizer optionalFinalizer;
    
    public DBCursor(final DBCollection collection, final DBObject query, final DBObject fields, final ReadPreference readPreference) {
        this(collection, query, new DBCollectionFindOptions().projection(fields).readPreference(readPreference));
        this.addOption(collection.getOptions());
        final DBObject indexKeys = lookupSuitableHints(query, collection.getHintFields());
        if (indexKeys != null) {
            this.hint(indexKeys);
        }
    }
    
    DBCursor(final DBCollection collection, final DBObject filter, final DBCollectionFindOptions findOptions) {
        this(collection, filter, findOptions, collection.getExecutor(), collection.getDBDecoderFactory(), collection.getObjectCodec());
    }
    
    private DBCursor(final DBCollection collection, final DBObject filter, final DBCollectionFindOptions findOptions, final OperationExecutor executor, final DBDecoderFactory decoderFactory, final Decoder<DBObject> decoder) {
        this.all = new ArrayList<DBObject>();
        this.collection = Assertions.notNull("collection", collection);
        this.filter = filter;
        this.executor = Assertions.notNull("executor", executor);
        this.findOptions = Assertions.notNull("findOptions", findOptions.copy());
        this.decoderFactory = decoderFactory;
        this.decoder = Assertions.notNull("decoder", decoder);
    }
    
    public DBCursor copy() {
        return new DBCursor(this.collection, this.filter, this.findOptions, this.executor, this.decoderFactory, this.decoder);
    }
    
    @Override
    public boolean hasNext() {
        if (this.closed) {
            throw new IllegalStateException("Cursor has been closed");
        }
        if (this.cursor == null) {
            final FindOperation<DBObject> operation = this.getQueryOperation(this.decoder);
            if (operation.getCursorType() == CursorType.Tailable) {
                operation.cursorType(CursorType.TailableAwait);
            }
            this.initializeCursor(operation);
        }
        final boolean hasNext = this.cursor.hasNext();
        this.setServerCursorOnFinalizer(this.cursor.getServerCursor());
        return hasNext;
    }
    
    @Override
    public DBObject next() {
        this.checkIteratorOrArray(IteratorOrArray.ITERATOR);
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        return this.nextInternal();
    }
    
    public DBObject tryNext() {
        if (this.cursor == null) {
            final FindOperation<DBObject> operation = this.getQueryOperation(this.decoder);
            if (!operation.getCursorType().isTailable()) {
                throw new IllegalArgumentException("Can only be used with a tailable cursor");
            }
            this.initializeCursor(operation);
        }
        final DBObject next = this.cursor.tryNext();
        this.setServerCursorOnFinalizer(this.cursor.getServerCursor());
        return this.currentObject(next);
    }
    
    public DBObject curr() {
        return this.currentObject;
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    public DBCursor addOption(final int option) {
        this.setOptions(this.options |= option);
        return this;
    }
    
    public DBCursor setOptions(final int options) {
        if ((options & 0x40) != 0x0) {
            throw new UnsupportedOperationException("exhaust query option is not supported");
        }
        this.options = options;
        return this;
    }
    
    public DBCursor resetOptions() {
        this.options = 0;
        return this;
    }
    
    public int getOptions() {
        return this.options;
    }
    
    public int getLimit() {
        return this.findOptions.getLimit();
    }
    
    public int getBatchSize() {
        return this.findOptions.getBatchSize();
    }
    
    public DBCursor addSpecial(final String name, final Object value) {
        if (name == null || value == null) {
            return this;
        }
        if ("$comment".equals(name)) {
            this.comment(value.toString());
        }
        else if ("$explain".equals(name)) {
            this.findOptions.getModifiers().put("$explain", true);
        }
        else if ("$hint".equals(name)) {
            if (value instanceof String) {
                this.hint((String)value);
            }
            else {
                this.hint((DBObject)value);
            }
        }
        else if ("$maxScan".equals(name)) {
            this.maxScan(((Number)value).intValue());
        }
        else if ("$maxTimeMS".equals(name)) {
            this.maxTime(((Number)value).longValue(), TimeUnit.MILLISECONDS);
        }
        else if ("$max".equals(name)) {
            this.max((DBObject)value);
        }
        else if ("$min".equals(name)) {
            this.min((DBObject)value);
        }
        else if ("$orderby".equals(name)) {
            this.sort((DBObject)value);
        }
        else if ("$returnKey".equals(name)) {
            this.returnKey();
        }
        else if ("$showDiskLoc".equals(name)) {
            this.showDiskLoc();
        }
        else if ("$snapshot".equals(name)) {
            this.snapshot();
        }
        else {
            if (!"$natural".equals(name)) {
                throw new IllegalArgumentException(name + "is not a supported modifier");
            }
            this.sort(new BasicDBObject("$natural", ((Number)value).intValue()));
        }
        return this;
    }
    
    public DBCursor comment(final String comment) {
        this.findOptions.getModifiers().put("$comment", comment);
        return this;
    }
    
    public DBCursor maxScan(final int max) {
        this.findOptions.getModifiers().put("$maxScan", max);
        return this;
    }
    
    public DBCursor max(final DBObject max) {
        this.findOptions.getModifiers().put("$max", max);
        return this;
    }
    
    public DBCursor min(final DBObject min) {
        this.findOptions.getModifiers().put("$min", min);
        return this;
    }
    
    public DBCursor returnKey() {
        this.findOptions.getModifiers().put("$returnKey", true);
        return this;
    }
    
    public DBCursor showDiskLoc() {
        this.findOptions.getModifiers().put("$showDiskLoc", true);
        return this;
    }
    
    public DBCursor hint(final DBObject indexKeys) {
        this.findOptions.getModifiers().put("$hint", indexKeys);
        return this;
    }
    
    public DBCursor hint(final String indexName) {
        this.findOptions.getModifiers().put("$hint", indexName);
        return this;
    }
    
    public DBCursor maxTime(final long maxTime, final TimeUnit timeUnit) {
        this.findOptions.maxTime(maxTime, timeUnit);
        return this;
    }
    
    public DBCursor snapshot() {
        this.findOptions.getModifiers().put("$snapshot", true);
        return this;
    }
    
    public DBObject explain() {
        return DBObjects.toDBObject(this.executor.execute(this.getQueryOperation(this.collection.getObjectCodec()).asExplainableOperation(ExplainVerbosity.QUERY_PLANNER), this.getReadPreference()));
    }
    
    private FindOperation<DBObject> getQueryOperation(final Decoder<DBObject> decoder) {
        final FindOperation<DBObject> operation = new FindOperation<DBObject>(this.collection.getNamespace(), decoder).readConcern(this.getReadConcern()).filter(this.collection.wrapAllowNull(this.filter)).batchSize(this.findOptions.getBatchSize()).skip(this.findOptions.getSkip()).limit(this.findOptions.getLimit()).maxAwaitTime(this.findOptions.getMaxAwaitTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS).maxTime(this.findOptions.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS).modifiers(this.collection.wrapAllowNull(this.findOptions.getModifiers())).projection(this.collection.wrapAllowNull(this.findOptions.getProjection())).sort(this.collection.wrapAllowNull(this.findOptions.getSort())).collation(this.findOptions.getCollation());
        if ((this.options & 0x2) != 0x0) {
            if ((this.options & 0x20) != 0x0) {
                operation.cursorType(CursorType.TailableAwait);
            }
            else {
                operation.cursorType(CursorType.Tailable);
            }
        }
        else {
            operation.cursorType(this.findOptions.getCursorType());
        }
        if ((this.options & 0x8) != 0x0) {
            operation.oplogReplay(true);
        }
        else {
            operation.oplogReplay(this.findOptions.isOplogReplay());
        }
        if ((this.options & 0x10) != 0x0) {
            operation.noCursorTimeout(true);
        }
        else {
            operation.noCursorTimeout(this.findOptions.isNoCursorTimeout());
        }
        if ((this.options & 0x80) != 0x0) {
            operation.partial(true);
        }
        else {
            operation.partial(this.findOptions.isPartial());
        }
        return operation;
    }
    
    public DBCursor sort(final DBObject orderBy) {
        this.findOptions.sort(orderBy);
        return this;
    }
    
    public DBCursor limit(final int limit) {
        this.findOptions.limit(limit);
        return this;
    }
    
    public DBCursor batchSize(final int numberOfElements) {
        this.findOptions.batchSize(numberOfElements);
        return this;
    }
    
    public DBCursor skip(final int numberOfElements) {
        this.findOptions.skip(numberOfElements);
        return this;
    }
    
    @Override
    public long getCursorId() {
        if (this.cursor != null && this.cursor.getServerCursor() != null) {
            return this.cursor.getServerCursor().getId();
        }
        return 0L;
    }
    
    public int numSeen() {
        return this.numSeen;
    }
    
    @Override
    public void close() {
        this.closed = true;
        if (this.cursor != null) {
            this.cursor.close();
            this.cursor = null;
            this.setServerCursorOnFinalizer(null);
        }
        this.currentObject = null;
    }
    
    @Deprecated
    public DBCursor slaveOk() {
        return this.addOption(4);
    }
    
    @Override
    public Iterator<DBObject> iterator() {
        return this.copy();
    }
    
    public List<DBObject> toArray() {
        return this.toArray(Integer.MAX_VALUE);
    }
    
    public List<DBObject> toArray(final int max) {
        this.checkIteratorOrArray(IteratorOrArray.ARRAY);
        this.fillArray(max - 1);
        return this.all;
    }
    
    public int count() {
        final DBCollectionCountOptions countOptions = this.getDbCollectionCountOptions();
        return (int)this.collection.getCount(this.getQuery(), countOptions);
    }
    
    public DBObject one() {
        final DBCursor findOneCursor = this.copy().limit(-1);
        return findOneCursor.hasNext() ? findOneCursor.next() : null;
    }
    
    public int length() {
        this.checkIteratorOrArray(IteratorOrArray.ARRAY);
        this.fillArray(Integer.MAX_VALUE);
        return this.all.size();
    }
    
    public int itcount() {
        int n = 0;
        while (this.hasNext()) {
            this.next();
            ++n;
        }
        return n;
    }
    
    public int size() {
        final DBCollectionCountOptions countOptions = this.getDbCollectionCountOptions().skip(this.findOptions.getSkip()).limit(this.findOptions.getLimit());
        return (int)this.collection.getCount(this.getQuery(), countOptions);
    }
    
    public DBObject getKeysWanted() {
        return this.findOptions.getProjection();
    }
    
    public DBObject getQuery() {
        return this.filter;
    }
    
    public DBCollection getCollection() {
        return this.collection;
    }
    
    @Override
    public ServerAddress getServerAddress() {
        if (this.cursor != null) {
            return this.cursor.getServerAddress();
        }
        return null;
    }
    
    public DBCursor setReadPreference(final ReadPreference readPreference) {
        this.findOptions.readPreference(readPreference);
        return this;
    }
    
    public ReadPreference getReadPreference() {
        if (this.findOptions.getReadPreference() != null) {
            return this.findOptions.getReadPreference();
        }
        return this.collection.getReadPreference();
    }
    
    DBCursor setReadConcern(final ReadConcern readConcern) {
        this.findOptions.readConcern(readConcern);
        return this;
    }
    
    ReadConcern getReadConcern() {
        if (this.findOptions.getReadConcern() != null) {
            return this.findOptions.getReadConcern();
        }
        return this.collection.getReadConcern();
    }
    
    public Collation getCollation() {
        return this.findOptions.getCollation();
    }
    
    public DBCursor setCollation(final Collation collation) {
        this.findOptions.collation(collation);
        return this;
    }
    
    public DBCursor setDecoderFactory(final DBDecoderFactory factory) {
        this.decoderFactory = factory;
        this.decoder = new DBDecoderAdapter(factory.create(), this.collection, this.getCollection().getBufferPool());
        return this;
    }
    
    public DBDecoderFactory getDecoderFactory() {
        return this.decoderFactory;
    }
    
    @Override
    public String toString() {
        return "DBCursor{collection=" + this.collection + ", find=" + this.findOptions + ((this.cursor != null) ? (", cursor=" + this.cursor.getServerCursor()) : "") + '}';
    }
    
    private void initializeCursor(final FindOperation<DBObject> operation) {
        this.cursor = new MongoBatchCursorAdapter<DBObject>(this.executor.execute((ReadOperation<BatchCursor<DBObject>>)operation, this.getReadPreferenceForCursor()));
        if (this.isCursorFinalizerEnabled() && this.cursor.getServerCursor() != null) {
            this.optionalFinalizer = new OptionalFinalizer(this.collection.getDB().getMongo(), this.collection.getNamespace());
        }
    }
    
    private boolean isCursorFinalizerEnabled() {
        return this.collection.getDB().getMongo().getMongoClientOptions().isCursorFinalizerEnabled();
    }
    
    private void setServerCursorOnFinalizer(final ServerCursor serverCursor) {
        if (this.optionalFinalizer != null) {
            this.optionalFinalizer.setServerCursor(serverCursor);
        }
    }
    
    private void checkIteratorOrArray(final IteratorOrArray expected) {
        if (this.iteratorOrArray == null) {
            this.iteratorOrArray = expected;
            return;
        }
        if (expected == this.iteratorOrArray) {
            return;
        }
        throw new IllegalArgumentException("Can't switch cursor access methods");
    }
    
    private ReadPreference getReadPreferenceForCursor() {
        ReadPreference readPreference = this.getReadPreference();
        if ((this.options & 0x4) != 0x0 && !readPreference.isSlaveOk()) {
            readPreference = ReadPreference.secondaryPreferred();
        }
        return readPreference;
    }
    
    private void fillArray(final int n) {
        this.checkIteratorOrArray(IteratorOrArray.ARRAY);
        while (n >= this.all.size() && this.hasNext()) {
            this.all.add(this.nextInternal());
        }
    }
    
    private DBObject nextInternal() {
        if (this.iteratorOrArray == null) {
            this.checkIteratorOrArray(IteratorOrArray.ITERATOR);
        }
        final DBObject next = this.cursor.next();
        this.setServerCursorOnFinalizer(this.cursor.getServerCursor());
        return this.currentObject(next);
    }
    
    private DBObject currentObject(final DBObject newCurrentObject) {
        if (newCurrentObject != null) {
            this.currentObject = newCurrentObject;
            ++this.numSeen;
            if (this.findOptions.getProjection() != null && !this.findOptions.getProjection().keySet().isEmpty()) {
                this.currentObject.markAsPartialObject();
            }
        }
        return newCurrentObject;
    }
    
    private static DBObject lookupSuitableHints(final DBObject query, final List<DBObject> hints) {
        if (hints == null) {
            return null;
        }
        final Set<String> keys = query.keySet();
        for (final DBObject hint : hints) {
            if (keys.containsAll(hint.keySet())) {
                return hint;
            }
        }
        return null;
    }
    
    private DBCollectionCountOptions getDbCollectionCountOptions() {
        final DBCollectionCountOptions countOptions = new DBCollectionCountOptions().readPreference(this.getReadPreferenceForCursor()).readConcern(this.getReadConcern()).collation(this.getCollation()).maxTime(this.findOptions.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        final Object hint = this.findOptions.getModifiers().get("$hint");
        if (hint != null) {
            if (hint instanceof String) {
                countOptions.hintString((String)hint);
            }
            else {
                countOptions.hint((DBObject)hint);
            }
        }
        return countOptions;
    }
    
    private enum IteratorOrArray
    {
        ITERATOR, 
        ARRAY;
    }
    
    private static class OptionalFinalizer
    {
        private final Mongo mongo;
        private final MongoNamespace namespace;
        private volatile ServerCursor serverCursor;
        
        private OptionalFinalizer(final Mongo mongo, final MongoNamespace namespace) {
            this.namespace = Assertions.notNull("namespace", namespace);
            this.mongo = Assertions.notNull("mongo", mongo);
        }
        
        private void setServerCursor(final ServerCursor serverCursor) {
            this.serverCursor = serverCursor;
        }
        
        @Override
        protected void finalize() {
            if (this.serverCursor != null) {
                this.mongo.addOrphanedCursor(this.serverCursor, this.namespace);
            }
        }
    }
}
