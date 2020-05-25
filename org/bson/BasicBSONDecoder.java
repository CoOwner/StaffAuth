package org.bson;

import java.io.*;
import java.nio.*;
import org.bson.io.*;

public class BasicBSONDecoder implements BSONDecoder
{
    @Override
    public BSONObject readObject(final byte[] bytes) {
        final BSONCallback bsonCallback = new BasicBSONCallback();
        this.decode(bytes, bsonCallback);
        return (BSONObject)bsonCallback.get();
    }
    
    @Override
    public BSONObject readObject(final InputStream in) throws IOException {
        return this.readObject(this.readFully(in));
    }
    
    @Override
    public int decode(final byte[] bytes, final BSONCallback callback) {
        final BsonBinaryReader reader = new BsonBinaryReader(new ByteBufferBsonInput(new ByteBufNIO(ByteBuffer.wrap(bytes))));
        try {
            final BsonWriter writer = new BSONCallbackAdapter(new BsonWriterSettings(), callback);
            writer.pipe(reader);
            return reader.getBsonInput().getPosition();
        }
        finally {
            reader.close();
        }
    }
    
    @Override
    public int decode(final InputStream in, final BSONCallback callback) throws IOException {
        return this.decode(this.readFully(in), callback);
    }
    
    private byte[] readFully(final InputStream input) throws IOException {
        final byte[] sizeBytes = new byte[4];
        Bits.readFully(input, sizeBytes);
        final int size = Bits.readInt(sizeBytes);
        final byte[] buffer = new byte[size];
        System.arraycopy(sizeBytes, 0, buffer, 0, 4);
        Bits.readFully(input, buffer, 4, size - 4);
        return buffer;
    }
}
