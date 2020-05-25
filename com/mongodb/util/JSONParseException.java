package com.mongodb.util;

public class JSONParseException extends RuntimeException
{
    private static final long serialVersionUID = -4415279469780082174L;
    final String jsonString;
    final int pos;
    
    @Override
    public String getMessage() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(this.jsonString);
        sb.append("\n");
        for (int i = 0; i < this.pos; ++i) {
            sb.append(" ");
        }
        sb.append("^");
        return sb.toString();
    }
    
    public JSONParseException(final String jsonString, final int position) {
        this.jsonString = jsonString;
        this.pos = position;
    }
    
    public JSONParseException(final String jsonString, final int position, final Throwable cause) {
        super(cause);
        this.jsonString = jsonString;
        this.pos = position;
    }
}
