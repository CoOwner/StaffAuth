package com.sun.mail.iap;

public class CommandFailedException extends ProtocolException
{
    private static final long serialVersionUID = 793932807880443631L;
    
    public CommandFailedException() {
    }
    
    public CommandFailedException(final String s) {
        super(s);
    }
    
    public CommandFailedException(final Response r) {
        super(r);
    }
}
