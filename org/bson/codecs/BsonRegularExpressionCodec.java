package org.bson.codecs;

import org.bson.*;

public class BsonRegularExpressionCodec implements Codec<BsonRegularExpression>
{
    @Override
    public BsonRegularExpression decode(final BsonReader reader, final DecoderContext decoderContext) {
        return reader.readRegularExpression();
    }
    
    @Override
    public void encode(final BsonWriter writer, final BsonRegularExpression value, final EncoderContext encoderContext) {
        writer.writeRegularExpression(value);
    }
    
    @Override
    public Class<BsonRegularExpression> getEncoderClass() {
        return BsonRegularExpression.class;
    }
}
