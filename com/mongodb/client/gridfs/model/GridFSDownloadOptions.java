package com.mongodb.client.gridfs.model;

public final class GridFSDownloadOptions
{
    private int revision;
    
    public GridFSDownloadOptions() {
        this.revision = -1;
    }
    
    public GridFSDownloadOptions revision(final int revision) {
        this.revision = revision;
        return this;
    }
    
    public int getRevision() {
        return this.revision;
    }
}
