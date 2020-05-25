package com.sun.mail.iap;

public class ParsingException extends ProtocolException
{
    private static final long serialVersionUID = 7756119840142724839L;
    
    public ParsingException() {
    }
    
    public ParsingException(final String s) {
        super(s);
    }
    
    public ParsingException(final Response r) {
        super(r);
    }
}
