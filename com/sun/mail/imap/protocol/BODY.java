package com.sun.mail.imap.protocol;

import com.sun.mail.iap.*;
import java.io.*;

public class BODY implements Item
{
    public static char[] name;
    public int msgno;
    public ByteArray data;
    public String section;
    public int origin;
    
    public BODY(final FetchResponse r) throws ParsingException {
        this.origin = 0;
        this.msgno = r.getNumber();
        r.skipSpaces();
        int b;
        while ((b = r.readByte()) != 93) {
            if (b == 0) {
                throw new ParsingException("BODY parse error: missing ``]'' at section end");
            }
        }
        if (r.readByte() == 60) {
            this.origin = r.readNumber();
            r.skip(1);
        }
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
        BODY.name = new char[] { 'B', 'O', 'D', 'Y' };
    }
}
