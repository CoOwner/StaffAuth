package org.bson;

import org.bson.types.*;

public abstract class BsonNumber extends BsonValue
{
    public abstract int intValue();
    
    public abstract long longValue();
    
    public abstract double doubleValue();
    
    public abstract Decimal128 decimal128Value();
}
