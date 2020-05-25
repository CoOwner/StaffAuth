package org.bson.codecs.configuration;

import org.bson.codecs.*;

class ChildCodecRegistry<T> implements CodecRegistry
{
    private final ChildCodecRegistry<?> parent;
    private final ProvidersCodecRegistry registry;
    private final Class<T> codecClass;
    
    ChildCodecRegistry(final ProvidersCodecRegistry registry, final Class<T> codecClass) {
        this.codecClass = codecClass;
        this.parent = null;
        this.registry = registry;
    }
    
    private ChildCodecRegistry(final ChildCodecRegistry<?> parent, final Class<T> codecClass) {
        this.parent = parent;
        this.codecClass = codecClass;
        this.registry = parent.registry;
    }
    
    public Class<T> getCodecClass() {
        return this.codecClass;
    }
    
    @Override
    public <U> Codec<U> get(final Class<U> clazz) {
        if (this.hasCycles(clazz)) {
            return new LazyCodec<U>(this.registry, clazz);
        }
        return this.registry.get(new ChildCodecRegistry(this, (Class<T>)clazz));
    }
    
    private <U> Boolean hasCycles(final Class<U> theClass) {
        for (ChildCodecRegistry current = this; current != null; current = current.parent) {
            if (current.codecClass.equals(theClass)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ChildCodecRegistry<?> that = (ChildCodecRegistry<?>)o;
        if (!this.codecClass.equals(that.codecClass)) {
            return false;
        }
        if (this.parent != null) {
            if (this.parent.equals(that.parent)) {
                return this.registry.equals(that.registry);
            }
        }
        else if (that.parent == null) {
            return this.registry.equals(that.registry);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.parent != null) ? this.parent.hashCode() : 0;
        result = 31 * result + this.registry.hashCode();
        result = 31 * result + this.codecClass.hashCode();
        return result;
    }
}
