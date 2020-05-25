package com.mongodb.connection;

import org.bson.io.*;
import org.bson.*;
import com.mongodb.assertions.*;
import java.util.*;
import java.io.*;

public class ByteBufferBsonOutput extends OutputBuffer
{
    private static final int MAX_SHIFT = 31;
    private static final int INITIAL_SHIFT = 10;
    public static final int INITIAL_BUFFER_SIZE = 1024;
    public static final int MAX_BUFFER_SIZE = 16777216;
    private final BufferProvider bufferProvider;
    private final List<ByteBuf> bufferList;
    private int curBufferIndex;
    private int position;
    private boolean closed;
    
    public ByteBufferBsonOutput(final BufferProvider bufferProvider) {
        this.bufferList = new ArrayList<ByteBuf>();
        this.curBufferIndex = 0;
        this.position = 0;
        this.bufferProvider = Assertions.notNull("bufferProvider", bufferProvider);
    }
    
    @Override
    public void writeBytes(final byte[] bytes, final int offset, final int length) {
        this.ensureOpen();
        int bytesToPutInCurrentBuffer;
        for (int currentOffset = offset, remainingLen = length; remainingLen > 0; remainingLen -= bytesToPutInCurrentBuffer, currentOffset += bytesToPutInCurrentBuffer) {
            final ByteBuf buf = this.getCurrentByteBuffer();
            bytesToPutInCurrentBuffer = Math.min(buf.remaining(), remainingLen);
            buf.put(bytes, currentOffset, bytesToPutInCurrentBuffer);
        }
        this.position += length;
    }
    
    @Override
    public void writeByte(final int value) {
        this.ensureOpen();
        this.getCurrentByteBuffer().put((byte)value);
        ++this.position;
    }
    
    private ByteBuf getCurrentByteBuffer() {
        final ByteBuf curByteBuffer = this.getByteBufferAtIndex(this.curBufferIndex);
        if (curByteBuffer.hasRemaining()) {
            return curByteBuffer;
        }
        ++this.curBufferIndex;
        return this.getByteBufferAtIndex(this.curBufferIndex);
    }
    
    private ByteBuf getByteBufferAtIndex(final int index) {
        if (this.bufferList.size() < index + 1) {
            this.bufferList.add(this.bufferProvider.getBuffer((index >= 21) ? 16777216 : Math.min(1024 << index, 16777216)));
        }
        return this.bufferList.get(index);
    }
    
    @Override
    public int getPosition() {
        this.ensureOpen();
        return this.position;
    }
    
    @Override
    public int getSize() {
        this.ensureOpen();
        return this.position;
    }
    
    @Override
    protected void write(final int absolutePosition, final int value) {
        this.ensureOpen();
        if (absolutePosition < 0) {
            throw new IllegalArgumentException(String.format("position must be >= 0 but was %d", absolutePosition));
        }
        if (absolutePosition > this.position - 1) {
            throw new IllegalArgumentException(String.format("position must be <= %d but was %d", this.position - 1, absolutePosition));
        }
        final BufferPositionPair bufferPositionPair = this.getBufferPositionPair(absolutePosition);
        final ByteBuf byteBuffer = this.getByteBufferAtIndex(bufferPositionPair.bufferIndex);
        byteBuffer.put(bufferPositionPair.position++, (byte)value);
    }
    
    @Override
    public List<ByteBuf> getByteBuffers() {
        this.ensureOpen();
        final List<ByteBuf> buffers = new ArrayList<ByteBuf>(this.bufferList.size());
        for (final ByteBuf cur : this.bufferList) {
            buffers.add(cur.duplicate().flip());
        }
        return buffers;
    }
    
    @Override
    public int pipe(final OutputStream out) throws IOException {
        this.ensureOpen();
        final byte[] tmp = new byte[1024];
        int total = 0;
        for (final ByteBuf cur : this.getByteBuffers()) {
            final ByteBuf dup = cur.duplicate();
            while (dup.hasRemaining()) {
                final int numBytesToCopy = Math.min(dup.remaining(), tmp.length);
                dup.get(tmp, 0, numBytesToCopy);
                out.write(tmp, 0, numBytesToCopy);
            }
            total += dup.limit();
        }
        return total;
    }
    
    @Override
    public void truncateToPosition(final int newPosition) {
        this.ensureOpen();
        if (newPosition > this.position || newPosition < 0) {
            throw new IllegalArgumentException();
        }
        final BufferPositionPair bufferPositionPair = this.getBufferPositionPair(newPosition);
        this.bufferList.get(bufferPositionPair.bufferIndex).position(bufferPositionPair.position);
        while (this.bufferList.size() > bufferPositionPair.bufferIndex + 1) {
            final ByteBuf buffer = this.bufferList.remove(this.bufferList.size() - 1);
            buffer.release();
        }
        this.curBufferIndex = bufferPositionPair.bufferIndex;
        this.position = newPosition;
    }
    
    @Override
    public void close() {
        for (final ByteBuf cur : this.bufferList) {
            cur.release();
        }
        this.bufferList.clear();
        this.closed = true;
    }
    
    private BufferPositionPair getBufferPositionPair(final int absolutePosition) {
        int positionInBuffer = absolutePosition;
        int bufferIndex = 0;
        for (int bufferSize = 1024, startPositionOfBuffer = 0; startPositionOfBuffer + bufferSize <= absolutePosition; startPositionOfBuffer += bufferSize, positionInBuffer -= bufferSize, bufferSize = this.bufferList.get(bufferIndex).limit()) {
            ++bufferIndex;
        }
        return new BufferPositionPair(bufferIndex, positionInBuffer);
    }
    
    private void ensureOpen() {
        if (this.closed) {
            throw new IllegalStateException("The output is closed");
        }
    }
    
    private static final class BufferPositionPair
    {
        private final int bufferIndex;
        private int position;
        
        BufferPositionPair(final int bufferIndex, final int position) {
            this.bufferIndex = bufferIndex;
            this.position = position;
        }
    }
}
