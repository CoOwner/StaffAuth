package com.mongodb.connection;

import java.nio.*;

interface AsyncWritableByteChannel
{
    void write(final ByteBuffer p0, final AsyncCompletionHandler<Void> p1);
}
