package org.bson.io;

import java.io.*;
import org.bson.types.*;

public interface BsonOutput extends Closeable
{
    int getPosition();
    
    int getSize();
    
    void truncateToPosition(final int p0);
    
    void writeBytes(final byte[] p0);
    
    void writeBytes(final byte[] p0, final int p1, final int p2);
    
    void writeByte(final int p0);
    
    void writeCString(final String p0);
    
    void writeString(final String p0);
    
    void writeDouble(final double p0);
    
    void writeInt32(final int p0);
    
    void writeInt32(final int p0, final int p1);
    
    void writeInt64(final long p0);
    
    void writeObjectId(final ObjectId p0);
    
    void close();
}
