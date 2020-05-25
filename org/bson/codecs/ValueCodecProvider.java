package org.bson.codecs;

import java.util.*;
import org.bson.codecs.configuration.*;

public class ValueCodecProvider implements CodecProvider
{
    private final Map<Class<?>, Codec<?>> codecs;
    
    public ValueCodecProvider() {
        this.codecs = new HashMap<Class<?>, Codec<?>>();
        this.addCodecs();
    }
    
    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        return (Codec<T>)this.codecs.get(clazz);
    }
    
    private void addCodecs() {
        this.addCodec((Codec<Object>)new BinaryCodec());
        this.addCodec((Codec<Object>)new BooleanCodec());
        this.addCodec((Codec<Object>)new DateCodec());
        this.addCodec((Codec<Object>)new DoubleCodec());
        this.addCodec((Codec<Object>)new IntegerCodec());
        this.addCodec((Codec<Object>)new LongCodec());
        this.addCodec((Codec<Object>)new MinKeyCodec());
        this.addCodec((Codec<Object>)new MaxKeyCodec());
        this.addCodec((Codec<Object>)new CodeCodec());
        this.addCodec((Codec<Object>)new Decimal128Codec());
        this.addCodec((Codec<Object>)new ObjectIdCodec());
        this.addCodec((Codec<Object>)new CharacterCodec());
        this.addCodec((Codec<Object>)new StringCodec());
        this.addCodec((Codec<Object>)new SymbolCodec());
        this.addCodec((Codec<Object>)new UuidCodec());
        this.addCodec((Codec<Object>)new ByteCodec());
        this.addCodec((Codec<Object>)new PatternCodec());
        this.addCodec((Codec<Object>)new ShortCodec());
        this.addCodec((Codec<Object>)new ByteArrayCodec());
        this.addCodec((Codec<Object>)new FloatCodec());
        this.addCodec((Codec<Object>)new AtomicBooleanCodec());
        this.addCodec((Codec<Object>)new AtomicIntegerCodec());
        this.addCodec((Codec<Object>)new AtomicLongCodec());
    }
    
    private <T> void addCodec(final Codec<T> codec) {
        this.codecs.put(codec.getEncoderClass(), codec);
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || (o != null && this.getClass() == o.getClass());
    }
    
    @Override
    public int hashCode() {
        return 0;
    }
}
