package com.mongodb;

import com.mongodb.assertions.*;
import org.bson.*;

public final class ReadConcern
{
    private final ReadConcernLevel readConcernLevel;
    public static final ReadConcern DEFAULT;
    public static final ReadConcern LOCAL;
    public static final ReadConcern MAJORITY;
    public static final ReadConcern LINEARIZABLE;
    
    public ReadConcern(final ReadConcernLevel readConcernLevel) {
        this.readConcernLevel = Assertions.notNull("readConcernLevel", readConcernLevel);
    }
    
    public boolean isServerDefault() {
        return this.readConcernLevel == null;
    }
    
    public BsonDocument asDocument() {
        final BsonDocument readConcern = new BsonDocument();
        if (!this.isServerDefault()) {
            readConcern.put("level", new BsonString(this.readConcernLevel.getValue()));
        }
        return readConcern;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        final ReadConcern that = (ReadConcern)o;
        return this.readConcernLevel == that.readConcernLevel;
    }
    
    @Override
    public int hashCode() {
        return (this.readConcernLevel != null) ? this.readConcernLevel.hashCode() : 0;
    }
    
    private ReadConcern() {
        this.readConcernLevel = null;
    }
    
    static {
        DEFAULT = new ReadConcern();
        LOCAL = new ReadConcern(ReadConcernLevel.LOCAL);
        MAJORITY = new ReadConcern(ReadConcernLevel.MAJORITY);
        LINEARIZABLE = new ReadConcern(ReadConcernLevel.LINEARIZABLE);
    }
}
