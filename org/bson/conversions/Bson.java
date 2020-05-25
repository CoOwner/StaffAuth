package org.bson.conversions;

import org.bson.codecs.configuration.*;
import org.bson.*;

public interface Bson
{
     <TDocument> BsonDocument toBsonDocument(final Class<TDocument> p0, final CodecRegistry p1);
}
