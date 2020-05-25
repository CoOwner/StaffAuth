package com.mongodb;

import org.bson.*;
import org.bson.codecs.*;

class DBEncoderFactoryAdapter implements Encoder<DBObject>
{
    private final DBEncoderFactory encoderFactory;
    
    public DBEncoderFactoryAdapter(final DBEncoderFactory encoderFactory) {
        this.encoderFactory = encoderFactory;
    }
    
    @Override
    public void encode(final BsonWriter writer, final DBObject value, final EncoderContext encoderContext) {
        new DBEncoderAdapter(this.encoderFactory.create()).encode(writer, value, encoderContext);
    }
    
    @Override
    public Class<DBObject> getEncoderClass() {
        return DBObject.class;
    }
}
