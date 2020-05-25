package com.mongodb.gridfs;

import com.mongodb.*;
import com.mongodb.util.*;
import org.bson.*;
import java.util.*;

public abstract class GridFSFile implements DBObject
{
    private static final Set<String> VALID_FIELDS;
    final DBObject extra;
    GridFS fs;
    Object id;
    String filename;
    String contentType;
    long length;
    long chunkSize;
    Date uploadDate;
    String md5;
    
    public GridFSFile() {
        this.extra = new BasicDBObject();
    }
    
    public void save() {
        if (this.fs == null) {
            throw new MongoException("need fs");
        }
        this.fs.getFilesCollection().save(this);
    }
    
    public void validate() {
        if (this.fs == null) {
            throw new MongoException("no fs");
        }
        if (this.md5 == null) {
            throw new MongoException("no md5 stored");
        }
        final DBObject cmd = new BasicDBObject("filemd5", this.id);
        cmd.put("root", this.fs.getBucketName());
        final DBObject res = this.fs.getDB().command(cmd);
        if (res == null || !res.containsField("md5")) {
            throw new MongoException("no md5 returned from server: " + res);
        }
        final String m = res.get("md5").toString();
        if (m.equals(this.md5)) {
            return;
        }
        throw new MongoException("md5 differ.  mine [" + this.md5 + "] theirs [" + m + "]");
    }
    
    public int numChunks() {
        double d = (double)this.length;
        d /= this.chunkSize;
        return (int)Math.ceil(d);
    }
    
    public Object getId() {
        return this.id;
    }
    
    public String getFilename() {
        return this.filename;
    }
    
    public String getContentType() {
        return this.contentType;
    }
    
    public long getLength() {
        return this.length;
    }
    
    public long getChunkSize() {
        return this.chunkSize;
    }
    
    public Date getUploadDate() {
        return this.uploadDate;
    }
    
    public List<String> getAliases() {
        return (List<String>)this.extra.get("aliases");
    }
    
    public DBObject getMetaData() {
        return (DBObject)this.extra.get("metadata");
    }
    
    public void setMetaData(final DBObject metadata) {
        this.extra.put("metadata", metadata);
    }
    
    public String getMD5() {
        return this.md5;
    }
    
    @Override
    public Object put(final String key, final Object v) {
        if (key == null) {
            throw new RuntimeException("key should never be null");
        }
        if (key.equals("_id")) {
            this.id = v;
        }
        else if (key.equals("filename")) {
            this.filename = ((v == null) ? null : v.toString());
        }
        else if (key.equals("contentType")) {
            this.contentType = (String)v;
        }
        else if (key.equals("length")) {
            this.length = ((Number)v).longValue();
        }
        else if (key.equals("chunkSize")) {
            this.chunkSize = ((Number)v).longValue();
        }
        else if (key.equals("uploadDate")) {
            this.uploadDate = (Date)v;
        }
        else if (key.equals("md5")) {
            this.md5 = (String)v;
        }
        else {
            this.extra.put(key, v);
        }
        return v;
    }
    
    @Override
    public Object get(final String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key should never be null");
        }
        if (key.equals("_id")) {
            return this.id;
        }
        if (key.equals("filename")) {
            return this.filename;
        }
        if (key.equals("contentType")) {
            return this.contentType;
        }
        if (key.equals("length")) {
            return this.length;
        }
        if (key.equals("chunkSize")) {
            return this.chunkSize;
        }
        if (key.equals("uploadDate")) {
            return this.uploadDate;
        }
        if (key.equals("md5")) {
            return this.md5;
        }
        return this.extra.get(key);
    }
    
    @Deprecated
    @Override
    public boolean containsKey(final String key) {
        return this.containsField(key);
    }
    
    @Override
    public boolean containsField(final String s) {
        return this.keySet().contains(s);
    }
    
    @Override
    public Set<String> keySet() {
        final Set<String> keys = new HashSet<String>();
        keys.addAll(GridFSFile.VALID_FIELDS);
        keys.addAll(this.extra.keySet());
        return keys;
    }
    
    @Override
    public boolean isPartialObject() {
        return false;
    }
    
    @Override
    public void markAsPartialObject() {
        throw new MongoException("Can't load partial GridFSFile file");
    }
    
    @Override
    public String toString() {
        return JSON.serialize(this);
    }
    
    protected void setGridFS(final GridFS fs) {
        this.fs = fs;
    }
    
    protected GridFS getGridFS() {
        return this.fs;
    }
    
    @Override
    public void putAll(final BSONObject o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void putAll(final Map m) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Map<?, ?> toMap() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Object removeField(final String key) {
        throw new UnsupportedOperationException();
    }
    
    static {
        VALID_FIELDS = Collections.unmodifiableSet((Set<? extends String>)new HashSet<String>(Arrays.asList("_id", "filename", "contentType", "length", "chunkSize", "uploadDate", "aliases", "md5")));
    }
}
