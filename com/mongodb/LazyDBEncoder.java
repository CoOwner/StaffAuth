package com.mongodb;

import org.bson.io.*;
import org.bson.*;
import java.io.*;

public class LazyDBEncoder implements DBEncoder
{
    @Override
    public int writeObject(final OutputBuffer outputBuffer, final BSONObject document) {
        if (!(document instanceof LazyDBObject)) {
            throw new IllegalArgumentException("LazyDBEncoder can only encode BSONObject instances of type LazyDBObject");
        }
        final LazyDBObject lazyDBObject = (LazyDBObject)document;
        try {
            return lazyDBObject.pipe(outputBuffer);
        }
        catch (IOException e) {
            throw new MongoException("Exception serializing a LazyDBObject", e);
        }
    }
}
