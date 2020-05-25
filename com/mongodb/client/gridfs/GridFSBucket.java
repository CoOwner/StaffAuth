package com.mongodb.client.gridfs;

import com.mongodb.annotations.*;
import com.mongodb.*;
import org.bson.*;
import org.bson.types.*;
import java.io.*;
import org.bson.conversions.*;
import com.mongodb.client.gridfs.model.*;

@ThreadSafe
public interface GridFSBucket
{
    String getBucketName();
    
    int getChunkSizeBytes();
    
    WriteConcern getWriteConcern();
    
    ReadPreference getReadPreference();
    
    ReadConcern getReadConcern();
    
    GridFSBucket withChunkSizeBytes(final int p0);
    
    GridFSBucket withReadPreference(final ReadPreference p0);
    
    GridFSBucket withWriteConcern(final WriteConcern p0);
    
    GridFSBucket withReadConcern(final ReadConcern p0);
    
    GridFSUploadStream openUploadStream(final String p0);
    
    GridFSUploadStream openUploadStream(final String p0, final GridFSUploadOptions p1);
    
    GridFSUploadStream openUploadStream(final BsonValue p0, final String p1);
    
    GridFSUploadStream openUploadStream(final BsonValue p0, final String p1, final GridFSUploadOptions p2);
    
    ObjectId uploadFromStream(final String p0, final InputStream p1);
    
    ObjectId uploadFromStream(final String p0, final InputStream p1, final GridFSUploadOptions p2);
    
    void uploadFromStream(final BsonValue p0, final String p1, final InputStream p2);
    
    void uploadFromStream(final BsonValue p0, final String p1, final InputStream p2, final GridFSUploadOptions p3);
    
    GridFSDownloadStream openDownloadStream(final ObjectId p0);
    
    void downloadToStream(final ObjectId p0, final OutputStream p1);
    
    GridFSDownloadStream openDownloadStream(final BsonValue p0);
    
    void downloadToStream(final BsonValue p0, final OutputStream p1);
    
    void downloadToStream(final String p0, final OutputStream p1);
    
    void downloadToStream(final String p0, final OutputStream p1, final GridFSDownloadOptions p2);
    
    GridFSDownloadStream openDownloadStream(final String p0);
    
    GridFSDownloadStream openDownloadStream(final String p0, final GridFSDownloadOptions p1);
    
    GridFSFindIterable find();
    
    GridFSFindIterable find(final Bson p0);
    
    void delete(final ObjectId p0);
    
    void delete(final BsonValue p0);
    
    void rename(final ObjectId p0, final String p1);
    
    void rename(final BsonValue p0, final String p1);
    
    void drop();
    
    @Deprecated
    GridFSDownloadStream openDownloadStreamByName(final String p0);
    
    @Deprecated
    GridFSDownloadStream openDownloadStreamByName(final String p0, final GridFSDownloadByNameOptions p1);
    
    @Deprecated
    void downloadToStreamByName(final String p0, final OutputStream p1);
    
    @Deprecated
    void downloadToStreamByName(final String p0, final OutputStream p1, final GridFSDownloadByNameOptions p2);
}
