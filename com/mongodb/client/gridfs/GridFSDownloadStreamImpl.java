package com.mongodb.client.gridfs;

import com.mongodb.client.gridfs.model.*;
import org.bson.*;
import com.mongodb.client.*;
import com.mongodb.assertions.*;
import com.mongodb.*;
import org.bson.conversions.*;
import org.bson.types.*;

class GridFSDownloadStreamImpl extends GridFSDownloadStream
{
    private final GridFSFile fileInfo;
    private final MongoCollection<Document> chunksCollection;
    private final BsonValue fileId;
    private final long length;
    private final int chunkSizeInBytes;
    private final int numberOfChunks;
    private MongoCursor<Document> cursor;
    private int batchSize;
    private int chunkIndex;
    private int bufferOffset;
    private long currentPosition;
    private byte[] buffer;
    private long markPosition;
    private final Object closeLock;
    private final Object cursorLock;
    private boolean closed;
    
    GridFSDownloadStreamImpl(final GridFSFile fileInfo, final MongoCollection<Document> chunksCollection) {
        this.buffer = null;
        this.closeLock = new Object();
        this.cursorLock = new Object();
        this.closed = false;
        this.fileInfo = Assertions.notNull("file information", fileInfo);
        this.chunksCollection = Assertions.notNull("chunks collection", chunksCollection);
        this.fileId = fileInfo.getId();
        this.length = fileInfo.getLength();
        this.chunkSizeInBytes = fileInfo.getChunkSize();
        this.numberOfChunks = (int)Math.ceil(this.length / (double)this.chunkSizeInBytes);
    }
    
    @Override
    public GridFSFile getGridFSFile() {
        return this.fileInfo;
    }
    
    @Override
    public GridFSDownloadStream batchSize(final int batchSize) {
        Assertions.isTrueArgument("batchSize cannot be negative", batchSize >= 0);
        this.batchSize = batchSize;
        this.discardCursor();
        return this;
    }
    
    @Override
    public int read() {
        final byte[] b = { 0 };
        final int res = this.read(b);
        if (res < 0) {
            return -1;
        }
        return b[0] & 0xFF;
    }
    
    @Override
    public int read(final byte[] b) {
        return this.read(b, 0, b.length);
    }
    
    @Override
    public int read(final byte[] b, final int off, final int len) {
        this.checkClosed();
        if (this.currentPosition == this.length) {
            return -1;
        }
        if (this.buffer == null) {
            this.buffer = this.getBuffer(this.chunkIndex);
        }
        else if (this.bufferOffset == this.buffer.length) {
            ++this.chunkIndex;
            this.buffer = this.getBuffer(this.chunkIndex);
            this.bufferOffset = 0;
        }
        final int r = Math.min(len, this.buffer.length - this.bufferOffset);
        System.arraycopy(this.buffer, this.bufferOffset, b, off, r);
        this.bufferOffset += r;
        this.currentPosition += r;
        return r;
    }
    
    @Override
    public long skip(final long bytesToSkip) {
        this.checkClosed();
        if (bytesToSkip <= 0L) {
            return 0L;
        }
        final long skippedPosition = this.currentPosition + bytesToSkip;
        this.bufferOffset = (int)skippedPosition % this.chunkSizeInBytes;
        if (skippedPosition >= this.length) {
            final long skipped = this.length - this.currentPosition;
            this.chunkIndex = this.numberOfChunks - 1;
            this.currentPosition = this.length;
            this.buffer = null;
            this.discardCursor();
            return skipped;
        }
        final int newChunkIndex = (int)Math.floor(skippedPosition / (float)this.chunkSizeInBytes);
        if (this.chunkIndex != newChunkIndex) {
            this.chunkIndex = newChunkIndex;
            this.buffer = null;
            this.discardCursor();
        }
        this.currentPosition += bytesToSkip;
        return bytesToSkip;
    }
    
