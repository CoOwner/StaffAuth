package com.mongodb.connection;

import javax.net.*;
import java.net.*;
import com.mongodb.assertions.*;
import java.io.*;
import org.bson.*;
import java.util.*;
import com.mongodb.*;

class SocketStream implements Stream
{
    private final ServerAddress address;
    private final SocketSettings settings;
    private final SslSettings sslSettings;
    private final SocketFactory socketFactory;
    private final BufferProvider bufferProvider;
    private volatile Socket socket;
    private volatile OutputStream outputStream;
    private volatile InputStream inputStream;
    private volatile boolean isClosed;
    
    public SocketStream(final ServerAddress address, final SocketSettings settings, final SslSettings sslSettings, final SocketFactory socketFactory, final BufferProvider bufferProvider) {
        this.address = Assertions.notNull("address", address);
        this.settings = Assertions.notNull("settings", settings);
        this.sslSettings = Assertions.notNull("sslSettings", sslSettings);
        this.socketFactory = Assertions.notNull("socketFactory", socketFactory);
        this.bufferProvider = Assertions.notNull("bufferProvider", bufferProvider);
    }
    
    @Override
    public void open() throws IOException {
        try {
            SocketStreamHelper.initialize(this.socket = this.socketFactory.createSocket(), this.address, this.settings, this.sslSettings);
            this.outputStream = this.socket.getOutputStream();
            this.inputStream = this.socket.getInputStream();
        }
        catch (IOException e) {
            this.close();
            throw new MongoSocketOpenException("Exception opening socket", this.getAddress(), e);
        }
    }
    
    @Override
    public ByteBuf getBuffer(final int size) {
        return this.bufferProvider.getBuffer(size);
    }
    
    @Override
    public void write(final List<ByteBuf> buffers) throws IOException {
        for (final ByteBuf cur : buffers) {
            this.outputStream.write(cur.array(), 0, cur.limit());
        }
    }
    
    @Override
    public ByteBuf read(final int numBytes) throws IOException {
        final ByteBuf buffer = this.bufferProvider.getBuffer(numBytes);
        int totalBytesRead = 0;
        final byte[] bytes = buffer.array();
        while (totalBytesRead < buffer.limit()) {
            final int bytesRead = this.inputStream.read(bytes, totalBytesRead, buffer.limit() - totalBytesRead);
            if (bytesRead == -1) {
                buffer.release();
                throw new MongoSocketReadException("Prematurely reached end of stream", this.getAddress());
            }
            totalBytesRead += bytesRead;
        }
        return buffer;
    }
    
    @Override
    public void openAsync(final AsyncCompletionHandler<Void> handler) {
        throw new UnsupportedOperationException(this.getClass() + " does not support asynchronous operations.");
    }
    
    @Override
    public void writeAsync(final List<ByteBuf> buffers, final AsyncCompletionHandler<Void> handler) {
        throw new UnsupportedOperationException(this.getClass() + " does not support asynchronous operations.");
    }
    
    @Override
    public void readAsync(final int numBytes, final AsyncCompletionHandler<ByteBuf> handler) {
        throw new UnsupportedOperationException(this.getClass() + " does not support asynchronous operations.");
    }
    
    @Override
    public ServerAddress getAddress() {
        return this.address;
    }
    
    SocketSettings getSettings() {
        return this.settings;
    }
    
    @Override
    public void close() {
        try {
            this.isClosed = true;
            if (this.socket != null) {
                this.socket.close();
            }
        }
        catch (IOException ex) {}
    }
    
    @Override
    public boolean isClosed() {
        return this.isClosed;
    }
}
