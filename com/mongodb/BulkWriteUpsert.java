package com.mongodb;

public class BulkWriteUpsert
{
    private final int index;
    private final Object id;
    
    public BulkWriteUpsert(final int index, final Object id) {
        this.index = index;
        this.id = id;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public Object getId() {
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
