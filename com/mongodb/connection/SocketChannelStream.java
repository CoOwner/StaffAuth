package com.mongodb.connection;

import java.nio.channels.*;
import com.mongodb.assertions.*;
import java.io.*;
import org.bson.*;
import java.util.*;
import java.nio.*;
import com.mongodb.*;

class SocketChannelStream implements Stream
{
    private final ServerAddress address;
    private final SocketSettings settings;
    private final SslSettings sslSettings;
    private final BufferProvider bufferProvider;
    private volatile SocketChannel socketChannel;
    private volatile boolean isClosed;
    
    public SocketChannelStream(final ServerAddress address, final SocketSettings settings, final SslSettings sslSettings, final BufferProvider bufferProvider) {
        this.address = Assertions.notNull("address", address);
        this.settings = Assertions.notNull("settings", settings);
        this.sslSettings = Assertions.notNull("sslSettings", sslSettings);
        this.bufferProvider = Assertions.notNull("bufferProvider", bufferProvider);
    }
    
    @Override
    public void open() throws IOException {
        try {
            this.socketChannel = SocketChannel.open();
            SocketStreamHelper.initialize(this.socketChannel.socket(), this.address, this.settings, this.sslSettings);
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
        Assertions.isTrue("open", !this.isClosed());
        int totalSize = 0;
        final ByteBuffer[] byteBufferArray = new ByteBuffer[buffers.size()];
        for (int i = 0; i < buffers.size(); ++i) {
            byteBufferArray[i] = buffers.get(i).asNIO();
            totalSize += byteBufferArray[i].limit();
        }
        for (long bytesRead = 0L; bytesRead < totalSize; bytesRead += this.socketChannel.write(byteBufferArray)) {}
    }
    
    @Override
    public ByteBuf read(final int numBytes) throws IOException {
        final ByteBuf buffer = this.bufferProvider.getBuffer(numBytes);
        Assertions.isTrue("open", !this.isClosed());
        int bytesRead;
        for (int totalBytesRead = 0; totalBytesRead < buffer.limit(); totalBytesRead += bytesRead) {
            bytesRead = this.socketChannel.read(buffer.asNIO());
            if (bytesRead == -1) {
                buffer.release();
                throw new MongoSocketReadException("Prematurely reached end of stream", this.getAddress());
            }
        }
        return buffer.flip();
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
            if (this.socketChannel != null) {
                this.socketChannel.close();
            }
        }
        catch (IOException ex) {}
    }
    
    @Override
    public boolean isClosed() {
        return this.isClosed;
    }
}
