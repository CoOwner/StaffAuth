package com.mongodb.client.model;

public class RenameCollectionOptions
{
    private boolean dropTarget;
    
    public boolean isDropTarget() {
        return this.dropTarget;
    }
    
    public RenameCollectionOptions dropTarget(final boolean dropTarget) {
        this.dropTarget = dropTarget;
        return this;
    }
}
