package com.mongodb.client.gridfs;

import com.mongodb.client.*;
import com.mongodb.assertions.*;
import org.bson.types.*;
import java.io.*;
import org.bson.conversions.*;
import org.bson.*;
import com.mongodb.client.result.*;
import com.mongodb.client.gridfs.model.*;
import com.mongodb.*;
import org.bson.codecs.configuration.*;
import com.mongodb.client.model.*;
import java.util.*;

final class GridFSBucketImpl implements GridFSBucket
{
    private static final int DEFAULT_CHUNKSIZE_BYTES = 261120;
    private final String bucketName;
    private final int chunkSizeBytes;
    private final MongoCollection<GridFSFile> filesCollection;
    private final MongoCollection<Document> chunksCollection;
    private volatile boolean checkedIndexes;
    
    GridFSBucketImpl(final MongoDatabase database) {
        this(database, "fs");
    }
    
    GridFSBucketImpl(final MongoDatabase database, final String bucketName) {
        this(Assertions.notNull("bucketName", bucketName), 261120, getFilesCollection(Assertions.notNull("database", database), bucketName), getChunksCollection(database, bucketName));
    }
    
    GridFSBucketImpl(final String bucketName, final int chunkSizeBytes, final MongoCollection<GridFSFile> filesCollection, final MongoCollection<Document> chunksCollection) {
        this.bucketName = Assertions.notNull("bucketName", bucketName);
        this.chunkSizeBytes = chunkSizeBytes;
        this.filesCollection = Assertions.notNull("filesCollection", filesCollection);
        this.chunksCollection = Assertions.notNull("chunksCollection", chunksCollection);
    }
    
    @Override
    public String getBucketName() {
        return this.bucketName;
    }
    
    @Override
    public int getChunkSizeBytes() {
        return this.chunkSizeBytes;
    }
    
    @Override
    public ReadPreference getReadPreference() {
        return this.filesCollection.getReadPreference();
    }
    
    @Override
    public WriteConcern getWriteConcern() {
        return this.filesCollection.getWriteConcern();
    }
    
    @Override
    public ReadConcern getReadConcern() {
        return this.filesCollection.getReadConcern();
    }
    
    @Override
    public GridFSBucket withChunkSizeBytes(final int chunkSizeBytes) {
        return new GridFSBucketImpl(this.bucketName, chunkSizeBytes, this.filesCollection, this.chunksCollection);
    }
    
    @Override
    public GridFSBucket withReadPreference(final ReadPreference readPreference) {
        return new GridFSBucketImpl(this.bucketName, this.chunkSizeBytes, this.filesCollection.withReadPreference(readPreference), this.chunksCollection.withReadPreference(readPreference));
    }
    
    @Override
    public GridFSBucket withWriteConcern(final WriteConcern writeConcern) {
        return new GridFSBucketImpl(this.bucketName, this.chunkSizeBytes, this.filesCollection.withWriteConcern(writeConcern), this.chunksCollection.withWriteConcern(writeConcern));
    }
    
    @Override
    public GridFSBucket withReadConcern(final ReadConcern readConcern) {
        return new GridFSBucketImpl(this.bucketName, this.chunkSizeBytes, this.filesCollection.withReadConcern(readConcern), this.chunksCollection.withReadConcern(readConcern));
    }
    
    @Override
    public GridFSUploadStream openUploadStream(final String filename) {
        return this.openUploadStream(new BsonObjectId(), filename);
    }
    
    @Override
    public GridFSUploadStream openUploadStream(final String filename, final GridFSUploadOptions options) {
        return this.openUploadStream(new BsonObjectId(), filename, options);
    }
    
    @Override
    public GridFSUploadStream openUploadStream(final BsonValue id, final String filename) {
        return this.openUploadStream(id, filename, new GridFSUploadOptions());
    }
    
    @Override
    public GridFSUploadStream openUploadStream(final BsonValue id, final String filename, final GridFSUploadOptions options) {
        final int chunkSize = (options.getChunkSizeBytes() == null) ? this.chunkSizeBytes : options.getChunkSizeBytes();
        this.checkCreateIndex();
        return new GridFSUploadStreamImpl(this.filesCollection, this.chunksCollection, id, filename, chunkSize, options.getMetadata());
    }
    
    @Override
    public ObjectId uploadFromStream(final String filename, final InputStream source) {
        return this.uploadFromStream(filename, source, new GridFSUploadOptions());
    }
    
