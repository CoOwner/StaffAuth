package org.bson;

import org.bson.types.*;

public interface BSONCallback
{
    void objectStart();
    
    void objectStart(final String p0);
    
    Object objectDone();
    
    void reset();
    
    Object get();
    
    BSONCallback createBSONCallback();
    
    void arrayStart();
    
    void arrayStart(final String p0);
    
    Object arrayDone();
    
    void gotNull(final String p0);
    
    void gotUndefined(final String p0);
    
    void gotMinKey(final String p0);
    
    void gotMaxKey(final String p0);
    
    void gotBoolean(final String p0, final boolean p1);
    
    void gotDouble(final String p0, final double p1);
    
    void gotDecimal128(final String p0, final Decimal128 p1);
    
    void gotInt(final String p0, final int p1);
    
    void gotLong(final String p0, final long p1);
    
    void gotDate(final String p0, final long p1);
    
    void gotString(final String p0, final String p1);
    
    void gotSymbol(final String p0, final String p1);
    
    void gotRegex(final String p0, final String p1, final String p2);
    
    void gotTimestamp(final String p0, final int p1, final int p2);
    
    void gotObjectId(final String p0, final ObjectId p1);
    
    void gotDBRef(final String p0, final String p1, final ObjectId p2);
    
    @Deprecated
    void gotBinaryArray(final String p0, final byte[] p1);
    
    void gotBinary(final String p0, final byte p1, final byte[] p2);
    
    void gotUUID(final String p0, final long p1, final long p2);
    
    void gotCode(final String p0, final String p1);
    
    void gotCodeWScope(final String p0, final String p1, final Object p2);
}
