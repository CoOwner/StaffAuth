package com.sun.mail.imap.protocol;

import com.sun.mail.iap.*;
import java.io.*;

public class RFC822DATA implements Item
{
    public static char[] name;
    public int msgno;
    public ByteArray data;
    
    public RFC822DATA(final FetchResponse r) throws ParsingException {
        this.msgno = r.getNumber();
        r.skipSpaces();
        this.data = r.readByteArray();
    }
    
    public ByteArray getByteArray() {
        return this.data;
    }
    
    public ByteArrayInputStream getByteArrayInputStream() {
        if (this.data != null) {
            return this.data.toByteArrayInputStream();
        }
        return null;
    }
    
    static {
        RFC822DATA.name = new char[] { 'R', 'F', 'C', '8', '2', '2' };
    }
}
