package org.bson.codecs.configuration;

import org.bson.codecs.*;

public interface CodecRegistry
{
     <T> Codec<T> get(final Class<T> p0);
}
