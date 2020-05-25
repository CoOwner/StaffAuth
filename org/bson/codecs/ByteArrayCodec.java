package org.bson.codecs;

import org.bson.*;

public class ByteArrayCodec implements Codec<byte[]>
{
    @Override
    public void encode(final BsonWriter writer, final byte[] value, final EncoderContext encoderContext) {
        writer.writeBinaryData(new BsonBinary(value));
    }
    
    @Override
    public byte[] decode(final BsonReader reader, final DecoderContext decoderContext) {
        return reader.readBinaryData().getData();
    }
    
    @Override
    public Class<byte[]> getEncoderClass() {
        return byte[].class;
    }
}
