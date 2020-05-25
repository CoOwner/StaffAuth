package com.mongodb.connection;

import com.mongodb.binding.*;
import com.mongodb.annotations.*;
import java.util.*;
import com.mongodb.async.*;
import com.mongodb.*;
import com.mongodb.bulk.*;
import org.bson.*;
import org.bson.codecs.*;

@ThreadSafe
public interface AsyncConnection extends ReferenceCounted
{
    AsyncConnection retain();
    
    ConnectionDescription getDescription();
    
    void insertAsync(final MongoNamespace p0, final boolean p1, final WriteConcern p2, final List<InsertRequest> p3, final SingleResultCallback<WriteConcernResult> p4);
    
    void updateAsync(final MongoNamespace p0, final boolean p1, final WriteConcern p2, final List<UpdateRequest> p3, final SingleResultCallback<WriteConcernResult> p4);
    
    void deleteAsync(final MongoNamespace p0, final boolean p1, final WriteConcern p2, final List<DeleteRequest> p3, final SingleResultCallback<WriteConcernResult> p4);
    
    @Deprecated
    void insertCommandAsync(final MongoNamespace p0, final boolean p1, final WriteConcern p2, final List<InsertRequest> p3, final SingleResultCallback<BulkWriteResult> p4);
    
    void insertCommandAsync(final MongoNamespace p0, final boolean p1, final WriteConcern p2, final Boolean p3, final List<InsertRequest> p4, final SingleResultCallback<BulkWriteResult> p5);
    
    @Deprecated
    void updateCommandAsync(final MongoNamespace p0, final boolean p1, final WriteConcern p2, final List<UpdateRequest> p3, final SingleResultCallback<BulkWriteResult> p4);
    
    void updateCommandAsync(final MongoNamespace p0, final boolean p1, final WriteConcern p2, final Boolean p3, final List<UpdateRequest> p4, final SingleResultCallback<BulkWriteResult> p5);
    
    void deleteCommandAsync(final MongoNamespace p0, final boolean p1, final WriteConcern p2, final List<DeleteRequest> p3, final SingleResultCallback<BulkWriteResult> p4);
    
     <T> void commandAsync(final String p0, final BsonDocument p1, final boolean p2, final FieldNameValidator p3, final Decoder<T> p4, final SingleResultCallback<T> p5);
    
    @Deprecated
     <T> void queryAsync(final MongoNamespace p0, final BsonDocument p1, final BsonDocument p2, final int p3, final int p4, final boolean p5, final boolean p6, final boolean p7, final boolean p8, final boolean p9, final boolean p10, final Decoder<T> p11, final SingleResultCallback<QueryResult<T>> p12);
    
     <T> void queryAsync(final MongoNamespace p0, final BsonDocument p1, final BsonDocument p2, final int p3, final int p4, final int p5, final boolean p6, final boolean p7, final boolean p8, final boolean p9, final boolean p10, final boolean p11, final Decoder<T> p12, final SingleResultCallback<QueryResult<T>> p13);
    
     <T> void getMoreAsync(final MongoNamespace p0, final long p1, final int p2, final Decoder<T> p3, final SingleResultCallback<QueryResult<T>> p4);
    
    @Deprecated
    void killCursorAsync(final List<Long> p0, final SingleResultCallback<Void> p1);
    
    void killCursorAsync(final MongoNamespace p0, final List<Long> p1, final SingleResultCallback<Void> p2);
}