    @Override
    public ObjectId uploadFromStream(final String filename, final InputStream source, final GridFSUploadOptions options) {
        final ObjectId id = new ObjectId();
        this.uploadFromStream(new BsonObjectId(id), filename, source, options);
        return id;
    }
    
    @Override
    public void uploadFromStream(final BsonValue id, final String filename, final InputStream source) {
        this.uploadFromStream(id, filename, source, new GridFSUploadOptions());
    }
    
    @Override
    public void uploadFromStream(final BsonValue id, final String filename, final InputStream source, final GridFSUploadOptions options) {
        final GridFSUploadStream uploadStream = this.openUploadStream(id, filename, options);
        final int chunkSize = (options.getChunkSizeBytes() == null) ? this.chunkSizeBytes : options.getChunkSizeBytes();
        final byte[] buffer = new byte[chunkSize];
        try {
            int len;
            while ((len = source.read(buffer)) != -1) {
                uploadStream.write(buffer, 0, len);
            }
            uploadStream.close();
        }
        catch (IOException e) {
            uploadStream.abort();
            throw new MongoGridFSException("IOException when reading from the InputStream", e);
        }
    }
    
    @Override
    public GridFSDownloadStream openDownloadStream(final ObjectId id) {
        return this.findTheFileInfoAndOpenDownloadStream(new BsonObjectId(id));
    }
    
    @Override
    public void downloadToStream(final ObjectId id, final OutputStream destination) {
        this.downloadToStream(this.findTheFileInfoAndOpenDownloadStream(new BsonObjectId(id)), destination);
    }
    
    @Override
    public void downloadToStream(final BsonValue id, final OutputStream destination) {
        this.downloadToStream(this.findTheFileInfoAndOpenDownloadStream(id), destination);
    }
    
    @Override
    public void downloadToStream(final String filename, final OutputStream destination) {
        this.downloadToStream(filename, destination, new GridFSDownloadOptions());
    }
    
    @Override
    public void downloadToStream(final String filename, final OutputStream destination, final GridFSDownloadOptions options) {
        this.downloadToStream(this.openDownloadStream(filename, options), destination);
    }
    
    @Override
    public GridFSDownloadStream openDownloadStream(final BsonValue id) {
        return this.findTheFileInfoAndOpenDownloadStream(id);
    }
    
    @Override
    public GridFSDownloadStream openDownloadStream(final String filename) {
        return this.openDownloadStream(filename, new GridFSDownloadOptions());
    }
    
    @Override
    public GridFSDownloadStream openDownloadStream(final String filename, final GridFSDownloadOptions options) {
        return new GridFSDownloadStreamImpl(this.getFileByName(filename, options), this.chunksCollection);
    }
    
    @Override
    public GridFSFindIterable find() {
        return new GridFSFindIterableImpl(this.filesCollection.find());
    }
    
    @Override
    public GridFSFindIterable find(final Bson filter) {
        return this.find().filter(filter);
    }
    
    @Override
    public void delete(final ObjectId id) {
        this.delete(new BsonObjectId(id));
    }
    
    @Override
    public void delete(final BsonValue id) {
        final DeleteResult result = this.filesCollection.deleteOne(new BsonDocument("_id", id));
        this.chunksCollection.deleteMany(new BsonDocument("files_id", id));
        if (result.wasAcknowledged() && result.getDeletedCount() == 0L) {
            throw new MongoGridFSException(String.format("No file found with the id: %s", id));
        }
    }
    
    @Override
    public void rename(final ObjectId id, final String newFilename) {
        this.rename(new BsonObjectId(id), newFilename);
    }
    
    @Override
    public void rename(final BsonValue id, final String newFilename) {
        final UpdateResult updateResult = this.filesCollection.updateOne(new BsonDocument("_id", id), new BsonDocument("$set", new BsonDocument("filename", new BsonString(newFilename))));
        if (updateResult.wasAcknowledged() && updateResult.getMatchedCount() == 0L) {
            throw new MongoGridFSException(String.format("No file found with the id: %s", id));
        }
    }
    
    @Override
    public void drop() {
        this.filesCollection.drop();
        this.chunksCollection.drop();
    }
    
    @Deprecated
    @Override
    public GridFSDownloadStream openDownloadStreamByName(final String filename) {
        return this.openDownloadStreamByName(filename, new GridFSDownloadByNameOptions());
    }
    
