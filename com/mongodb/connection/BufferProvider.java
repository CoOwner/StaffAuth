package com.mongodb.connection;

import org.bson.*;

public interface BufferProvider
{
    ByteBuf getBuffer(final int p0);
}
