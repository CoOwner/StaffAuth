package org.bson;

public class BsonInvalidOperationException extends BSONException
{
    private static final long serialVersionUID = 7684248076818601418L;
    
    public BsonInvalidOperationException(final String message) {
        super(message);
    }
}
