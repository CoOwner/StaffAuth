package com.sun.mail.imap.protocol;

import java.io.*;
import com.sun.mail.iap.*;
import java.util.*;

public class FetchResponse extends IMAPResponse
{
    private Item[] items;
    private static final char[] HEADER;
    private static final char[] TEXT;
    
    public FetchResponse(final Protocol p) throws IOException, ProtocolException {
        super(p);
        this.parse();
    }
    
    public FetchResponse(final IMAPResponse r) throws IOException, ProtocolException {
        super(r);
        this.parse();
    }
    
    public int getItemCount() {
        return this.items.length;
    }
    
    public Item getItem(final int index) {
        return this.items[index];
    }
    
    public Item getItem(final Class c) {
        for (int i = 0; i < this.items.length; ++i) {
            if (c.isInstance(this.items[i])) {
                return this.items[i];
            }
        }
        return null;
    }
    
    public static Item getItem(final Response[] r, final int msgno, final Class c) {
        if (r == null) {
            return null;
        }
        for (int i = 0; i < r.length; ++i) {
            if (r[i] != null && r[i] instanceof FetchResponse) {
                if (((FetchResponse)r[i]).getNumber() == msgno) {
                    final FetchResponse f = (FetchResponse)r[i];
                    for (int j = 0; j < f.items.length; ++j) {
                        if (c.isInstance(f.items[j])) {
                            return f.items[j];
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private void parse() throws ParsingException {
        this.skipSpaces();
        if (this.buffer[this.index] != 40) {
            throw new ParsingException("error in FETCH parsing, missing '(' at index " + this.index);
        }
        final Vector v = new Vector();
        Item i = null;
        do {
            ++this.index;
            if (this.index >= this.size) {
                throw new ParsingException("error in FETCH parsing, ran off end of buffer, size " + this.size);
            }
            switch (this.buffer[this.index]) {
                case 69: {
                    if (this.match(ENVELOPE.name)) {
                        this.index += ENVELOPE.name.length;
                        i = new ENVELOPE(this);
                        break;
                    }
                    break;
                }
                case 70: {
                    if (this.match(FLAGS.name)) {
                        this.index += FLAGS.name.length;
                        i = new FLAGS(this);
                        break;
                    }
                    break;
                }
                case 73: {
                    if (this.match(INTERNALDATE.name)) {
                        this.index += INTERNALDATE.name.length;
                        i = new INTERNALDATE(this);
                        break;
                    }
                    break;
                }
                case 66: {
                    if (!this.match(BODY.name)) {
                        break;
                    }
                    if (this.buffer[this.index + 4] == 91) {
                        this.index += BODY.name.length;
                        i = new BODY(this);
                        break;
                    }
                    if (this.match(BODYSTRUCTURE.name)) {
                        this.index += BODYSTRUCTURE.name.length;
                    }
                    else {
                        this.index += BODY.name.length;
                    }
                    i = new BODYSTRUCTURE(this);
                    break;
                }
                case 82: {
                    if (this.match(RFC822SIZE.name)) {
                        this.index += RFC822SIZE.name.length;
                        i = new RFC822SIZE(this);
                        break;
                    }
                    if (this.match(RFC822DATA.name)) {
                        this.index += RFC822DATA.name.length;
                        if (this.match(FetchResponse.HEADER)) {
                            this.index += FetchResponse.HEADER.length;
                        }
                        else if (this.match(FetchResponse.TEXT)) {
                            this.index += FetchResponse.TEXT.length;
                        }
                        i = new RFC822DATA(this);
                        break;
                    }
                    break;
                }
                case 85: {
                    if (this.match(UID.name)) {
                        this.index += UID.name.length;
                        i = new UID(this);
                        break;
                    }
                    break;
                }
            }
            if (i == null) {
                continue;
            }
            v.addElement(i);
        } while (this.buffer[this.index] != 41);
        ++this.index;
        v.copyInto(this.items = new Item[v.size()]);
    }
    
    private boolean match(final char[] itemName) {
        final int len = itemName.length;
        int i = 0;
        int j = this.index;
        while (i < len) {
            if (Character.toUpperCase((char)this.buffer[j++]) != itemName[i++]) {
                return false;
            }
        }
        return true;
    }
    
    static {
        HEADER = new char[] { '.', 'H', 'E', 'A', 'D', 'E', 'R' };
        TEXT = new char[] { '.', 'T', 'E', 'X', 'T' };
    }
}
