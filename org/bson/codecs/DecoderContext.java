package org.bson.codecs;

public final class DecoderContext
{
    public static Builder builder() {
        return new Builder();
    }
    
    private DecoderContext(final Builder builder) {
    }
    
    public static final class Builder
    {
        private Builder() {
        }
        
        public DecoderContext build() {
            return new DecoderContext(this, null);
        }
    }
}