    @Deprecated
    @Override
    public GridFSDownloadStream openDownloadStreamByName(final String filename, final GridFSDownloadByNameOptions options) {
        return this.openDownloadStream(filename, new GridFSDownloadOptions().revision(options.getRevision()));
    }
    
    @Deprecated
    @Override
    public void downloadToStreamByName(final String filename, final OutputStream destination) {
        this.downloadToStreamByName(filename, destination, new GridFSDownloadByNameOptions());
    }
    
    @Deprecated
    @Override
    public void downloadToStreamByName(final String filename, final OutputStream destination, final GridFSDownloadByNameOptions options) {
        this.downloadToStream(filename, destination, new GridFSDownloadOptions().revision(options.getRevision()));
    }
    
    private static MongoCollection<GridFSFile> getFilesCollection(final MongoDatabase database, final String bucketName) {
        return database.getCollection(bucketName + ".files", GridFSFile.class).withCodecRegistry(CodecRegistries.fromRegistries(database.getCodecRegistry(), MongoClient.getDefaultCodecRegistry()));
    }
    
    private static MongoCollection<Document> getChunksCollection(final MongoDatabase database, final String bucketName) {
        return database.getCollection(bucketName + ".chunks").withCodecRegistry(MongoClient.getDefaultCodecRegistry());
    }
    
    private void checkCreateIndex() {
        if (!this.checkedIndexes) {
            if (this.filesCollection.withDocumentClass(Document.class).withReadPreference(ReadPreference.primary()).find().projection(new Document("_id", 1)).first() == null) {
                final Document filesIndex = new Document("filename", 1).append("uploadDate", 1);
                if (!this.hasIndex(this.filesCollection.withReadPreference(ReadPreference.primary()), filesIndex)) {
                    this.filesCollection.createIndex(filesIndex);
                }
                final Document chunksIndex = new Document("files_id", 1).append("n", 1);
                if (!this.hasIndex(this.chunksCollection.withReadPreference(ReadPreference.primary()), chunksIndex)) {
                    this.chunksCollection.createIndex(chunksIndex, new IndexOptions().unique(true));
                }
            }
            this.checkedIndexes = true;
        }
    }
    
    private <T> boolean hasIndex(final MongoCollection<T> collection, final Document index) {
        boolean hasIndex = false;
        final ArrayList<Document> indexes = collection.listIndexes().into(new ArrayList<Document>());
        for (final Document indexDoc : indexes) {
            if (indexDoc.get("key", Document.class).equals(index)) {
                hasIndex = true;
                break;
            }
        }
        return hasIndex;
    }
    
    private GridFSFile getFileByName(final String filename, final GridFSDownloadOptions options) {
        final int revision = options.getRevision();
        int skip;
        int sort;
        if (revision >= 0) {
            skip = revision;
            sort = 1;
        }
        else {
            skip = -revision - 1;
            sort = -1;
        }
        final GridFSFile fileInfo = this.find(new Document("filename", filename)).skip(skip).sort(new Document("uploadDate", sort)).first();
        if (fileInfo == null) {
            throw new MongoGridFSException(String.format("No file found with the filename: %s and revision: %s", filename, revision));
        }
        return fileInfo;
    }
    
    private GridFSDownloadStream findTheFileInfoAndOpenDownloadStream(final BsonValue id) {
        final GridFSFile fileInfo = this.find(new Document("_id", id)).first();
        if (fileInfo == null) {
            throw new MongoGridFSException(String.format("No file found with the id: %s", id));
        }
        return new GridFSDownloadStreamImpl(fileInfo, this.chunksCollection);
    }
    
    private void downloadToStream(final GridFSDownloadStream downloadStream, final OutputStream destination) {
        final byte[] buffer = new byte[downloadStream.getGridFSFile().getChunkSize()];
        MongoGridFSException savedThrowable = null;
        try {
            int len;
            while ((len = downloadStream.read(buffer)) != -1) {
                destination.write(buffer, 0, len);
            }
            try {
                downloadStream.close();
            }
            catch (Exception ex) {}
            if (savedThrowable != null) {
                throw savedThrowable;
            }
        }
        catch (IOException e) {
            savedThrowable = new MongoGridFSException("IOException when reading from the OutputStream", e);
        }
        catch (Exception e2) {
            savedThrowable = new MongoGridFSException("Unexpected Exception when reading GridFS and writing to the Stream", e2);
        }
        finally {
            try {
                downloadStream.close();
            }
            catch (Exception ex2) {}
            if (savedThrowable != null) {
                throw savedThrowable;
            }
        }
    }
}
