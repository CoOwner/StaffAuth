package org.bson.codecs;

import org.bson.*;
import org.bson.io.*;

public class RawBsonDocumentCodec implements Codec<RawBsonDocument>
{
    @Override
    public void encode(final BsonWriter writer, final RawBsonDocument value, final EncoderContext encoderContext) {
        final BsonBinaryReader reader = new BsonBinaryReader(new ByteBufferBsonInput(value.getByteBuffer()));
        try {
            writer.pipe(reader);
        }
        finally {
            reader.close();
        }
    }
    
    @Override
    public RawBsonDocument decode(final BsonReader reader, final DecoderContext decoderContext) {
        final BasicOutputBuffer buffer = new BasicOutputBuffer(0);
        final BsonBinaryWriter writer = new BsonBinaryWriter(buffer);
        try {
            writer.pipe(reader);
            return new RawBsonDocument(buffer.getInternalBuffer(), 0, buffer.getPosition());
        }
        finally {
            writer.close();
            buffer.close();
        }
    }
    
    @Override
    public Class<RawBsonDocument> getEncoderClass() {
        return RawBsonDocument.class;
    }
}
