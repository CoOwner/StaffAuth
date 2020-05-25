package org.bson.io;

import java.io.*;
import org.bson.types.*;

public interface BsonInput extends Closeable
{
    int getPosition();
    
    byte readByte();
    
    void readBytes(final byte[] p0);
    
    void readBytes(final byte[] p0, final int p1, final int p2);
    
    long readInt64();
    
    double readDouble();
    
    int readInt32();
    
    String readString();
    
    ObjectId readObjectId();
    
    String readCString();
    
    void skipCString();
    
    void skip(final int p0);
    
    void mark(final int p0);
    
    void reset();
    
    boolean hasRemaining();
    
    void close();
}
