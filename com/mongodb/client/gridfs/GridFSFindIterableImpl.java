package com.mongodb.client.gridfs;

import com.mongodb.client.gridfs.model.*;
import org.bson.conversions.*;
import java.util.concurrent.*;
import com.mongodb.client.model.*;
import com.mongodb.client.*;
import com.mongodb.*;
import java.util.*;

class GridFSFindIterableImpl implements GridFSFindIterable
{
    private final FindIterable<GridFSFile> underlying;
    
    public GridFSFindIterableImpl(final FindIterable<GridFSFile> underlying) {
        this.underlying = underlying;
    }
    
    @Override
    public GridFSFindIterable sort(final Bson sort) {
        this.underlying.sort(sort);
        return this;
    }
    
    @Override
    public GridFSFindIterable skip(final int skip) {
        this.underlying.skip(skip);
        return this;
    }
    
    @Override
    public GridFSFindIterable limit(final int limit) {
        this.underlying.limit(limit);
        return this;
    }
    
    @Override
    public GridFSFindIterable filter(final Bson filter) {
        this.underlying.filter(filter);
        return this;
    }
    
    @Override
    public GridFSFindIterable maxTime(final long maxTime, final TimeUnit timeUnit) {
        this.underlying.maxTime(maxTime, timeUnit);
        return this;
    }
    
    @Override
    public GridFSFindIterable batchSize(final int batchSize) {
        this.underlying.batchSize(batchSize);
        return this;
    }
    
    @Override
    public GridFSFindIterable collation(final Collation collation) {
        this.underlying.collation(collation);
        return this;
    }
    
    @Override
    public GridFSFindIterable noCursorTimeout(final boolean noCursorTimeout) {
        this.underlying.noCursorTimeout(noCursorTimeout);
        return this;
    }
    
    @Override
    public MongoCursor<GridFSFile> iterator() {
        return this.underlying.iterator();
    }
    
    @Override
    public GridFSFile first() {
        return this.underlying.first();
    }
    
    @Override
    public <U> MongoIterable<U> map(final Function<GridFSFile, U> mapper) {
        return this.underlying.map(mapper);
    }
    
    @Override
    public void forEach(final Block<? super GridFSFile> block) {
        this.underlying.forEach(block);
    }
    
    @Override
    public <A extends Collection<? super GridFSFile>> A into(final A target) {
        return this.underlying.into(target);
    }
}
