package com.mongodb.connection;

import javax.net.*;
import com.mongodb.internal.connection.*;
import com.mongodb.assertions.*;
import com.mongodb.*;
import javax.net.ssl.*;

public class SocketStreamFactory implements StreamFactory
{
    private final SocketSettings settings;
    private final SslSettings sslSettings;
    private final SocketFactory socketFactory;
    private final BufferProvider bufferProvider;
    
    public SocketStreamFactory(final SocketSettings settings, final SslSettings sslSettings) {
        this(settings, sslSettings, null);
    }
    
    public SocketStreamFactory(final SocketSettings settings, final SslSettings sslSettings, final SocketFactory socketFactory) {
        this.bufferProvider = new PowerOfTwoBufferPool();
        this.settings = Assertions.notNull("settings", settings);
        this.sslSettings = Assertions.notNull("sslSettings", sslSettings);
        this.socketFactory = socketFactory;
    }
    
    @Override
    public Stream create(final ServerAddress serverAddress) {
        Stream stream;
        if (this.socketFactory != null) {
            stream = new SocketStream(serverAddress, this.settings, this.sslSettings, this.socketFactory, this.bufferProvider);
        }
        else if (this.sslSettings.isEnabled()) {
            stream = new SocketStream(serverAddress, this.settings, this.sslSettings, SSLSocketFactory.getDefault(), this.bufferProvider);
        }
        else if (System.getProperty("org.mongodb.useSocket", "false").equals("true")) {
            stream = new SocketStream(serverAddress, this.settings, this.sslSettings, SocketFactory.getDefault(), this.bufferProvider);
        }
        else {
            stream = new SocketChannelStream(serverAddress, this.settings, this.sslSettings, this.bufferProvider);
        }
        return stream;
    }
}
