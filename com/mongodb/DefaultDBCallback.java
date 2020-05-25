package com.mongodb;

import org.bson.*;
import org.bson.types.*;
import java.util.*;

public class DefaultDBCallback extends BasicBSONCallback implements DBCallback
{
    private final DBObjectFactory objectFactory;
    public static final DBCallbackFactory FACTORY;
    
    public DefaultDBCallback(final DBCollection collection) {
        if (collection != null) {
            this.objectFactory = collection.getObjectFactory();
        }
        else {
            this.objectFactory = new DBCollectionObjectFactory();
        }
    }
    
    @Override
    public BSONObject create() {
        return this.objectFactory.getInstance();
    }
    
    @Override
    public BSONObject create(final boolean array, final List<String> path) {
        return array ? new BasicDBList() : this.objectFactory.getInstance((path != null) ? path : Collections.emptyList());
    }
    
    @Override
    public void gotDBRef(final String name, final String namespace, final ObjectId id) {
        this._put(name, new DBRef(namespace, id));
    }
    
    @Override
    public Object objectDone() {
        final String name = this.curName();
        final BSONObject document = (BSONObject)super.objectDone();
        if (!(document instanceof BasicBSONList)) {
            final Iterator<String> iterator = document.keySet().iterator();
            if (iterator.hasNext() && iterator.next().equals("$ref") && iterator.hasNext() && iterator.next().equals("$id")) {
                this._put(name, new DBRef((String)document.get("$db"), (String)document.get("$ref"), document.get("$id")));
            }
        }
        return document;
    }
    
    static {
        FACTORY = new DBCallbackFactory() {
            @Override
            public DBCallback create(final DBCollection collection) {
                return new DefaultDBCallback(collection);
            }
        };
    }
}
