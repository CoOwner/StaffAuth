package com.mongodb;

import org.bson.conversions.*;
import org.bson.json.*;
import java.io.*;
import org.bson.codecs.*;
import com.mongodb.util.*;
import org.bson.codecs.configuration.*;
import org.bson.*;
import java.util.*;

public class BasicDBObject extends BasicBSONObject implements DBObject, Bson
{
    private static final long serialVersionUID = -4415279469780082174L;
    private boolean isPartialObject;
    
    public static BasicDBObject parse(final String json) {
        return parse(json, MongoClient.getDefaultCodecRegistry().get(BasicDBObject.class));
    }
    
    public static BasicDBObject parse(final String json, final Decoder<BasicDBObject> decoder) {
        return decoder.decode(new JsonReader(json), DecoderContext.builder().build());
    }
    
    public BasicDBObject() {
    }
    
    public BasicDBObject(final int size) {
        super(size);
    }
    
    public BasicDBObject(final String key, final Object value) {
        super(key, value);
    }
    
    public BasicDBObject(final Map map) {
        super(map);
    }
    
    @Override
    public BasicDBObject append(final String key, final Object val) {
        this.put(key, val);
        return this;
    }
    
    @Override
    public boolean isPartialObject() {
        return this.isPartialObject;
    }
    
    public String toJson() {
        return this.toJson(new JsonWriterSettings());
    }
    
    public String toJson(final JsonWriterSettings writerSettings) {
        return this.toJson(writerSettings, MongoClient.getDefaultCodecRegistry().get(BasicDBObject.class));
    }
    
    public String toJson(final Encoder<BasicDBObject> encoder) {
        return this.toJson(new JsonWriterSettings(), encoder);
    }
    
    public String toJson(final JsonWriterSettings writerSettings, final Encoder<BasicDBObject> encoder) {
        final JsonWriter writer = new JsonWriter(new StringWriter(), writerSettings);
        encoder.encode(writer, this, EncoderContext.builder().isEncodingCollectibleDocument(true).build());
        return writer.getWriter().toString();
    }
    
    @Override
    public String toString() {
        return JSON.serialize(this);
    }
    
    @Override
    public void markAsPartialObject() {
        this.isPartialObject = true;
    }
    
    public Object copy() {
        final BasicDBObject newCopy = new BasicDBObject(this.toMap());
        for (final String field : ((LinkedHashMap<String, V>)this).keySet()) {
            final Object val = this.get(field);
            if (val instanceof BasicDBObject) {
                newCopy.put(field, ((BasicDBObject)val).copy());
            }
            else {
                if (!(val instanceof BasicDBList)) {
                    continue;
                }
                newCopy.put(field, ((BasicDBList)val).copy());
            }
        }
        return newCopy;
    }
    
    @Override
    public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
        return new BsonDocumentWrapper<Object>(this, codecRegistry.get(BasicDBObject.class));
    }
}
