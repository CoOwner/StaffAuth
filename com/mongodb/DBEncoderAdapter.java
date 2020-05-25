package com.mongodb;

import com.mongodb.assertions.*;
import org.bson.codecs.*;
import java.nio.*;
import org.bson.io.*;
import org.bson.*;

class DBEncoderAdapter implements Encoder<DBObject>
{
    private final DBEncoder encoder;
    
    public DBEncoderAdapter(final DBEncoder encoder) {
        this.encoder = Assertions.notNull("encoder", encoder);
    }
    
    @Override
    public void encode(final BsonWriter writer, final DBObject document, final EncoderContext encoderContext) {
        final BasicOutputBuffer buffer = new BasicOutputBuffer();
        try {
            this.encoder.writeObject(buffer, document);
            final BsonBinaryReader reader = new BsonBinaryReader(new ByteBufferBsonInput(new ByteBufNIO(ByteBuffer.wrap(buffer.toByteArray()))));
            try {
                writer.pipe(reader);
            }
            finally {
                reader.close();
            }
        }
        finally {
            buffer.close();
        }
    }
    
    @Override
    public Class<DBObject> getEncoderClass() {
        return DBObject.class;
    }
}
