package me.conflicted.staffauth.Mongo;

import com.mongodb.*;
import java.util.*;

public class Mongo
{
    private MongoClient mongo;
    private DB db;
    private Credentials creds;
    
    public Mongo(final String username, final String password, final String database, final String host, final int port) {
        this.mongo = null;
        this.db = null;
        this.creds = Credentials.getCreds();
        final MongoCredential creds = MongoCredential.createCredential(username, database, password.toCharArray());
        this.mongo = new MongoClient(new ServerAddress(host, port), Arrays.asList(creds));
    }
    
    public MongoClient getMongo() {
        if (this.mongo == null) {
            new Mongo(this.creds.user, this.creds.pass, this.creds.db, this.creds.ip, this.creds.port);
        }
        return this.mongo;
    }
    
    public DB getDatabase() {
        if (this.db == null) {
            this.db = this.getMongo().getDB(this.creds.db);
        }
        return this.db;
    }
    
    public void setDatabase(final String name) {
        this.db = this.getMongo().getDB(name);
    }
    
    public void close() {
        if (this.mongo != null) {
            this.mongo.close();
        }
    }
    
    public DB getDb() {
        return this.db;
    }
}
