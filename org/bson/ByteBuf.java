package org.bson;

import java.nio.*;

public interface ByteBuf
{
    int capacity();
    
    ByteBuf put(final int p0, final byte p1);
    
    int remaining();
    
    ByteBuf put(final byte[] p0, final int p1, final int p2);
    
    boolean hasRemaining();
    
    ByteBuf put(final byte p0);
    
    ByteBuf flip();
    
    byte[] array();
    
    int limit();
    
    ByteBuf position(final int p0);
    
    ByteBuf clear();
    
    ByteBuf order(final ByteOrder p0);
    
    byte get();
    
    byte get(final int p0);
    
    ByteBuf get(final byte[] p0);
    
    ByteBuf get(final int p0, final byte[] p1);
    
    ByteBuf get(final byte[] p0, final int p1, final int p2);
    
    ByteBuf get(final int p0, final byte[] p1, final int p2, final int p3);
    
    long getLong();
    
    long getLong(final int p0);
    
    double getDouble();
    
    double getDouble(final int p0);
    
    int getInt();
    
    int getInt(final int p0);
    
    int position();
    
    ByteBuf limit(final int p0);
    
    ByteBuf asReadOnly();
    
    ByteBuf duplicate();
    
    ByteBuffer asNIO();
    
    int getReferenceCount();
    
    ByteBuf retain();
    
    void release();
}
