package org.bson.codecs;

import org.bson.types.*;
import org.bson.*;

public class CodeCodec implements Codec<Code>
{
    @Override
    public void encode(final BsonWriter writer, final Code value, final EncoderContext encoderContext) {
        writer.writeJavaScript(value.getCode());
    }
    
    @Override
    public Code decode(final BsonReader bsonReader, final DecoderContext decoderContext) {
        return new Code(bsonReader.readJavaScript());
    }
    
    @Override
    public Class<Code> getEncoderClass() {
        return Code.class;
    }
}
