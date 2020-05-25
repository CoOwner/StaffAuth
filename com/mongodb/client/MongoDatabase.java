package com.mongodb.client;

import com.mongodb.annotations.*;
import org.bson.codecs.configuration.*;
import com.mongodb.*;
import org.bson.*;
import org.bson.conversions.*;
import java.util.*;
import com.mongodb.client.model.*;

@ThreadSafe
public interface MongoDatabase
{
    String getName();
    
    CodecRegistry getCodecRegistry();
    
    ReadPreference getReadPreference();
    
    WriteConcern getWriteConcern();
    
    ReadConcern getReadConcern();
    
    MongoDatabase withCodecRegistry(final CodecRegistry p0);
    
    MongoDatabase withReadPreference(final ReadPreference p0);
    
    MongoDatabase withWriteConcern(final WriteConcern p0);
    
    MongoDatabase withReadConcern(final ReadConcern p0);
    
    MongoCollection<Document> getCollection(final String p0);
    
     <TDocument> MongoCollection<TDocument> getCollection(final String p0, final Class<TDocument> p1);
    
    Document runCommand(final Bson p0);
    
    Document runCommand(final Bson p0, final ReadPreference p1);
    
     <TResult> TResult runCommand(final Bson p0, final Class<TResult> p1);
    
     <TResult> TResult runCommand(final Bson p0, final ReadPreference p1, final Class<TResult> p2);
    
    void drop();
    
    MongoIterable<String> listCollectionNames();
    
    ListCollectionsIterable<Document> listCollections();
    
     <TResult> ListCollectionsIterable<TResult> listCollections(final Class<TResult> p0);
    
    void createCollection(final String p0);
    
    void createCollection(final String p0, final CreateCollectionOptions p1);
    
    void createView(final String p0, final String p1, final List<? extends Bson> p2);
    
    void createView(final String p0, final String p1, final List<? extends Bson> p2, final CreateViewOptions p3);
}
