package com.mongodb.gridfs;

import org.bson.types.*;
import java.util.*;
import java.security.*;
import java.io.*;
import com.mongodb.*;
import com.mongodb.util.*;

public class GridFSInputFile extends GridFSFile
{
    private final InputStream inputStream;
    private final boolean closeStreamOnPersist;
    private boolean savedChunks;
    private byte[] buffer;
    private int currentChunkNumber;
    private int currentBufferPosition;
    private long totalBytes;
    private OutputStream outputStream;
    private MessageDigest messageDigester;
    
    protected GridFSInputFile(final GridFS gridFS, final InputStream inputStream, final String filename, final boolean closeStreamOnPersist) {
        this.savedChunks = false;
        this.buffer = null;
        this.currentChunkNumber = 0;
        this.currentBufferPosition = 0;
        this.totalBytes = 0L;
        this.outputStream = null;
        this.messageDigester = null;
        this.fs = gridFS;
        this.inputStream = inputStream;
        this.filename = filename;
        this.closeStreamOnPersist = closeStreamOnPersist;
        this.id = new ObjectId();
        this.chunkSize = 261120L;
        this.uploadDate = new Date();
        try {
            this.messageDigester = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No MD5!");
        }
        this.messageDigester.reset();
        this.buffer = new byte[(int)this.chunkSize];
    }
    
    protected GridFSInputFile(final GridFS gridFS, final InputStream inputStream, final String filename) {
        this(gridFS, inputStream, filename, false);
    }
    
    protected GridFSInputFile(final GridFS gridFS, final String filename) {
        this(gridFS, null, filename);
    }
    
    protected GridFSInputFile(final GridFS gridFS) {
        this(gridFS, null, null);
    }
    
    public void setId(final Object id) {
        this.id = id;
    }
    
    public void setFilename(final String filename) {
        this.filename = filename;
    }
    
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }
    
    public void setChunkSize(final long chunkSize) {
        if (this.outputStream != null || this.savedChunks) {
            return;
        }
        this.chunkSize = chunkSize;
        this.buffer = new byte[(int)this.chunkSize];
    }
    
    @Override
    public void save() {
        this.save(this.chunkSize);
    }
    
    public void save(final long chunkSize) {
        if (this.outputStream != null) {
            throw new MongoException("cannot mix OutputStream and regular save()");
        }
        if (!this.savedChunks) {
            try {
                this.saveChunks(chunkSize);
            }
            catch (IOException ioe) {
                throw new MongoException("couldn't save chunks", ioe);
            }
        }
        super.save();
    }
    
    public int saveChunks() throws IOException {
        return this.saveChunks(this.chunkSize);
    }
    
    public int saveChunks(final long chunkSize) throws IOException {
        if (this.outputStream != null) {
            throw new MongoException("Cannot mix OutputStream and regular save()");
        }
        if (this.savedChunks) {
            throw new MongoException("Chunks already saved!");
        }
        if (chunkSize <= 0L) {
            throw new MongoException("chunkSize must be greater than zero");
        }
        if (this.chunkSize != chunkSize) {
            this.chunkSize = chunkSize;
            this.buffer = new byte[(int)this.chunkSize];
        }
        int bytesRead = 0;
        while (bytesRead >= 0) {
            this.currentBufferPosition = 0;
            bytesRead = this._readStream2Buffer();
            this.dumpBuffer(true);
        }
        this.finishData();
        return this.currentChunkNumber;
    }
    
    public OutputStream getOutputStream() {
        if (this.outputStream == null) {
            this.outputStream = new GridFSOutputStream();
        }
        return this.outputStream;
    }
    
    private void dumpBuffer(final boolean writePartial) {
        if (this.currentBufferPosition < this.chunkSize && !writePartial) {
            return;
        }
        if (this.currentBufferPosition == 0) {
            return;
        }
        byte[] writeBuffer = this.buffer;
        if (this.currentBufferPosition != this.chunkSize) {
            writeBuffer = new byte[this.currentBufferPosition];
            System.arraycopy(this.buffer, 0, writeBuffer, 0, this.currentBufferPosition);
        }
        final DBObject chunk = this.createChunk(this.id, this.currentChunkNumber, writeBuffer);
        this.fs.getChunksCollection().save(chunk);
        ++this.currentChunkNumber;
        this.totalBytes += writeBuffer.length;
        this.messageDigester.update(writeBuffer);
        this.currentBufferPosition = 0;
    }
    
    protected DBObject createChunk(final Object id, final int currentChunkNumber, final byte[] writeBuffer) {
        return new BasicDBObject("files_id", id).append("n", currentChunkNumber).append("data", writeBuffer);
    }
    
    private int _readStream2Buffer() throws IOException {
        int bytesRead = 0;
        while (this.currentBufferPosition < this.chunkSize && bytesRead >= 0) {
            bytesRead = this.inputStream.read(this.buffer, this.currentBufferPosition, (int)this.chunkSize - this.currentBufferPosition);
            if (bytesRead > 0) {
                this.currentBufferPosition += bytesRead;
            }
            else {
                if (bytesRead == 0) {
                    throw new RuntimeException("i'm doing something wrong");
                }
                continue;
            }
        }
        return bytesRead;
    }
    
    private void finishData() {
        if (!this.savedChunks) {
            this.md5 = Util.toHex(this.messageDigester.digest());
            this.messageDigester = null;
            this.length = this.totalBytes;
            this.savedChunks = true;
            try {
                if (this.inputStream != null && this.closeStreamOnPersist) {
                    this.inputStream.close();
                }
            }
            catch (IOException ex) {}
        }
    }
    
    private class GridFSOutputStream extends OutputStream
    {
        @Override
        public void write(final int b) throws IOException {
            final byte[] byteArray = { (byte)(b & 0xFF) };
            this.write(byteArray, 0, 1);
        }
        
        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            int offset = off;
            int length = len;
            int toCopy = 0;
            while (length > 0) {
                toCopy = length;
                if (toCopy > GridFSInputFile.this.chunkSize - GridFSInputFile.this.currentBufferPosition) {
                    toCopy = (int)GridFSInputFile.this.chunkSize - GridFSInputFile.this.currentBufferPosition;
                }
                System.arraycopy(b, offset, GridFSInputFile.this.buffer, GridFSInputFile.this.currentBufferPosition, toCopy);
                GridFSInputFile.this.currentBufferPosition += toCopy;
                offset += toCopy;
                length -= toCopy;
                if (GridFSInputFile.this.currentBufferPosition == GridFSInputFile.this.chunkSize) {
                    GridFSInputFile.this.dumpBuffer(false);
                }
            }
        }
        
        @Override
        public void close() {
            GridFSInputFile.this.dumpBuffer(true);
            GridFSInputFile.this.finishData();
            GridFSFile.this.save();
        }
    }
}
