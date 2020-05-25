package org.bson.codecs;

import org.bson.*;
import org.bson.assertions.*;
import org.bson.codecs.configuration.*;

public class IterableCodecProvider implements CodecProvider
{
    private final BsonTypeClassMap bsonTypeClassMap;
    private final Transformer valueTransformer;
    
    public IterableCodecProvider() {
        this(new BsonTypeClassMap());
    }
    
    public IterableCodecProvider(final Transformer valueTransformer) {
        this(new BsonTypeClassMap(), valueTransformer);
    }
    
    public IterableCodecProvider(final BsonTypeClassMap bsonTypeClassMap) {
        this(bsonTypeClassMap, null);
    }
    
    public IterableCodecProvider(final BsonTypeClassMap bsonTypeClassMap, final Transformer valueTransformer) {
        this.bsonTypeClassMap = Assertions.notNull("bsonTypeClassMap", bsonTypeClassMap);
        this.valueTransformer = valueTransformer;
    }
    
    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        if (Iterable.class.isAssignableFrom(clazz)) {
            return (Codec<T>)new IterableCodec(registry, this.bsonTypeClassMap, this.valueTransformer);
        }
        return null;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final IterableCodecProvider that = (IterableCodecProvider)o;
        if (!this.bsonTypeClassMap.equals(that.bsonTypeClassMap)) {
            return false;
        }
        if (this.valueTransformer != null) {
            if (this.valueTransformer.equals(that.valueTransformer)) {
                return true;
            }
        }
        else if (that.valueTransformer == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = this.bsonTypeClassMap.hashCode();
        result = 31 * result + ((this.valueTransformer != null) ? this.valueTransformer.hashCode() : 0);
        return result;
    }
}
