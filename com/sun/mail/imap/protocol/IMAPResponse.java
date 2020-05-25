package com.sun.mail.imap.protocol;

import java.io.*;
import com.sun.mail.iap.*;
import java.util.*;
import com.sun.mail.util.*;

public class IMAPResponse extends Response
{
    private String key;
    private int number;
    
    public IMAPResponse(final Protocol c) throws IOException, ProtocolException {
        super(c);
        if (this.isUnTagged() && !this.isOK() && !this.isNO() && !this.isBAD() && !this.isBYE()) {
            this.key = this.readAtom();
            try {
                this.number = Integer.parseInt(this.key);
                this.key = this.readAtom();
            }
            catch (NumberFormatException ex) {}
        }
    }
    
    public IMAPResponse(final IMAPResponse r) {
        super(r);
        this.key = r.key;
        this.number = r.number;
    }
    
    public String[] readSimpleList() {
        this.skipSpaces();
        if (this.buffer[this.index] != 40) {
            return null;
        }
        ++this.index;
        final Vector v = new Vector();
        int start = this.index;
        while (this.buffer[this.index] != 41) {
            if (this.buffer[this.index] == 32) {
                v.addElement(ASCIIUtility.toString(this.buffer, start, this.index));
                start = this.index + 1;
            }
            ++this.index;
        }
        if (this.index > start) {
            v.addElement(ASCIIUtility.toString(this.buffer, start, this.index));
        }
        ++this.index;
        final int size = v.size();
        if (size > 0) {
            final String[] s = new String[size];
            v.copyInto(s);
            return s;
        }
        return null;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public boolean keyEquals(final String k) {
        return this.key != null && this.key.equalsIgnoreCase(k);
    }
    
    public int getNumber() {
        return this.number;
    }
    
    public static IMAPResponse readResponse(final Protocol p) throws IOException, ProtocolException {
        IMAPResponse r = new IMAPResponse(p);
        if (r.keyEquals("FETCH")) {
            r = new FetchResponse(r);
        }
        return r;
    }
}
