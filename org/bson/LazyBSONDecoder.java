package org.bson;

import java.io.*;
import org.bson.io.*;
import java.util.*;

public class LazyBSONDecoder implements BSONDecoder
{
    private static final int BYTES_IN_INTEGER = 4;
    
    @Override
    public BSONObject readObject(final byte[] bytes) {
        final BSONCallback bsonCallback = new LazyBSONCallback();
        this.decode(bytes, bsonCallback);
        return (BSONObject)bsonCallback.get();
    }
    
    @Override
    public BSONObject readObject(final InputStream in) throws IOException {
        final BSONCallback bsonCallback = new LazyBSONCallback();
        this.decode(in, bsonCallback);
        return (BSONObject)bsonCallback.get();
    }
    
    @Override
    public int decode(final byte[] bytes, final BSONCallback callback) {
        try {
            return this.decode(new ByteArrayInputStream(bytes), callback);
        }
        catch (IOException e) {
            throw new BSONException("Invalid bytes received", e);
        }
    }
    
    @Override
    public int decode(final InputStream in, final BSONCallback callback) throws IOException {
        final byte[] documentSizeBuffer = new byte[4];
        final int documentSize = Bits.readInt(in, documentSizeBuffer);
        final byte[] documentBytes = Arrays.copyOf(documentSizeBuffer, documentSize);
        Bits.readFully(in, documentBytes, 4, documentSize - 4);
        callback.gotBinary(null, (byte)0, documentBytes);
        return documentSize;
    }
}
