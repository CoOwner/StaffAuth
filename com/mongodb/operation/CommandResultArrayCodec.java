package com.mongodb.operation;

import org.bson.codecs.configuration.*;
import java.util.*;
import org.bson.*;
import org.bson.codecs.*;

class CommandResultArrayCodec<T> extends BsonArrayCodec
{
    private final Decoder<T> decoder;
    
    CommandResultArrayCodec(final CodecRegistry registry, final Decoder<T> decoder) {
        super(registry);
        this.decoder = decoder;
    }
    
    @Override
    public BsonArray decode(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readStartArray();
        final List<T> list = new ArrayList<T>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            if (reader.getCurrentBsonType() == BsonType.NULL) {
                reader.readNull();
                list.add(null);
            }
            else {
                list.add(this.decoder.decode(reader, decoderContext));
            }
        }
        reader.readEndArray();
        return new BsonArrayWrapper<Object>(list);
    }
    
    @Override
    protected BsonValue readValue(final BsonReader reader, final DecoderContext decoderContext) {
        if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
            return new BsonDocumentWrapper<Object>(this.decoder.decode(reader, decoderContext), null);
        }
        return super.readValue(reader, decoderContext);
    }
}
