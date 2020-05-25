package org.bson.codecs;

import org.bson.*;

public interface Encoder<T>
{
    void encode(final BsonWriter p0, final T p1, final EncoderContext p2);
    
    Class<T> getEncoderClass();
}
