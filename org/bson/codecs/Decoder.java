package org.bson.codecs;

import org.bson.*;

public interface Decoder<T>
{
    T decode(final BsonReader p0, final DecoderContext p1);
}
