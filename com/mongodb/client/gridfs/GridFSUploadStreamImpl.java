package com.mongodb.client.gridfs;

import com.mongodb.client.*;
import com.mongodb.client.gridfs.model.*;
import org.bson.*;
import com.mongodb.assertions.*;
import com.mongodb.*;
import java.security.*;
import org.bson.conversions.*;
import java.util.*;
import com.mongodb.util.*;
import org.bson.types.*;

final class GridFSUploadStreamImpl extends GridFSUploadStream
{
    private final MongoCollection<GridFSFile> filesCollection;
    private final MongoCollection<Document> chunksCollection;
    private final BsonValue fileId;
    private final String filename;
    private final int chunkSizeBytes;
    private final Document metadata;
    private final MessageDigest md5;
    private byte[] buffer;
    private long lengthInBytes;
    private int bufferOffset;
    private int chunkIndex;
    private final Object closeLock;
    private boolean closed;
    
    GridFSUploadStreamImpl(final MongoCollection<GridFSFile> filesCollection, final MongoCollection<Document> chunksCollection, final BsonValue fileId, final String filename, final int chunkSizeBytes, final Document metadata) {
        this.closeLock = new Object();
        this.closed = false;
        this.filesCollection = Assertions.notNull("files collection", filesCollection);
        this.chunksCollection = Assertions.notNull("chunks collection", chunksCollection);
        this.fileId = Assertions.notNull("File Id", fileId);
        this.filename = Assertions.notNull("filename", filename);
        this.chunkSizeBytes = chunkSizeBytes;
        this.metadata = metadata;
        try {
            this.md5 = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new MongoGridFSException("No MD5 message digest available, cannot upload file", e);
        }
        this.chunkIndex = 0;
        this.bufferOffset = 0;
        this.buffer = new byte[chunkSizeBytes];
    }
    
    @Deprecated
    @Override
    public ObjectId getFileId() {
        return this.getObjectId();
    }
    
    @Override
    public ObjectId getObjectId() {
        if (!this.fileId.isObjectId()) {
            throw new MongoGridFSException("Custom id type used for this GridFS upload stream");
        }
        return this.fileId.asObjectId().getValue();
    }
    
    @Override
    public BsonValue getId() {
        return this.fileId;
    }
    
    @Override
    public void abort() {
        synchronized (this.closeLock) {
            this.checkClosed();
            this.closed = true;
        }
        this.chunksCollection.deleteMany(new Document("files_id", this.fileId));
    }
    
    @Override
    public void write(final int b) {
        final byte[] byteArray = { (byte)(0xFF & b) };
        this.write(byteArray, 0, 1);
    }
    
    @Override
    public void write(final byte[] b) {
        this.write(b, 0, b.length);
    }
    
    @Override
    public void write(final byte[] b, final int off, final int len) {
        this.checkClosed();
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        int currentOffset = off;
        int lengthToWrite = len;
        int amountToCopy = 0;
        while (lengthToWrite > 0) {
            amountToCopy = lengthToWrite;
            if (amountToCopy > this.chunkSizeBytes - this.bufferOffset) {
                amountToCopy = this.chunkSizeBytes - this.bufferOffset;
            }
            System.arraycopy(b, currentOffset, this.buffer, this.bufferOffset, amountToCopy);
            this.bufferOffset += amountToCopy;
            currentOffset += amountToCopy;
            lengthToWrite -= amountToCopy;
            this.lengthInBytes += amountToCopy;
            if (this.bufferOffset == this.chunkSizeBytes) {
                this.writeChunk();
            }
        }
    }
    
    @Override
    public void close() {
        synchronized (this.closeLock) {
            if (this.closed) {
                return;
            }
            this.closed = true;
        }
        this.writeChunk();
        final GridFSFile gridFSFile = new GridFSFile(this.fileId, this.filename, this.lengthInBytes, this.chunkSizeBytes, new Date(), Util.toHex(this.md5.digest()), this.metadata);
        this.filesCollection.insertOne(gridFSFile);
        this.buffer = null;
    }
    
    private void writeChunk() {
        if (this.bufferOffset > 0) {
            this.chunksCollection.insertOne(new Document("files_id", this.fileId).append("n", this.chunkIndex).append("data", this.getData()));
            this.md5.update(this.buffer);
            ++this.chunkIndex;
            this.bufferOffset = 0;
        }
    }
    
    private Binary getData() {
        if (this.bufferOffset < this.chunkSizeBytes) {
            final byte[] sizedBuffer = new byte[this.bufferOffset];
            System.arraycopy(this.buffer, 0, sizedBuffer, 0, this.bufferOffset);
            this.buffer = sizedBuffer;
        }
        return new Binary(this.buffer);
    }
    
    private void checkClosed() {
        synchronized (this.closeLock) {
            if (this.closed) {
                throw new MongoGridFSException("The OutputStream has been closed");
            }
        }
    }
}
