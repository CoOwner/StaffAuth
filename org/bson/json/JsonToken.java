package org.bson.json;

import org.bson.types.*;
import org.bson.*;

class JsonToken
{
    private final Object value;
    private final JsonTokenType type;
    
    public JsonToken(final JsonTokenType type, final Object value) {
        this.value = value;
        this.type = type;
    }
    
    public Object getValue() {
        return this.value;
    }
    
    public <T> T getValue(final Class<T> clazz) {
        if (Long.class == clazz) {
            if (this.value instanceof Integer) {
                return clazz.cast((long)this.value);
            }
            if (this.value instanceof String) {
                return clazz.cast(Long.valueOf((String)this.value));
            }
        }
        else if (Decimal128.class == clazz) {
            if (this.value instanceof Integer) {
                return clazz.cast(new Decimal128((int)this.value));
            }
            if (this.value instanceof Long) {
                return clazz.cast(new Decimal128((long)this.value));
            }
            if (this.value instanceof Double) {
                return clazz.cast(new BsonDouble((double)this.value).decimal128Value());
            }
            if (this.value instanceof String) {
                return clazz.cast(Decimal128.parse((String)this.value));
            }
        }
        try {
            return clazz.cast(this.value);
        }
        catch (ClassCastException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public JsonTokenType getType() {
        return this.type;
    }
}
