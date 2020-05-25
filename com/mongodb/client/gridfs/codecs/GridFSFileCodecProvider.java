package com.mongodb.client.gridfs.codecs;

import org.bson.codecs.configuration.*;
import org.bson.codecs.*;
import com.mongodb.client.gridfs.model.*;

public final class GridFSFileCodecProvider implements CodecProvider
{
    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        if (clazz.equals(GridFSFile.class)) {
            return (Codec<T>)new GridFSFileCodec(registry);
        }
        return null;
    }
}
