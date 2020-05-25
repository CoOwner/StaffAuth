package com.mongodb.client.gridfs.model;

@Deprecated
public final class GridFSDownloadByNameOptions
{
    private int revision;
    
    public GridFSDownloadByNameOptions() {
        this.revision = -1;
    }
    
    public GridFSDownloadByNameOptions revision(final int revision) {
        this.revision = revision;
        return this;
    }
    
    public int getRevision() {
        return this.revision;
    }
}
