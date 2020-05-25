package com.sun.mail.imap.protocol;

import com.sun.mail.iap.*;

public class UID implements Item
{
    public static final char[] name;
    public int msgno;
    public long uid;
    
    public UID(final FetchResponse r) throws ParsingException {
        this.msgno = r.getNumber();
        r.skipSpaces();
        this.uid = r.readLong();
    }
    
    static {
        name = new char[] { 'U', 'I', 'D' };
    }
}
