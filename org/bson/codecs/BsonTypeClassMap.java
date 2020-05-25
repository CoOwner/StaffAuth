package org.bson.codecs;

import java.util.*;
import org.bson.types.*;
import org.bson.*;

public class BsonTypeClassMap
{
    private final Map<BsonType, Class<?>> map;
    
    public BsonTypeClassMap(final Map<BsonType, Class<?>> replacementsForDefaults) {
        this.map = new HashMap<BsonType, Class<?>>();
        this.addDefaults();
        this.map.putAll(replacementsForDefaults);
    }
    
    public BsonTypeClassMap() {
        this(Collections.emptyMap());
    }
    
    Set<BsonType> keys() {
        return this.map.keySet();
    }
    
    public Class<?> get(final BsonType bsonType) {
        return this.map.get(bsonType);
    }
    
    private void addDefaults() {
        this.map.put(BsonType.BINARY, Binary.class);
        this.map.put(BsonType.BOOLEAN, Boolean.class);
        this.map.put(BsonType.DATE_TIME, Date.class);
        this.map.put(BsonType.DB_POINTER, BsonDbPointer.class);
        this.map.put(BsonType.DOCUMENT, Document.class);
        this.map.put(BsonType.DOUBLE, Double.class);
        this.map.put(BsonType.INT32, Integer.class);
        this.map.put(BsonType.INT64, Long.class);
        this.map.put(BsonType.DECIMAL128, Decimal128.class);
        this.map.put(BsonType.MAX_KEY, MaxKey.class);
        this.map.put(BsonType.MIN_KEY, MinKey.class);
        this.map.put(BsonType.JAVASCRIPT, Code.class);
        this.map.put(BsonType.JAVASCRIPT_WITH_SCOPE, CodeWithScope.class);
        this.map.put(BsonType.OBJECT_ID, ObjectId.class);
        this.map.put(BsonType.REGULAR_EXPRESSION, BsonRegularExpression.class);
        this.map.put(BsonType.STRING, String.class);
        this.map.put(BsonType.SYMBOL, Symbol.class);
        this.map.put(BsonType.TIMESTAMP, BsonTimestamp.class);
        this.map.put(BsonType.UNDEFINED, BsonUndefined.class);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BsonTypeClassMap that = (BsonTypeClassMap)o;
        return this.map.equals(that.map);
    }
    
    @Override
    public int hashCode() {
        return this.map.hashCode();
    }
}