    @Override
    public int available() {
        this.checkClosed();
        if (this.buffer == null) {
            return 0;
        }
        return this.buffer.length - this.bufferOffset;
    }
    
    @Override
    public void mark() {
        this.mark(Integer.MAX_VALUE);
    }
    
    @Override
    public synchronized void mark(final int readlimit) {
        this.markPosition = this.currentPosition;
    }
    
    @Override
    public synchronized void reset() {
        this.checkClosed();
        if (this.currentPosition == this.markPosition) {
            return;
        }
        this.bufferOffset = (int)this.markPosition % this.chunkSizeInBytes;
        this.currentPosition = this.markPosition;
        final int markChunkIndex = (int)Math.floor(this.markPosition / (float)this.chunkSizeInBytes);
        if (markChunkIndex != this.chunkIndex) {
            this.chunkIndex = markChunkIndex;
            this.buffer = null;
            this.cursor = null;
        }
    }
    
    @Override
    public boolean markSupported() {
        return true;
    }
    
    @Override
    public void close() {
        synchronized (this.closeLock) {
            if (!this.closed) {
                this.closed = true;
            }
            this.discardCursor();
        }
    }
    
    private void checkClosed() {
        synchronized (this.closeLock) {
            if (this.closed) {
                throw new MongoGridFSException("The InputStream has been closed");
            }
        }
    }
    
    private void discardCursor() {
        synchronized (this.cursorLock) {
            if (this.cursor != null) {
                this.cursor.close();
                this.cursor = null;
            }
        }
    }
    
    private Document getChunk(final int startChunkIndex) {
        if (this.cursor == null) {
            this.cursor = this.chunksCollection.find(new Document("files_id", this.fileId).append("n", new Document("$gte", startChunkIndex))).batchSize(this.batchSize).sort(new Document("n", 1)).iterator();
        }
        Document chunk = null;
        if (this.cursor.hasNext()) {
            chunk = this.cursor.next();
            if (this.batchSize == 1) {
                this.discardCursor();
            }
            if (chunk.getInteger("n") != startChunkIndex) {
                throw new MongoGridFSException(String.format("Could not find file chunk for file_id: %s at chunk index %s.", this.fileId, startChunkIndex));
            }
        }
        return chunk;
    }
    
    private byte[] getBufferFromChunk(final Document chunk, final int expectedChunkIndex) {
        if (chunk == null || chunk.getInteger("n") != expectedChunkIndex) {
            throw new MongoGridFSException(String.format("Could not find file chunk for file_id: %s at chunk index %s.", this.fileId, expectedChunkIndex));
        }
        if (!(chunk.get("data") instanceof Binary)) {
            throw new MongoGridFSException("Unexpected data format for the chunk");
        }
        final byte[] data = chunk.get("data", Binary.class).getData();
        long expectedDataLength = 0L;
        boolean extraChunk = false;
        if (expectedChunkIndex + 1 > this.numberOfChunks) {
            extraChunk = true;
        }
        else if (expectedChunkIndex + 1 == this.numberOfChunks) {
            expectedDataLength = this.length - expectedChunkIndex * (long)this.chunkSizeInBytes;
        }
        else {
            expectedDataLength = this.chunkSizeInBytes;
        }
        if (extraChunk && data.length > expectedDataLength) {
            throw new MongoGridFSException(String.format("Extra chunk data for file_id: %s. Unexpected chunk at chunk index %s.The size was %s and it should be %s bytes.", this.fileId, expectedChunkIndex, data.length, expectedDataLength));
        }
        if (data.length != expectedDataLength) {
            throw new MongoGridFSException(String.format("Chunk size data length is not the expected size. The size was %s for file_id: %s chunk index %s it should be %s bytes.", data.length, this.fileId, expectedChunkIndex, expectedDataLength));
        }
        return data;
    }
    
    private byte[] getBuffer(final int chunkIndexToFetch) {
        return this.getBufferFromChunk(this.getChunk(chunkIndexToFetch), chunkIndexToFetch);
    }
}
