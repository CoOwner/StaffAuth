package com.mongodb.client.gridfs;

import com.mongodb.client.*;
import com.mongodb.client.gridfs.model.*;
import org.bson.conversions.*;
import java.util.concurrent.*;
import com.mongodb.client.model.*;

public interface GridFSFindIterable extends MongoIterable<GridFSFile>
{
    GridFSFindIterable filter(final Bson p0);
    
    GridFSFindIterable limit(final int p0);
    
    GridFSFindIterable skip(final int p0);
    
    GridFSFindIterable sort(final Bson p0);
    
    GridFSFindIterable noCursorTimeout(final boolean p0);
    
    GridFSFindIterable maxTime(final long p0, final TimeUnit p1);
    
    GridFSFindIterable batchSize(final int p0);
    
    GridFSFindIterable collation(final Collation p0);
}
