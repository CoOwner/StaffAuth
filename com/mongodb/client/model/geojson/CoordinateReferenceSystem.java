package com.mongodb.client.model.geojson;

import com.mongodb.annotations.*;

@Immutable
public abstract class CoordinateReferenceSystem
{
    public abstract CoordinateReferenceSystemType getType();
}
