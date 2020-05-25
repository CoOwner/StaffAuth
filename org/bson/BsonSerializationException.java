package org.bson;

public class BsonSerializationException extends BSONException
{
    private static final long serialVersionUID = -5214580094005440780L;
    
    public BsonSerializationException(final String message) {
        super(message);
    }
}
