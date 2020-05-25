package com.sun.mail.iap;

public class ProtocolException extends Exception
{
    protected transient Response response;
    private static final long serialVersionUID = -4360500807971797439L;
    
    public ProtocolException() {
        this.response = null;
    }
    
    public ProtocolException(final String s) {
        super(s);
        this.response = null;
    }
    
    public ProtocolException(final Response r) {
        super(r.toString());
        this.response = null;
        this.response = r;
    }
    
    public Response getResponse() {
        return this.response;
    }
}
