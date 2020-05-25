package com.mongodb.gridfs;

import com.mongodb.*;
import org.bson.types.*;
import java.util.*;
import java.io.*;

public class GridFS
{
    public static final int DEFAULT_CHUNKSIZE = 261120;
    @Deprecated
    public static final long MAX_CHUNKSIZE = 3500000L;
    public static final String DEFAULT_BUCKET = "fs";
    private final DB database;
    private final String bucketName;
    private final DBCollection filesCollection;
    private final DBCollection chunksCollection;
    
    public GridFS(final DB db) {
        this(db, "fs");
    }
    
    public GridFS(final DB db, final String bucket) {
        this.database = db;
        this.bucketName = bucket;
        this.filesCollection = this.database.getCollection(this.bucketName + ".files");
        this.chunksCollection = this.database.getCollection(this.bucketName + ".chunks");
        try {
            if (this.filesCollection.count() < 1000L) {
                this.filesCollection.createIndex(new BasicDBObject("filename", 1).append("uploadDate", 1));
            }
            if (this.chunksCollection.count() < 1000L) {
                this.chunksCollection.createIndex(new BasicDBObject("files_id", 1).append("n", 1), new BasicDBObject("unique", true));
            }
        }
        catch (MongoException ex) {}
        this.filesCollection.setObjectClass(GridFSDBFile.class);
    }
    
    public DBCursor getFileList() {
        return this.filesCollection.find().sort(new BasicDBObject("filename", 1));
    }
    
    public DBCursor getFileList(final DBObject query) {
        return this.filesCollection.find(query).sort(new BasicDBObject("filename", 1));
    }
    
    public DBCursor getFileList(final DBObject query, final DBObject sort) {
        return this.filesCollection.find(query).sort(sort);
    }
    
    public GridFSDBFile find(final ObjectId objectId) {
        return this.findOne(objectId);
    }
    
    public GridFSDBFile findOne(final ObjectId objectId) {
        return this.findOne(new BasicDBObject("_id", objectId));
    }
    
    public GridFSDBFile findOne(final String filename) {
        return this.findOne(new BasicDBObject("filename", filename));
    }
    
    public GridFSDBFile findOne(final DBObject query) {
        return this.injectGridFSInstance(this.filesCollection.findOne(query));
    }
    
    public List<GridFSDBFile> find(final String filename) {
        return this.find(new BasicDBObject("filename", filename));
    }
    
    public List<GridFSDBFile> find(final String filename, final DBObject sort) {
        return this.find(new BasicDBObject("filename", filename), sort);
    }
    
    public List<GridFSDBFile> find(final DBObject query) {
        return this.find(query, null);
    }
    
    public List<GridFSDBFile> find(final DBObject query, final DBObject sort) {
        final List<GridFSDBFile> files = new ArrayList<GridFSDBFile>();
        final DBCursor cursor = this.filesCollection.find(query);
        if (sort != null) {
            cursor.sort(sort);
        }
        try {
            while (cursor.hasNext()) {
                files.add(this.injectGridFSInstance(cursor.next()));
            }
        }
        finally {
            cursor.close();
        }
        return Collections.unmodifiableList((List<? extends GridFSDBFile>)files);
    }
    
    private GridFSDBFile injectGridFSInstance(final Object o) {
        if (o == null) {
            return null;
        }
        if (!(o instanceof GridFSDBFile)) {
            throw new IllegalArgumentException("somehow didn't get a GridFSDBFile");
        }
        final GridFSDBFile f = (GridFSDBFile)o;
        f.fs = this;
        return f;
    }
    
    public void remove(final ObjectId id) {
        if (id == null) {
            throw new IllegalArgumentException("file id can not be null");
        }
        this.filesCollection.remove(new BasicDBObject("_id", id));
        this.chunksCollection.remove(new BasicDBObject("files_id", id));
    }
    
    public void remove(final String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("filename can not be null");
        }
        this.remove(new BasicDBObject("filename", filename));
    }
    
    public void remove(final DBObject query) {
        if (query == null) {
            throw new IllegalArgumentException("query can not be null");
        }
        for (final GridFSDBFile f : this.find(query)) {
            f.remove();
        }
    }
    
    public GridFSInputFile createFile(final byte[] data) {
        return this.createFile(new ByteArrayInputStream(data), true);
    }
    
    public GridFSInputFile createFile(final File file) throws IOException {
        return this.createFile(new FileInputStream(file), file.getName(), true);
    }
    
    public GridFSInputFile createFile(final InputStream in) {
        return this.createFile(in, null);
    }
    
    public GridFSInputFile createFile(final InputStream in, final boolean closeStreamOnPersist) {
        return this.createFile(in, null, closeStreamOnPersist);
    }
    
    public GridFSInputFile createFile(final InputStream in, final String filename) {
        return new GridFSInputFile(this, in, filename);
    }
    
    public GridFSInputFile createFile(final InputStream in, final String filename, final boolean closeStreamOnPersist) {
        return new GridFSInputFile(this, in, filename, closeStreamOnPersist);
    }
    
    public GridFSInputFile createFile(final String filename) {
        return new GridFSInputFile(this, filename);
    }
    
    public GridFSInputFile createFile() {
        return new GridFSInputFile(this);
    }
    
    public String getBucketName() {
        return this.bucketName;
    }
    
    public DB getDB() {
        return this.database;
    }
    
    protected DBCollection getFilesCollection() {
        return this.filesCollection;
    }
    
    protected DBCollection getChunksCollection() {
        return this.chunksCollection;
    }
}
