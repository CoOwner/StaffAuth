package com.mongodb;

import org.bson.io.*;
import org.bson.*;

public class DefaultDBEncoder extends BasicBSONEncoder implements DBEncoder
{
    public static final DBEncoderFactory FACTORY;
    
    @Override
    public int writeObject(final OutputBuffer outputBuffer, final BSONObject document) {
        this.set(outputBuffer);
        final int x = this.putObject(document);
        this.done();
        return x;
    }
    
    @Override
    protected boolean putSpecial(final String name, final Object value) {
        if (value instanceof DBRef) {
            this.putDBRef(name, (DBRef)value);
            return true;
        }
        return false;
    }
    
    protected void putDBRef(final String name, final DBRef ref) {
        this.putObject(name, new BasicDBObject("$ref", ref.getCollectionName()).append("$id", ref.getId()));
    }
    
    @Override
    public String toString() {
        return String.format("DBEncoder{class=%s}", this.getClass().getName());
    }
    
    static {
        FACTORY = new DBEncoderFactory() {
            @Override
            public DBEncoder create() {
                return new DefaultDBEncoder();
            }
        };
    }
}
