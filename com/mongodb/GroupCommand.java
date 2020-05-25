package com.mongodb;

import com.mongodb.client.model.*;
import com.mongodb.assertions.*;
import com.mongodb.operation.*;
import org.bson.*;
import org.bson.codecs.*;

public class GroupCommand
{
    private final String collectionName;
    private final DBObject keys;
    private final String keyf;
    private final DBObject condition;
    private final DBObject initial;
    private final String reduce;
    private final String finalize;
    private final Collation collation;
    
    public GroupCommand(final DBCollection collection, final DBObject keys, final DBObject condition, final DBObject initial, final String reduce, final String finalize) {
        this(collection, keys, condition, initial, reduce, finalize, null);
    }
    
    public GroupCommand(final DBCollection collection, final DBObject keys, final DBObject condition, final DBObject initial, final String reduce, final String finalize, final Collation collation) {
        Assertions.notNull("collection", collection);
        this.collectionName = collection.getName();
        this.keys = keys;
        this.condition = condition;
        this.initial = initial;
        this.reduce = reduce;
        this.finalize = finalize;
        this.keyf = null;
        this.collation = collation;
    }
    
    public GroupCommand(final DBCollection collection, final String keyf, final DBObject condition, final DBObject initial, final String reduce, final String finalize) {
        this(collection, keyf, condition, initial, reduce, finalize, null);
    }
    
    public GroupCommand(final DBCollection collection, final String keyf, final DBObject condition, final DBObject initial, final String reduce, final String finalize, final Collation collation) {
        Assertions.notNull("collection", collection);
        this.collectionName = collection.getName();
        this.keyf = Assertions.notNull("keyf", keyf);
        this.condition = condition;
        this.initial = initial;
        this.reduce = reduce;
        this.finalize = finalize;
        this.keys = null;
        this.collation = collation;
    }
    
    public DBObject toDBObject() {
        final DBObject args = new BasicDBObject("ns", this.collectionName).append("cond", this.condition).append("$reduce", this.reduce).append("initial", this.initial);
        if (this.keys != null) {
            args.put("key", this.keys);
        }
        if (this.keyf != null) {
            args.put("$keyf", this.keyf);
        }
        if (this.finalize != null) {
            args.put("finalize", this.finalize);
        }
        return new BasicDBObject("group", args);
    }
    
    GroupOperation<DBObject> toOperation(final MongoNamespace namespace, final DBObjectCodec codec) {
        if (this.initial == null) {
            throw new IllegalArgumentException("Group command requires an initial document for the aggregate result");
        }
        if (this.reduce == null) {
            throw new IllegalArgumentException("Group command requires a reduce function for the aggregate result");
        }
        final GroupOperation<DBObject> operation = new GroupOperation<DBObject>(namespace, new BsonJavaScript(this.reduce), new BsonDocumentWrapper<Object>(this.initial, codec), codec);
        if (this.keys != null) {
            operation.key(new BsonDocumentWrapper<Object>(this.keys, codec));
        }
        if (this.keyf != null) {
            operation.keyFunction(new BsonJavaScript(this.keyf));
        }
        if (this.condition != null) {
            operation.filter(new BsonDocumentWrapper<Object>(this.condition, codec));
        }
        if (this.finalize != null) {
            operation.finalizeFunction(new BsonJavaScript(this.finalize));
        }
        operation.collation(this.collation);
        return operation;
    }
}
