package org.bson.codecs.configuration;

import org.bson.*;
import org.bson.codecs.*;

class LazyCodec<T> implements Codec<T>
{
    private final CodecRegistry registry;
    private final Class<T> clazz;
    private volatile Codec<T> wrapped;
    
    public LazyCodec(final CodecRegistry registry, final Class<T> clazz) {
        this.registry = registry;
        this.clazz = clazz;
    }
    
    @Override
    public void encode(final BsonWriter writer, final T value, final EncoderContext encoderContext) {
        this.getWrapped().encode(writer, value, encoderContext);
    }
    
    @Override
    public Class<T> getEncoderClass() {
        return this.clazz;
    }
    
    @Override
    public T decode(final BsonReader reader, final DecoderContext decoderContext) {
        return this.getWrapped().decode(reader, decoderContext);
    }
    
    private Codec<T> getWrapped() {
        if (this.wrapped == null) {
            this.wrapped = this.registry.get(this.clazz);
        }
        return this.wrapped;
    }
}
