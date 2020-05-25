package com.mongodb.gridfs;

import java.io.*;
import com.mongodb.*;

public class GridFSDBFile extends GridFSFile
{
    public InputStream getInputStream() {
        return new GridFSInputStream();
    }
    
    public long writeTo(final String filename) throws IOException {
        return this.writeTo(new File(filename));
    }
    
    public long writeTo(final File file) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            return this.writeTo(out);
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    public long writeTo(final OutputStream out) throws IOException {
        for (int nc = this.numChunks(), i = 0; i < nc; ++i) {
            out.write(this.getChunk(i));
        }
        return this.length;
    }
    
    private byte[] getChunk(final int chunkNumber) {
        if (this.fs == null) {
            throw new IllegalStateException("No GridFS instance defined!");
        }
        final DBObject chunk = this.fs.getChunksCollection().findOne(new BasicDBObject("files_id", this.id).append("n", chunkNumber));
        if (chunk == null) {
            throw new MongoException("Can't find a chunk!  file id: " + this.id + " chunk: " + chunkNumber);
        }
        return (byte[])chunk.get("data");
    }
    
    void remove() {
        this.fs.getFilesCollection().remove(new BasicDBObject("_id", this.id));
        this.fs.getChunksCollection().remove(new BasicDBObject("files_id", this.id));
    }
    
    private class GridFSInputStream extends InputStream
    {
        private final int numberOfChunks;
        private int currentChunkId;
        private int offset;
        private byte[] buffer;
        
        GridFSInputStream() {
            this.currentChunkId = -1;
            this.offset = 0;
            this.buffer = null;
            this.numberOfChunks = GridFSDBFile.this.numChunks();
        }
        
        @Override
        public int available() {
            if (this.buffer == null) {
                return 0;
            }
            return this.buffer.length - this.offset;
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
            if (this.buffer == null || this.offset >= this.buffer.length) {
                if (this.currentChunkId + 1 >= this.numberOfChunks) {
                    return -1;
                }
                this.buffer = GridFSDBFile.this.getChunk(++this.currentChunkId);
                this.offset = 0;
            }
            final int r = Math.min(len, this.buffer.length - this.offset);
            System.arraycopy(this.buffer, this.offset, b, off, r);
            this.offset += r;
            return r;
        }
        
        @Override
        public long skip(final long bytesToSkip) throws IOException {
            if (bytesToSkip <= 0L) {
                return 0L;
            }
            if (this.currentChunkId == this.numberOfChunks) {
                return 0L;
            }
            long offsetInFile = 0L;
            if (this.currentChunkId >= 0) {
                offsetInFile = this.currentChunkId * GridFSDBFile.this.chunkSize + this.offset;
            }
            if (bytesToSkip + offsetInFile >= GridFSDBFile.this.length) {
                this.currentChunkId = this.numberOfChunks;
                this.buffer = null;
                return GridFSDBFile.this.length - offsetInFile;
            }
            final int temp = this.currentChunkId;
            this.currentChunkId = (int)((bytesToSkip + offsetInFile) / GridFSDBFile.this.chunkSize);
            if (temp != this.currentChunkId) {
                this.buffer = GridFSDBFile.this.getChunk(this.currentChunkId);
            }
            this.offset = (int)((bytesToSkip + offsetInFile) % GridFSDBFile.this.chunkSize);
            return bytesToSkip;
        }
    }
}
