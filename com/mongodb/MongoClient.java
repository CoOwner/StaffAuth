package com.mongodb;

import java.io.*;
import com.mongodb.client.*;
import org.bson.*;
import org.bson.codecs.*;
import com.mongodb.client.model.geojson.codecs.*;
import com.mongodb.client.gridfs.codecs.*;
import java.util.*;
import org.bson.codecs.configuration.*;

public class MongoClient extends Mongo implements Closeable
{
    private static final CodecRegistry DEFAULT_CODEC_REGISTRY;
    
    public static CodecRegistry getDefaultCodecRegistry() {
        return MongoClient.DEFAULT_CODEC_REGISTRY;
    }
    
    public MongoClient() {
        this(new ServerAddress());
    }
    
    public MongoClient(final String host) {
        this(new ServerAddress(host));
    }
    
    public MongoClient(final String host, final MongoClientOptions options) {
        this(new ServerAddress(host), options);
    }
    
    public MongoClient(final String host, final int port) {
        this(new ServerAddress(host, port));
    }
    
    public MongoClient(final ServerAddress addr) {
        this(addr, new MongoClientOptions.Builder().build());
    }
    
    public MongoClient(final ServerAddress addr, final List<MongoCredential> credentialsList) {
        this(addr, credentialsList, new MongoClientOptions.Builder().build());
    }
    
    public MongoClient(final ServerAddress addr, final MongoClientOptions options) {
        super(addr, options);
    }
    
    public MongoClient(final ServerAddress addr, final List<MongoCredential> credentialsList, final MongoClientOptions options) {
        super(addr, credentialsList, options);
    }
    
    public MongoClient(final List<ServerAddress> seeds) {
        this(seeds, new MongoClientOptions.Builder().build());
    }
    
    public MongoClient(final List<ServerAddress> seeds, final List<MongoCredential> credentialsList) {
        this(seeds, credentialsList, new MongoClientOptions.Builder().build());
    }
    
    public MongoClient(final List<ServerAddress> seeds, final MongoClientOptions options) {
        super(seeds, options);
    }
    
    public MongoClient(final List<ServerAddress> seeds, final List<MongoCredential> credentialsList, final MongoClientOptions options) {
        super(seeds, credentialsList, options);
    }
    
    public MongoClient(final MongoClientURI uri) {
        super(uri);
    }
    
    public MongoClient(final MongoClientURI uri, final MongoDriverInformation mongoDriverInformation) {
        super(uri, mongoDriverInformation);
    }
    
    public MongoClient(final ServerAddress addr, final List<MongoCredential> credentialsList, final MongoClientOptions options, final MongoDriverInformation mongoDriverInformation) {
        super(addr, credentialsList, options, mongoDriverInformation);
    }
    
    public MongoClient(final List<ServerAddress> seeds, final List<MongoCredential> credentialsList, final MongoClientOptions options, final MongoDriverInformation mongoDriverInformation) {
        super(seeds, credentialsList, options, mongoDriverInformation);
    }
    
    public MongoClientOptions getMongoClientOptions() {
        return super.getMongoClientOptions();
    }
    
    public List<MongoCredential> getCredentialsList() {
        return super.getCredentialsList();
    }
    
    public MongoIterable<String> listDatabaseNames() {
        return new ListDatabasesIterableImpl<BsonDocument>(BsonDocument.class, getDefaultCodecRegistry(), ReadPreference.primary(), this.createOperationExecutor()).map((Function<BsonDocument, String>)new Function<BsonDocument, String>() {
            @Override
            public String apply(final BsonDocument result) {
                return result.getString("name").getValue();
            }
        });
    }
    
    public ListDatabasesIterable<Document> listDatabases() {
        return this.listDatabases(Document.class);
    }
    
    public <T> ListDatabasesIterable<T> listDatabases(final Class<T> clazz) {
        return new ListDatabasesIterableImpl<T>(clazz, this.getMongoClientOptions().getCodecRegistry(), ReadPreference.primary(), this.createOperationExecutor());
    }
    
    public MongoDatabase getDatabase(final String databaseName) {
        final MongoClientOptions clientOptions = this.getMongoClientOptions();
        return new MongoDatabaseImpl(databaseName, clientOptions.getCodecRegistry(), clientOptions.getReadPreference(), clientOptions.getWriteConcern(), clientOptions.getReadConcern(), this.createOperationExecutor());
    }
    
    static DBObjectCodec getCommandCodec() {
        return new DBObjectCodec(getDefaultCodecRegistry());
    }
    
    static {
        DEFAULT_CODEC_REGISTRY = CodecRegistries.fromProviders(Arrays.asList(new ValueCodecProvider(), new DBRefCodecProvider(), new DocumentCodecProvider(new DocumentToDBRefTransformer()), new DBObjectCodecProvider(), new BsonValueCodecProvider(), new IterableCodecProvider(new DocumentToDBRefTransformer()), new GeoJsonCodecProvider(), new GridFSFileCodecProvider()));
    }
}
