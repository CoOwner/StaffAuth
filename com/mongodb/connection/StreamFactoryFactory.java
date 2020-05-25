package com.mongodb.connection;

public interface StreamFactoryFactory
{
    StreamFactory create(final SocketSettings p0, final SslSettings p1);
}
