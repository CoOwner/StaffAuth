package org.bson.codecs;

import org.bson.assertions.*;
import org.bson.codecs.configuration.*;
import org.bson.types.*;
import org.bson.*;

public class DocumentCodecProvider implements CodecProvider
{
    private final BsonTypeClassMap bsonTypeClassMap;
    private final Transformer valueTransformer;
    
    public DocumentCodecProvider() {
        this(new BsonTypeClassMap());
    }
    
    public DocumentCodecProvider(final Transformer valueTransformer) {
        this(new BsonTypeClassMap(), valueTransformer);
    }
    
    public DocumentCodecProvider(final BsonTypeClassMap bsonTypeClassMap) {
        this(bsonTypeClassMap, null);
    }
    
    public DocumentCodecProvider(final BsonTypeClassMap bsonTypeClassMap, final Transformer valueTransformer) {
        this.bsonTypeClassMap = Assertions.notNull("bsonTypeClassMap", bsonTypeClassMap);
        this.valueTransformer = valueTransformer;
    }
    
    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        if (clazz == CodeWithScope.class) {
            return (Codec<T>)new CodeWithScopeCodec(registry.get(Document.class));
        }
        if (clazz == Document.class) {
            return (Codec<T>)new DocumentCodec(registry, this.bsonTypeClassMap, this.valueTransformer);
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
        final DocumentCodecProvider that = (DocumentCodecProvider)o;
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
