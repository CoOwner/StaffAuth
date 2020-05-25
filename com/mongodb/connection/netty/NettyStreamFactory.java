package com.mongodb.connection.netty;

import io.netty.channel.*;
import io.netty.channel.socket.*;
import com.mongodb.assertions.*;
import io.netty.channel.socket.nio.*;
import io.netty.buffer.*;
import io.netty.channel.nio.*;
import com.mongodb.*;
import com.mongodb.connection.*;

public class NettyStreamFactory implements StreamFactory
{
    private final SocketSettings settings;
    private final SslSettings sslSettings;
    private final EventLoopGroup eventLoopGroup;
    private final Class<? extends SocketChannel> socketChannelClass;
    private final ByteBufAllocator allocator;
    
    public NettyStreamFactory(final SocketSettings settings, final SslSettings sslSettings, final EventLoopGroup eventLoopGroup, final Class<? extends SocketChannel> socketChannelClass, final ByteBufAllocator allocator) {
        this.settings = Assertions.notNull("settings", settings);
        this.sslSettings = Assertions.notNull("sslSettings", sslSettings);
        this.eventLoopGroup = Assertions.notNull("eventLoopGroup", eventLoopGroup);
        this.socketChannelClass = Assertions.notNull("socketChannelClass", socketChannelClass);
        this.allocator = Assertions.notNull("allocator", allocator);
    }
    
    public NettyStreamFactory(final SocketSettings settings, final SslSettings sslSettings, final EventLoopGroup eventLoopGroup, final ByteBufAllocator allocator) {
        this(settings, sslSettings, eventLoopGroup, (Class<? extends SocketChannel>)NioSocketChannel.class, allocator);
    }
    
    public NettyStreamFactory(final SocketSettings settings, final SslSettings sslSettings, final EventLoopGroup eventLoopGroup) {
        this(settings, sslSettings, eventLoopGroup, (ByteBufAllocator)PooledByteBufAllocator.DEFAULT);
    }
    
    public NettyStreamFactory(final SocketSettings settings, final SslSettings sslSettings) {
        this(settings, sslSettings, (EventLoopGroup)new NioEventLoopGroup());
    }
    
    @Override
    public Stream create(final ServerAddress serverAddress) {
        return new NettyStream(serverAddress, this.settings, this.sslSettings, this.eventLoopGroup, this.socketChannelClass, this.allocator);
    }
}
