package com.mongodb;

import org.bson.*;
import java.io.*;

public class DefaultDBDecoder extends BasicBSONDecoder implements DBDecoder
{
    public static final DBDecoderFactory FACTORY;
    
    @Override
    public DBCallback getDBCallback(final DBCollection collection) {
        return new DefaultDBCallback(collection);
    }
    
    @Override
    public DBObject decode(final InputStream input, final DBCollection collection) throws IOException {
        final DBCallback callback = this.getDBCallback(collection);
        this.decode(input, callback);
        return (DBObject)callback.get();
    }
    
    @Override
    public DBObject decode(final byte[] bytes, final DBCollection collection) {
        final DBCallback callback = this.getDBCallback(collection);
        this.decode(bytes, callback);
        return (DBObject)callback.get();
    }
    
    @Override
    public String toString() {
        return String.format("DBDecoder{class=%s}", this.getClass().getName());
    }
    
    static {
        FACTORY = new DBDecoderFactory() {
            @Override
            public DBDecoder create() {
                return new DefaultDBDecoder();
            }
        };
    }
}
