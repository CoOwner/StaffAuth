package com.sun.mail.imap.protocol;

import com.sun.mail.iap.*;

public class RFC822SIZE implements Item
{
    public static char[] name;
    public int msgno;
    public int size;
    
    public RFC822SIZE(final FetchResponse r) throws ParsingException {
        this.msgno = r.getNumber();
        r.skipSpaces();
        this.size = r.readNumber();
    }
    
    static {
        RFC822SIZE.name = new char[] { 'R', 'F', 'C', '8', '2', '2', '.', 'S', 'I', 'Z', 'E' };
    }
}
