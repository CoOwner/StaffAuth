package com.mongodb;

import com.mongodb.client.*;

class MongoCursorAdapter implements Cursor
{
    private final MongoCursor<DBObject> cursor;
    
    public MongoCursorAdapter(final MongoCursor<DBObject> cursor) {
        this.cursor = cursor;
    }
    
    @Override
    public long getCursorId() {
        return this.cursor.getServerCursor().getId();
    }
    
    @Override
    public ServerAddress getServerAddress() {
        return this.cursor.getServerAddress();
    }
    
    @Override
    public void close() {
        this.cursor.close();
    }
    
    @Override
    public boolean hasNext() {
        return this.cursor.hasNext();
    }
    
    @Override
    public DBObject next() {
        return this.cursor.next();
    }
    
    @Override
    public void remove() {
        this.cursor.remove();
    }
}
