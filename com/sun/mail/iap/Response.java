package com.sun.mail.iap;

import com.sun.mail.util.*;
import java.util.*;
import java.io.*;

public class Response
{
    protected int index;
    protected int pindex;
    protected int size;
    protected byte[] buffer;
    protected int type;
    protected String tag;
    private static final int increment = 100;
    public static final int TAG_MASK = 3;
    public static final int CONTINUATION = 1;
    public static final int TAGGED = 2;
    public static final int UNTAGGED = 3;
    public static final int TYPE_MASK = 28;
    public static final int OK = 4;
    public static final int NO = 8;
    public static final int BAD = 12;
    public static final int BYE = 16;
    public static final int SYNTHETIC = 32;
    
    public Response(final String s) {
        this.buffer = null;
        this.type = 0;
        this.tag = null;
        this.buffer = ASCIIUtility.getBytes(s);
        this.size = this.buffer.length;
        this.parse();
    }
    
    public Response(final Protocol p) throws IOException, ProtocolException {
        this.buffer = null;
        this.type = 0;
        this.tag = null;
        final ByteArray response = p.getInputStream().readResponse();
        this.buffer = response.getBytes();
        this.size = response.getCount() - 2;
        this.parse();
    }
    
    public Response(final Response r) {
        this.buffer = null;
        this.type = 0;
        this.tag = null;
        this.index = r.index;
        this.size = r.size;
        this.buffer = r.buffer;
        this.type = r.type;
        this.tag = r.tag;
    }
    
    public static Response byeResponse(final Exception ex) {
        String err = "* BYE JavaMail Exception: " + ex.toString();
        err = err.replace('\r', ' ').replace('\n', ' ');
        final Response response;
        final Response r = response = new Response(err);
        response.type |= 0x20;
        return r;
    }
    
    private void parse() {
        this.index = 0;
        if (this.buffer[this.index] == 43) {
            this.type |= 0x1;
            ++this.index;
            return;
        }
        if (this.buffer[this.index] == 42) {
            this.type |= 0x3;
            ++this.index;
        }
        else {
            this.type |= 0x2;
            this.tag = this.readAtom();
        }
        final int mark = this.index;
        final String s = this.readAtom();
        if (s.equalsIgnoreCase("OK")) {
            this.type |= 0x4;
        }
        else if (s.equalsIgnoreCase("NO")) {
            this.type |= 0x8;
        }
        else if (s.equalsIgnoreCase("BAD")) {
            this.type |= 0xC;
        }
        else if (s.equalsIgnoreCase("BYE")) {
            this.type |= 0x10;
        }
        else {
            this.index = mark;
        }
        this.pindex = this.index;
    }
    
    public void skipSpaces() {
        while (this.index < this.size && this.buffer[this.index] == 32) {
            ++this.index;
        }
    }
    
    public void skipToken() {
        while (this.index < this.size && this.buffer[this.index] != 32) {
            ++this.index;
        }
    }
    
    public void skip(final int count) {
        this.index += count;
    }
    
    public byte peekByte() {
        if (this.index < this.size) {
            return this.buffer[this.index];
        }
        return 0;
    }
    
    public byte readByte() {
        if (this.index < this.size) {
            return this.buffer[this.index++];
        }
        return 0;
    }
    
    public String readAtom() {
        return this.readAtom('\0');
    }
    
    public String readAtom(final char delim) {
        this.skipSpaces();
        if (this.index >= this.size) {
            return null;
        }
        final int start = this.index;
        byte b;
        while (this.index < this.size && (b = this.buffer[this.index]) > 32 && b != 40 && b != 41 && b != 37 && b != 42 && b != 34 && b != 92 && b != 127 && (delim == '\0' || b != delim)) {
            ++this.index;
        }
        return ASCIIUtility.toString(this.buffer, start, this.index);
    }
    
    public String readString(final char delim) {
        this.skipSpaces();
        if (this.index >= this.size) {
            return null;
        }
        final int start = this.index;
        while (this.index < this.size && this.buffer[this.index] != delim) {
            ++this.index;
        }
        return ASCIIUtility.toString(this.buffer, start, this.index);
    }
    
