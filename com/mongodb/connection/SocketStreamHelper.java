package com.mongodb.connection;

import java.util.concurrent.*;
import com.mongodb.*;
import com.mongodb.internal.connection.*;
import java.net.*;
import javax.net.ssl.*;
import java.io.*;

final class SocketStreamHelper
{
    static void initialize(final Socket socket, final ServerAddress address, final SocketSettings settings, final SslSettings sslSettings) throws IOException {
        socket.setTcpNoDelay(true);
        socket.setSoTimeout(settings.getReadTimeout(TimeUnit.MILLISECONDS));
        socket.setKeepAlive(settings.isKeepAlive());
        if (settings.getReceiveBufferSize() > 0) {
            socket.setReceiveBufferSize(settings.getReceiveBufferSize());
        }
        if (settings.getSendBufferSize() > 0) {
            socket.setSendBufferSize(settings.getSendBufferSize());
        }
        if (sslSettings.isEnabled() || socket instanceof SSLSocket) {
            if (!(socket instanceof SSLSocket)) {
                throw new MongoInternalException("SSL is enabled but the socket is not an instance of javax.net.ssl.SSLSocket");
            }
            final SSLSocket sslSocket = (SSLSocket)socket;
            final SSLParameters sslParameters = sslSocket.getSSLParameters();
            SslHelper.enableSni(address, sslParameters);
            if (!sslSettings.isInvalidHostNameAllowed()) {
                SslHelper.enableHostNameVerification(sslParameters);
            }
            sslSocket.setSSLParameters(sslParameters);
        }
        socket.connect(address.getSocketAddress(), settings.getConnectTimeout(TimeUnit.MILLISECONDS));
    }
    
    private SocketStreamHelper() {
    }
}
