package org.bson.json;

public class JsonParseException extends RuntimeException
{
    private static final long serialVersionUID = -6722022620020198727L;
    
    public JsonParseException() {
    }
    
    public JsonParseException(final String s) {
        super(s);
    }
    
    public JsonParseException(final String pattern, final Object... args) {
        super(String.format(pattern, args));
    }
    
    public JsonParseException(final Throwable t) {
        super(t);
    }
}
