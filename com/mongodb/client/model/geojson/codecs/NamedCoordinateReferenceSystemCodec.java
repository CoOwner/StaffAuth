package com.mongodb.client.model.geojson.codecs;

import com.mongodb.client.model.geojson.*;
import org.bson.*;
import org.bson.codecs.*;

public class NamedCoordinateReferenceSystemCodec implements Codec<NamedCoordinateReferenceSystem>
{
    @Override
    public void encode(final BsonWriter writer, final NamedCoordinateReferenceSystem value, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString("type", value.getType().getTypeName());
        writer.writeStartDocument("properties");
        writer.writeString("name", value.getName());
        writer.writeEndDocument();
        writer.writeEndDocument();
    }
    
    @Override
    public Class<NamedCoordinateReferenceSystem> getEncoderClass() {
        return NamedCoordinateReferenceSystem.class;
    }
    
    @Override
    public NamedCoordinateReferenceSystem decode(final BsonReader reader, final DecoderContext decoderContext) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
