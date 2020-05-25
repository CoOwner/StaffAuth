package org.bson.codecs.configuration;

import org.bson.codecs.*;
import java.util.*;

final class MapOfCodecsProvider implements CodecProvider
{
    private final Map<Class<?>, Codec<?>> codecsMap;
    
    public MapOfCodecsProvider(final List<? extends Codec<?>> codecsList) {
        this.codecsMap = new HashMap<Class<?>, Codec<?>>();
        for (final Codec<?> codec : codecsList) {
            this.codecsMap.put(codec.getEncoderClass(), codec);
        }
    }
    
    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        return (Codec<T>)this.codecsMap.get(clazz);
    }
}
