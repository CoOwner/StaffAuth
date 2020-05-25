package org.bson.codecs.configuration;

import org.bson.assertions.*;
import org.bson.codecs.*;
import java.util.*;

final class ProvidersCodecRegistry implements CodecRegistry, CodecProvider
{
    private final List<CodecProvider> codecProviders;
    private final CodecCache codecCache;
    
    ProvidersCodecRegistry(final List<? extends CodecProvider> codecProviders) {
        this.codecCache = new CodecCache();
        Assertions.isTrueArgument("codecProviders must not be null or empty", codecProviders != null && codecProviders.size() > 0);
        this.codecProviders = new ArrayList<CodecProvider>(codecProviders);
    }
    
    @Override
    public <T> Codec<T> get(final Class<T> clazz) {
        return this.get(new ChildCodecRegistry(this, (Class<T>)clazz));
    }
    
    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        for (final CodecProvider provider : this.codecProviders) {
            final Codec<T> codec = provider.get(clazz, registry);
            if (codec != null) {
                return codec;
            }
        }
        return null;
    }
    
     <T> Codec<T> get(final ChildCodecRegistry context) {
        if (!this.codecCache.containsKey(context.getCodecClass())) {
            for (final CodecProvider provider : this.codecProviders) {
                final Codec<T> codec = provider.get(context.getCodecClass(), context);
                if (codec != null) {
                    this.codecCache.put(context.getCodecClass(), codec);
                    return codec;
                }
            }
            this.codecCache.put(context.getCodecClass(), null);
        }
        return this.codecCache.getOrThrow(context.getCodecClass());
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ProvidersCodecRegistry that = (ProvidersCodecRegistry)o;
        if (this.codecProviders.size() != that.codecProviders.size()) {
            return false;
        }
        for (int i = 0; i < this.codecProviders.size(); ++i) {
            if (this.codecProviders.get(i).getClass() != that.codecProviders.get(i).getClass()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return this.codecProviders.hashCode();
    }
}
