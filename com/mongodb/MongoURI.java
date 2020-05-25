package com.mongodb;

import java.util.*;

@Deprecated
public class MongoURI
{
    public static final String MONGODB_PREFIX = "mongodb://";
    private final MongoClientURI proxied;
    private final MongoOptions options;
    
    @Deprecated
    public MongoURI(final String uri) {
        this.proxied = new MongoClientURI(uri, MongoClientOptions.builder().connectionsPerHost(10).writeConcern(WriteConcern.UNACKNOWLEDGED));
        this.options = new MongoOptions(this.proxied.getOptions());
    }
    
    @Deprecated
    public MongoURI(final MongoClientURI proxied) {
        this.proxied = proxied;
        this.options = new MongoOptions(proxied.getOptions());
    }
    
    public String getUsername() {
        return this.proxied.getUsername();
    }
    
    public char[] getPassword() {
        return this.proxied.getPassword();
    }
    
    public List<String> getHosts() {
        return this.proxied.getHosts();
    }
    
    public String getDatabase() {
        return this.proxied.getDatabase();
    }
    
    public String getCollection() {
        return this.proxied.getCollection();
    }
    
    public MongoCredential getCredentials() {
        return this.proxied.getCredentials();
    }
    
    public MongoOptions getOptions() {
        return this.options;
    }
    
    public Mongo connect() {
        return new Mongo(this);
    }
    
    public DB connectDB() {
        return this.connect().getDB(this.getDatabase());
    }
    
    public DB connectDB(final Mongo mongo) {
        return mongo.getDB(this.getDatabase());
    }
    
    public DBCollection connectCollection(final DB db) {
        return db.getCollection(this.getCollection());
    }
    
    public DBCollection connectCollection(final Mongo mongo) {
        return this.connectDB(mongo).getCollection(this.getCollection());
    }
    
    @Override
    public String toString() {
        return this.proxied.toString();
    }
    
    MongoClientURI toClientURI() {
        return this.proxied;
    }
}
