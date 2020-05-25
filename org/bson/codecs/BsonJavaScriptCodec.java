package org.bson.codecs;

import org.bson.*;

public class BsonJavaScriptCodec implements Codec<BsonJavaScript>
{
    @Override
    public BsonJavaScript decode(final BsonReader reader, final DecoderContext decoderContext) {
        return new BsonJavaScript(reader.readJavaScript());
    }
    
    @Override
    public void encode(final BsonWriter writer, final BsonJavaScript value, final EncoderContext encoderContext) {
        writer.writeJavaScript(value.getCode());
    }
    
    @Override
    public Class<BsonJavaScript> getEncoderClass() {
        return BsonJavaScript.class;
    }
}
