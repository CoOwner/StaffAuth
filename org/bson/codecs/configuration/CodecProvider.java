package org.bson.codecs.configuration;

import org.bson.codecs.*;

public interface CodecProvider
{
     <T> Codec<T> get(final Class<T> p0, final CodecRegistry p1);
}
