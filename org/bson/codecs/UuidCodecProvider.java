package org.bson.codecs;

import org.bson.*;
import org.bson.codecs.configuration.*;
import java.util.*;

public class UuidCodecProvider implements CodecProvider
{
    private UuidRepresentation uuidRepresentation;
    
    public UuidCodecProvider(final UuidRepresentation uuidRepresentation) {
        this.uuidRepresentation = uuidRepresentation;
    }
    
    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        if (clazz == UUID.class) {
            return (Codec<T>)new UuidCodec(this.uuidRepresentation);
        }
        return null;
    }
}
