package com.mongodb.client.model;

import org.bson.conversions.*;

public class PushOptions
{
    private Integer position;
    private Integer slice;
    private Integer sort;
    private Bson sortDocument;
    
    public Integer getPosition() {
        return this.position;
    }
    
    public PushOptions position(final Integer position) {
        this.position = position;
        return this;
    }
    
    public Integer getSlice() {
        return this.slice;
    }
    
    public PushOptions slice(final Integer slice) {
        this.slice = slice;
        return this;
    }
    
    public Integer getSort() {
        return this.sort;
    }
    
    public PushOptions sort(final Integer sort) {
        if (this.sortDocument != null) {
            throw new IllegalStateException("sort can not be set if sortDocument already is");
        }
        this.sort = sort;
        return this;
    }
    
    public Bson getSortDocument() {
        return this.sortDocument;
    }
    
    public PushOptions sortDocument(final Bson sortDocument) {
        if (this.sort != null) {
            throw new IllegalStateException("sortDocument can not be set if sort already is");
        }
        this.sortDocument = sortDocument;
        return this;
    }
    
    @Override
    public String toString() {
        return "Push Options{position=" + this.position + ", slice=" + this.slice + ((this.sort == null) ? "" : (", sort=" + this.sort)) + ((this.sortDocument == null) ? "" : (", sortDocument=" + this.sortDocument)) + '}';
    }
}
