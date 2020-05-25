package com.mongodb.connection.netty;

import com.mongodb.connection.*;
import io.netty.buffer.*;
import org.bson.*;

final class NettyBufferProvider implements BufferProvider
{
    private final ByteBufAllocator allocator;
    
    public NettyBufferProvider() {
        this.allocator = (ByteBufAllocator)PooledByteBufAllocator.DEFAULT;
    }
    
    public NettyBufferProvider(final ByteBufAllocator allocator) {
        this.allocator = allocator;
    }
    
    @Override
    public ByteBuf getBuffer(final int size) {
        return new NettyByteBuf(this.allocator.directBuffer(size, size));
    }
}
