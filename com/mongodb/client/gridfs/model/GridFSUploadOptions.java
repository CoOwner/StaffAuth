package com.mongodb.client.gridfs.model;

import org.bson.*;

public final class GridFSUploadOptions
{
    private Integer chunkSizeBytes;
    private Document metadata;
    
    public Integer getChunkSizeBytes() {
        return this.chunkSizeBytes;
    }
    
    public GridFSUploadOptions chunkSizeBytes(final Integer chunkSizeBytes) {
        this.chunkSizeBytes = chunkSizeBytes;
        return this;
    }
    
    public Document getMetadata() {
        return this.metadata;
    }
    
    public GridFSUploadOptions metadata(final Document metadata) {
        this.metadata = metadata;
        return this;
    }
}