    public String[] readStringList() {
        this.skipSpaces();
        if (this.buffer[this.index] != 40) {
            return null;
        }
        ++this.index;
        final Vector v = new Vector();
        do {
            v.addElement(this.readString());
        } while (this.buffer[this.index++] != 41);
        final int size = v.size();
        if (size > 0) {
            final String[] s = new String[size];
            v.copyInto(s);
            return s;
        }
        return null;
    }
    
    public int readNumber() {
        this.skipSpaces();
        final int start = this.index;
        while (this.index < this.size && Character.isDigit((char)this.buffer[this.index])) {
            ++this.index;
        }
        if (this.index > start) {
            try {
                return ASCIIUtility.parseInt(this.buffer, start, this.index);
            }
            catch (NumberFormatException ex) {}
        }
        return -1;
    }
    
    public long readLong() {
        this.skipSpaces();
        final int start = this.index;
        while (this.index < this.size && Character.isDigit((char)this.buffer[this.index])) {
            ++this.index;
        }
        if (this.index > start) {
            try {
                return ASCIIUtility.parseLong(this.buffer, start, this.index);
            }
            catch (NumberFormatException ex) {}
        }
        return -1L;
    }
    
    public String readString() {
        return (String)this.parseString(false, true);
    }
    
    public ByteArrayInputStream readBytes() {
        final ByteArray ba = this.readByteArray();
        if (ba != null) {
            return ba.toByteArrayInputStream();
        }
        return null;
    }
    
    public ByteArray readByteArray() {
        if (this.isContinuation()) {
            this.skipSpaces();
            return new ByteArray(this.buffer, this.index, this.size - this.index);
        }
        return (ByteArray)this.parseString(false, false);
    }
    
    public String readAtomString() {
        return (String)this.parseString(true, true);
    }
    
    private Object parseString(final boolean parseAtoms, final boolean returnString) {
        this.skipSpaces();
        byte b = this.buffer[this.index];
        if (b == 34) {
            ++this.index;
            final int start = this.index;
            int copyto = this.index;
            while ((b = this.buffer[this.index]) != 34) {
                if (b == 92) {
                    ++this.index;
                }
                if (this.index != copyto) {
                    this.buffer[copyto] = this.buffer[this.index];
                }
                ++copyto;
                ++this.index;
            }
            ++this.index;
            if (returnString) {
                return ASCIIUtility.toString(this.buffer, start, copyto);
            }
            return new ByteArray(this.buffer, start, copyto - start);
        }
        else if (b == 123) {
            int start = ++this.index;
            while (this.buffer[this.index] != 125) {
                ++this.index;
            }
            int count = 0;
            try {
                count = ASCIIUtility.parseInt(this.buffer, start, this.index);
            }
            catch (NumberFormatException nex) {
                return null;
            }
            start = this.index + 3;
            this.index = start + count;
            if (returnString) {
                return ASCIIUtility.toString(this.buffer, start, start + count);
            }
            return new ByteArray(this.buffer, start, count);
        }
        else if (parseAtoms) {
            final int start = this.index;
            final String s = this.readAtom();
            if (returnString) {
                return s;
            }
            return new ByteArray(this.buffer, start, this.index);
        }
        else {
            if (b == 78 || b == 110) {
                this.index += 3;
                return null;
            }
            return null;
        }
    }
    
    public int getType() {
        return this.type;
    }
    
    public boolean isContinuation() {
        return (this.type & 0x3) == 0x1;
    }
    
    public boolean isTagged() {
        return (this.type & 0x3) == 0x2;
    }
    
    public boolean isUnTagged() {
        return (this.type & 0x3) == 0x3;
    }
    
    public boolean isOK() {
        return (this.type & 0x1C) == 0x4;
    }
    
    public boolean isNO() {
        return (this.type & 0x1C) == 0x8;
    }
    
    public boolean isBAD() {
        return (this.type & 0x1C) == 0xC;
    }
    
    public boolean isBYE() {
        return (this.type & 0x1C) == 0x10;
    }
    
    public boolean isSynthetic() {
        return (this.type & 0x20) == 0x20;
    }
    
    public String getTag() {
        return this.tag;
    }
    
    public String getRest() {
        this.skipSpaces();
        return ASCIIUtility.toString(this.buffer, this.index, this.size);
    }
    
    public void reset() {
        this.index = this.pindex;
    }
    
    public String toString() {
        return ASCIIUtility.toString(this.buffer, 0, this.size);
    }
}
