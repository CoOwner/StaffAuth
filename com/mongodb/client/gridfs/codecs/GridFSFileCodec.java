package com.mongodb.client.gridfs.codecs;

import com.mongodb.client.gridfs.model.*;
import org.bson.codecs.configuration.*;
import com.mongodb.assertions.*;
import org.bson.codecs.*;
import org.bson.*;
import java.util.*;

public final class GridFSFileCodec implements Codec<GridFSFile>
{
    private static final List<String> VALID_FIELDS;
    private final Codec<Document> documentCodec;
    private final Codec<BsonDocument> bsonDocumentCodec;
    
    public GridFSFileCodec(final CodecRegistry registry) {
        this.documentCodec = Assertions.notNull("DocumentCodec", Assertions.notNull("registry", registry).get(Document.class));
        this.bsonDocumentCodec = Assertions.notNull("BsonDocumentCodec", registry.get(BsonDocument.class));
    }
    
    @Override
    public GridFSFile decode(final BsonReader reader, final DecoderContext decoderContext) {
        final BsonDocument bsonDocument = this.bsonDocumentCodec.decode(reader, decoderContext);
        final BsonValue id = bsonDocument.get("_id");
        final String filename = bsonDocument.getString("filename").getValue();
        final long length = bsonDocument.getNumber("length").longValue();
        final int chunkSize = bsonDocument.getNumber("chunkSize").intValue();
        final Date uploadDate = new Date(bsonDocument.getDateTime("uploadDate").getValue());
        final String md5 = bsonDocument.getString("md5").getValue();
        final BsonDocument metadataBsonDocument = bsonDocument.getDocument("metadata", new BsonDocument());
        final Document optionalMetadata = this.asDocumentOrNull(metadataBsonDocument);
        for (final String key : GridFSFileCodec.VALID_FIELDS) {
            bsonDocument.remove(key);
        }
        final Document deprecatedExtraElements = this.asDocumentOrNull(bsonDocument);
        return new GridFSFile(id, filename, length, chunkSize, uploadDate, md5, optionalMetadata, deprecatedExtraElements);
    }
    
    @Override
    public void encode(final BsonWriter writer, final GridFSFile value, final EncoderContext encoderContext) {
        final BsonDocument bsonDocument = new BsonDocument();
        bsonDocument.put("_id", value.getId());
        bsonDocument.put("filename", new BsonString(value.getFilename()));
        bsonDocument.put("length", new BsonInt64(value.getLength()));
        bsonDocument.put("chunkSize", new BsonInt32(value.getChunkSize()));
        bsonDocument.put("uploadDate", new BsonDateTime(value.getUploadDate().getTime()));
        bsonDocument.put("md5", new BsonString(value.getMD5()));
        final Document metadata = value.getMetadata();
        if (metadata != null) {
            bsonDocument.put("metadata", new BsonDocumentWrapper<Object>(metadata, this.documentCodec));
        }
        final Document extraElements = value.getExtraElements();
        if (extraElements != null) {
            bsonDocument.putAll(new BsonDocumentWrapper<Object>(extraElements, this.documentCodec));
        }
        this.bsonDocumentCodec.encode(writer, bsonDocument, encoderContext);
    }
    
    @Override
    public Class<GridFSFile> getEncoderClass() {
        return GridFSFile.class;
    }
    
    private Document asDocumentOrNull(final BsonDocument bsonDocument) {
        if (bsonDocument.isEmpty()) {
            return null;
        }
        final BsonDocumentReader reader = new BsonDocumentReader(bsonDocument);
        return this.documentCodec.decode(reader, DecoderContext.builder().build());
    }
    
    static {
        VALID_FIELDS = Arrays.asList("_id", "filename", "length", "chunkSize", "uploadDate", "md5", "metadata");
    }
}
