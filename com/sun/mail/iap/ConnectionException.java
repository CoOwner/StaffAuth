package com.sun.mail.iap;

public class ConnectionException extends ProtocolException
{
    private transient Protocol p;
    private static final long serialVersionUID = 5749739604257464727L;
    
    public ConnectionException() {
    }
    
    public ConnectionException(final String s) {
        super(s);
    }
    
    public ConnectionException(final Protocol p, final Response r) {
        super(r);
        this.p = p;
    }
    
    public Protocol getProtocol() {
        return this.p;
    }
}
