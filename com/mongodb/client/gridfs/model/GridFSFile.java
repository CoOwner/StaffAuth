package com.mongodb.client.gridfs.model;

import org.bson.*;
import com.mongodb.assertions.*;
import org.bson.types.*;
import com.mongodb.*;
import java.util.*;

public final class GridFSFile
{
    private final BsonValue id;
    private final String filename;
    private final long length;
    private final int chunkSize;
    private final Date uploadDate;
    private final String md5;
    private final Document metadata;
    private final Document extraElements;
    
    public GridFSFile(final BsonValue id, final String filename, final long length, final int chunkSize, final Date uploadDate, final String md5, final Document metadata) {
        this(id, filename, length, chunkSize, uploadDate, md5, metadata, null);
    }
    
    public GridFSFile(final BsonValue id, final String filename, final long length, final int chunkSize, final Date uploadDate, final String md5, final Document metadata, final Document extraElements) {
        this.id = Assertions.notNull("id", id);
        this.filename = Assertions.notNull("filename", filename);
        this.length = Assertions.notNull("length", length);
        this.chunkSize = Assertions.notNull("chunkSize", chunkSize);
        this.uploadDate = Assertions.notNull("uploadDate", uploadDate);
        this.md5 = Assertions.notNull("md5", md5);
        this.metadata = ((metadata != null && metadata.isEmpty()) ? null : metadata);
        this.extraElements = extraElements;
    }
    
    public ObjectId getObjectId() {
        if (!this.id.isObjectId()) {
            throw new MongoGridFSException("Custom id type used for this GridFS file");
        }
        return this.id.asObjectId().getValue();
    }
    
    public BsonValue getId() {
        return this.id;
    }
    
    public String getFilename() {
        return this.filename;
    }
    
    public long getLength() {
        return this.length;
    }
    
    public int getChunkSize() {
        return this.chunkSize;
    }
    
    public Date getUploadDate() {
        return this.uploadDate;
    }
    
    public String getMD5() {
        return this.md5;
    }
    
    public Document getMetadata() {
        return this.metadata;
    }
    
    @Deprecated
    public Document getExtraElements() {
        return this.extraElements;
    }
    
    @Deprecated
    public String getContentType() {
        if (this.extraElements != null && this.extraElements.containsKey("contentType")) {
            return this.extraElements.getString("contentType");
        }
        throw new MongoGridFSException("No contentType data for this GridFS file");
    }
    
    @Deprecated
    public List<String> getAliases() {
        if (this.extraElements != null && this.extraElements.containsKey("aliases")) {
            return (List<String>)this.extraElements.get("aliases");
        }
        throw new MongoGridFSException("No aliases data for this GridFS file");
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final GridFSFile that = (GridFSFile)o;
        Label_0062: {
            if (this.id != null) {
                if (this.id.equals(that.id)) {
                    break Label_0062;
                }
            }
            else if (that.id == null) {
                break Label_0062;
            }
            return false;
        }
        if (!this.filename.equals(that.filename)) {
            return false;
        }
        if (this.length != that.length) {
            return false;
        }
        if (this.chunkSize != that.chunkSize) {
            return false;
        }
        if (!this.uploadDate.equals(that.uploadDate)) {
            return false;
        }
        if (!this.md5.equals(that.md5)) {
            return false;
        }
        Label_0170: {
            if (this.metadata != null) {
                if (this.metadata.equals(that.metadata)) {
                    break Label_0170;
                }
            }
            else if (that.metadata == null) {
                break Label_0170;
            }
            return false;
        }
        if (this.extraElements != null) {
            if (this.extraElements.equals(that.extraElements)) {
                return true;
            }
        }
        else if (that.extraElements == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.id != null) ? this.id.hashCode() : 0;
        result = 31 * result + this.filename.hashCode();
        result = 31 * result + (int)(this.length ^ this.length >>> 32);
        result = 31 * result + this.chunkSize;
        result = 31 * result + this.uploadDate.hashCode();
        result = 31 * result + this.md5.hashCode();
        result = 31 * result + ((this.metadata != null) ? this.metadata.hashCode() : 0);
        result = 31 * result + ((this.extraElements != null) ? this.extraElements.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "GridFSFile{id=" + this.id + ", filename='" + this.filename + '\'' + ", length=" + this.length + ", chunkSize=" + this.chunkSize + ", uploadDate=" + this.uploadDate + ", md5='" + this.md5 + '\'' + ", metadata=" + this.metadata + ", extraElements='" + this.extraElements + '\'' + '}';
    }
}
