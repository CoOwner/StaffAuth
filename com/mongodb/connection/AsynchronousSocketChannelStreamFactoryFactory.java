package com.mongodb.connection;

public class AsynchronousSocketChannelStreamFactoryFactory implements StreamFactoryFactory
{
    @Override
    public StreamFactory create(final SocketSettings socketSettings, final SslSettings sslSettings) {
        return new AsynchronousSocketChannelStreamFactory(socketSettings, sslSettings);
    }
}
