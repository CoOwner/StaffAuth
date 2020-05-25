package org.bson.codecs;

import org.bson.*;

public class BsonJavaScriptWithScopeCodec implements Codec<BsonJavaScriptWithScope>
{
    private final Codec<BsonDocument> documentCodec;
    
    public BsonJavaScriptWithScopeCodec(final Codec<BsonDocument> documentCodec) {
        this.documentCodec = documentCodec;
    }
    
    @Override
    public BsonJavaScriptWithScope decode(final BsonReader bsonReader, final DecoderContext decoderContext) {
        final String code = bsonReader.readJavaScriptWithScope();
        final BsonDocument scope = this.documentCodec.decode(bsonReader, decoderContext);
        return new BsonJavaScriptWithScope(code, scope);
    }
    
    @Override
    public void encode(final BsonWriter writer, final BsonJavaScriptWithScope codeWithScope, final EncoderContext encoderContext) {
        writer.writeJavaScriptWithScope(codeWithScope.getCode());
        this.documentCodec.encode(writer, codeWithScope.getScope(), encoderContext);
    }
    
    @Override
    public Class<BsonJavaScriptWithScope> getEncoderClass() {
        return BsonJavaScriptWithScope.class;
    }
}
