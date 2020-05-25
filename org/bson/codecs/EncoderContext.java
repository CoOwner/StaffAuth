package org.bson.codecs;

import org.bson.*;

public final class EncoderContext
{
    private static final EncoderContext DEFAULT_CONTEXT;
    private final boolean encodingCollectibleDocument;
    
    public static Builder builder() {
        return new Builder();
    }
    
    public boolean isEncodingCollectibleDocument() {
        return this.encodingCollectibleDocument;
    }
    
    public <T> void encodeWithChildContext(final Encoder<T> encoder, final BsonWriter writer, final T value) {
        encoder.encode(writer, value, EncoderContext.DEFAULT_CONTEXT);
    }
    
    public EncoderContext getChildContext() {
        return EncoderContext.DEFAULT_CONTEXT;
    }
    
    private EncoderContext(final Builder builder) {
        this.encodingCollectibleDocument = builder.encodingCollectibleDocument;
    }
    
    static {
        DEFAULT_CONTEXT = builder().build();
    }
    
    public static final class Builder
    {
        private boolean encodingCollectibleDocument;
        
        private Builder() {
        }
        
        public Builder isEncodingCollectibleDocument(final boolean encodingCollectibleDocument) {
            this.encodingCollectibleDocument = encodingCollectibleDocument;
            return this;
        }
        
        public EncoderContext build() {
            return new EncoderContext(this, null);
        }
    }
}
