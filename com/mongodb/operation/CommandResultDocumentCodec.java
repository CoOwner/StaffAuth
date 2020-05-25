package com.mongodb.operation;

import org.bson.codecs.configuration.*;
import org.bson.*;
import org.bson.codecs.*;

class CommandResultDocumentCodec<T> extends BsonDocumentCodec
{
    private final Decoder<T> payloadDecoder;
    private final String fieldContainingPayload;
    
    CommandResultDocumentCodec(final CodecRegistry registry, final Decoder<T> payloadDecoder, final String fieldContainingPayload) {
        super(registry);
        this.payloadDecoder = payloadDecoder;
        this.fieldContainingPayload = fieldContainingPayload;
    }
    
    static <P> Codec<BsonDocument> create(final Decoder<P> decoder, final String fieldContainingPayload) {
        final CodecRegistry registry = CodecRegistries.fromProviders(new CommandResultCodecProvider<Object>(decoder, fieldContainingPayload));
        return registry.get(BsonDocument.class);
    }
    
    @Override
    protected BsonValue readValue(final BsonReader reader, final DecoderContext decoderContext) {
        if (reader.getCurrentName().equals(this.fieldContainingPayload)) {
            if (reader.getCurrentBsonType() == BsonType.DOCUMENT) {
                return new BsonDocumentWrapper<Object>(this.payloadDecoder.decode(reader, decoderContext), null);
            }
            if (reader.getCurrentBsonType() == BsonType.ARRAY) {
                return new CommandResultArrayCodec(this.getCodecRegistry(), (Decoder<Object>)this.payloadDecoder).decode(reader, decoderContext);
            }
        }
        return super.readValue(reader, decoderContext);
    }
}
