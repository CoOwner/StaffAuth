package com.mongodb.client.gridfs;

import java.io.*;
import com.mongodb.annotations.*;
import org.bson.types.*;
import org.bson.*;

@NotThreadSafe
public abstract class GridFSUploadStream extends OutputStream
{
    @Deprecated
    public abstract ObjectId getFileId();
    
    public abstract ObjectId getObjectId();
    
    public abstract BsonValue getId();
    
    public abstract void abort();
    
    @Override
    public abstract void write(final int p0);
    
    @Override
    public abstract void write(final byte[] p0);
    
    @Override
    public abstract void write(final byte[] p0, final int p1, final int p2);
    
    @Override
    public void flush() {
    }
    
    @Override
    public abstract void close();
}
