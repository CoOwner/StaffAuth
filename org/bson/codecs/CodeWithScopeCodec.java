package org.bson.codecs;

import org.bson.types.*;
import org.bson.*;

public class CodeWithScopeCodec implements Codec<CodeWithScope>
{
    private final Codec<Document> documentCodec;
    
    public CodeWithScopeCodec(final Codec<Document> documentCodec) {
        this.documentCodec = documentCodec;
    }
    
    @Override
    public CodeWithScope decode(final BsonReader bsonReader, final DecoderContext decoderContext) {
        final String code = bsonReader.readJavaScriptWithScope();
        final Document scope = this.documentCodec.decode(bsonReader, decoderContext);
        return new CodeWithScope(code, scope);
    }
    
    @Override
    public void encode(final BsonWriter writer, final CodeWithScope codeWithScope, final EncoderContext encoderContext) {
        writer.writeJavaScriptWithScope(codeWithScope.getCode());
        this.documentCodec.encode(writer, codeWithScope.getScope(), encoderContext);
    }
    
    @Override
    public Class<CodeWithScope> getEncoderClass() {
        return CodeWithScope.class;
    }
}
