package com.mongodb;

import com.mongodb.client.*;
import com.mongodb.operation.*;
import java.util.*;

class MongoBatchCursorAdapter<T> implements MongoCursor<T>
{
    private final BatchCursor<T> batchCursor;
    private List<T> curBatch;
    private int curPos;
    
    public MongoBatchCursorAdapter(final BatchCursor<T> batchCursor) {
        this.batchCursor = batchCursor;
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cursors do not support removal");
    }
    
    @Override
    public void close() {
        this.batchCursor.close();
    }
    
    @Override
    public boolean hasNext() {
        return this.curBatch != null || this.batchCursor.hasNext();
    }
    
    @Override
    public T next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }
        if (this.curBatch == null) {
            this.curBatch = this.batchCursor.next();
        }
        return this.getNextInBatch();
    }
    
    @Override
    public T tryNext() {
        if (this.curBatch == null) {
            this.curBatch = this.batchCursor.tryNext();
        }
        return (this.curBatch == null) ? null : this.getNextInBatch();
    }
    
    @Override
    public ServerCursor getServerCursor() {
        return this.batchCursor.getServerCursor();
    }
    
    @Override
    public ServerAddress getServerAddress() {
        return this.batchCursor.getServerAddress();
    }
    
    private T getNextInBatch() {
        final T nextInBatch = this.curBatch.get(this.curPos);
        if (this.curPos < this.curBatch.size() - 1) {
            ++this.curPos;
        }
        else {
            this.curBatch = null;
            this.curPos = 0;
        }
        return nextInBatch;
    }
}
