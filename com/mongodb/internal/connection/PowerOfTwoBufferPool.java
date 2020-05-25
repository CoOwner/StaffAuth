package com.mongodb.internal.connection;

import com.mongodb.connection.*;
import java.util.*;
import java.nio.*;
import org.bson.*;

public class PowerOfTwoBufferPool implements BufferProvider
{
    private final Map<Integer, ConcurrentPool<ByteBuffer>> powerOfTwoToPoolMap;
    
    public PowerOfTwoBufferPool() {
        this(24);
    }
    
    public PowerOfTwoBufferPool(final int highestPowerOfTwo) {
        this.powerOfTwoToPoolMap = new HashMap<Integer, ConcurrentPool<ByteBuffer>>();
        int powerOfTwo = 1;
        for (int i = 0; i <= highestPowerOfTwo; ++i) {
            final int size = powerOfTwo;
            this.powerOfTwoToPoolMap.put(i, new ConcurrentPool<ByteBuffer>(Integer.MAX_VALUE, new ConcurrentPool.ItemFactory<ByteBuffer>() {
                @Override
                public ByteBuffer create(final boolean initialize) {
                    return PowerOfTwoBufferPool.this.createNew(size);
                }
                
                @Override
                public void close(final ByteBuffer byteBuffer) {
                }
                
                @Override
                public boolean shouldPrune(final ByteBuffer byteBuffer) {
                    return false;
                }
            }));
            powerOfTwo <<= 1;
        }
    }
    
    @Override
    public ByteBuf getBuffer(final int size) {
        final ConcurrentPool<ByteBuffer> pool = this.powerOfTwoToPoolMap.get(log2(roundUpToNextHighestPowerOfTwo(size)));
        final ByteBuffer byteBuffer = (pool == null) ? this.createNew(size) : pool.get();
        byteBuffer.clear();
        byteBuffer.limit(size);
        return new PooledByteBufNIO(byteBuffer);
    }
    
    private ByteBuffer createNew(final int size) {
        final ByteBuffer buf = ByteBuffer.allocate(size);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf;
    }
    
    private void release(final ByteBuffer buffer) {
        final ConcurrentPool<ByteBuffer> pool = this.powerOfTwoToPoolMap.get(log2(roundUpToNextHighestPowerOfTwo(buffer.capacity())));
        if (pool != null) {
            pool.release(buffer);
        }
    }
    
    static int log2(final int powerOfTwo) {
        return 31 - Integer.numberOfLeadingZeros(powerOfTwo);
    }
    
    static int roundUpToNextHighestPowerOfTwo(final int size) {
        int v = size;
        v = (--v | v >> 1);
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        return ++v;
    }
    
    private class PooledByteBufNIO extends ByteBufNIO
    {
        public PooledByteBufNIO(final ByteBuffer buf) {
            super(buf);
        }
        
        @Override
        public void release() {
            final ByteBuffer wrapped = this.asNIO();
            super.release();
            if (this.getReferenceCount() == 0) {
                PowerOfTwoBufferPool.this.release(wrapped);
            }
        }
    }
}
