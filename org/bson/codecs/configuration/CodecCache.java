package org.bson.codecs.configuration;

import org.bson.codecs.*;
import java.util.concurrent.*;

final class CodecCache
{
    private final ConcurrentMap<Class<?>, Optional<? extends Codec<?>>> codecCache;
    
    CodecCache() {
        this.codecCache = new ConcurrentHashMap<Class<?>, Optional<? extends Codec<?>>>();
    }
    
    public boolean containsKey(final Class<?> clazz) {
        return this.codecCache.containsKey(clazz);
    }
    
    public void put(final Class<?> clazz, final Codec<?> codec) {
        this.codecCache.put(clazz, Optional.of(codec));
    }
    
    public <T> Codec<T> getOrThrow(final Class<T> clazz) {
        if (this.codecCache.containsKey(clazz)) {
            final Optional<? extends Codec<?>> optionalCodec = this.codecCache.get(clazz);
            if (!optionalCodec.isEmpty()) {
                return (Codec<T>)optionalCodec.get();
            }
        }
        throw new CodecConfigurationException(String.format("Can't find a codec for %s.", clazz));
    }
}
