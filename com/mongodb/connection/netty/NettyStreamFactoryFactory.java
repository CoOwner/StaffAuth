package com.mongodb.connection.netty;

import io.netty.channel.*;
import io.netty.channel.socket.*;
import io.netty.buffer.*;
import com.mongodb.connection.*;
import io.netty.channel.nio.*;
import io.netty.channel.socket.nio.*;
import com.mongodb.assertions.*;

public class NettyStreamFactoryFactory implements StreamFactoryFactory
{
    private final EventLoopGroup eventLoopGroup;
    private final Class<? extends SocketChannel> socketChannelClass;
    private final ByteBufAllocator allocator;
    
    @Deprecated
    public NettyStreamFactoryFactory() {
        this(builder());
    }
    
    @Deprecated
    public NettyStreamFactoryFactory(final EventLoopGroup eventLoopGroup, final ByteBufAllocator allocator) {
        this(builder().eventLoopGroup(eventLoopGroup).allocator(allocator));
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public StreamFactory create(final SocketSettings socketSettings, final SslSettings sslSettings) {
        return new NettyStreamFactory(socketSettings, sslSettings, this.eventLoopGroup, this.socketChannelClass, this.allocator);
    }
    
    @Override
    public String toString() {
        return "NettyStreamFactoryFactory{eventLoopGroup=" + this.eventLoopGroup + ", socketChannelClass=" + this.socketChannelClass + ", allocator=" + this.allocator + '}';
    }
    
    private NettyStreamFactoryFactory(final Builder builder) {
        this.allocator = builder.allocator;
        this.socketChannelClass = builder.socketChannelClass;
        if (builder.eventLoopGroup != null) {
            this.eventLoopGroup = builder.eventLoopGroup;
        }
        else {
            this.eventLoopGroup = (EventLoopGroup)new NioEventLoopGroup();
        }
    }
    
    public static final class Builder
    {
        private ByteBufAllocator allocator;
        private Class<? extends SocketChannel> socketChannelClass;
        private EventLoopGroup eventLoopGroup;
        
        private Builder() {
            this.allocator(ByteBufAllocator.DEFAULT);
            this.socketChannelClass((Class<? extends SocketChannel>)NioSocketChannel.class);
        }
        
        public Builder allocator(final ByteBufAllocator allocator) {
            this.allocator = Assertions.notNull("allocator", allocator);
            return this;
        }
        
        public Builder socketChannelClass(final Class<? extends SocketChannel> socketChannelClass) {
            this.socketChannelClass = Assertions.notNull("socketChannelClass", socketChannelClass);
            return this;
        }
        
        public Builder eventLoopGroup(final EventLoopGroup eventLoopGroup) {
            this.eventLoopGroup = Assertions.notNull("eventLoopGroup", eventLoopGroup);
            return this;
        }
        
        public NettyStreamFactoryFactory build() {
            return new NettyStreamFactoryFactory(this, null);
        }
    }
}
