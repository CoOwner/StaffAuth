package com.mongodb.bulk;

import org.bson.*;

public class BulkWriteUpsert
{
    private final int index;
    private final BsonValue id;
    
    public BulkWriteUpsert(final int index, final BsonValue id) {
        this.index = index;
        this.id = id;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public BsonValue getId() {
        return this.id;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BulkWriteUpsert that = (BulkWriteUpsert)o;
        return this.index == that.index && this.id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        int result = this.index;
        result = 31 * result + this.id.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "BulkWriteUpsert{index=" + this.index + ", id=" + this.id + '}';
    }
}
