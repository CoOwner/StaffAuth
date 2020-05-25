package org.bson.codecs;

import org.bson.codecs.configuration.*;
import org.bson.assertions.*;
import org.bson.*;
import java.util.*;

public class BsonTypeCodecMap
{
    private final Codec<?>[] codecs;
    
    public BsonTypeCodecMap(final BsonTypeClassMap bsonTypeClassMap, final CodecRegistry codecRegistry) {
        this.codecs = (Codec<?>[])new Codec[256];
        Assertions.notNull("bsonTypeClassMap", bsonTypeClassMap);
        Assertions.notNull("codecRegistry", codecRegistry);
        for (final BsonType cur : bsonTypeClassMap.keys()) {
            final Class<?> clazz = bsonTypeClassMap.get(cur);
            if (clazz != null) {
                this.codecs[cur.getValue()] = codecRegistry.get(clazz);
            }
        }
    }
    
    public Codec<?> get(final BsonType bsonType) {
        return this.codecs[bsonType.getValue()];
    }
}
