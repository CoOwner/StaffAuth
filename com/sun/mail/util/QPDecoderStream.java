package com.sun.mail.util;

import java.io.*;

public class QPDecoderStream extends FilterInputStream
{
    protected byte[] ba;
    protected int spaces;
    
    public QPDecoderStream(final InputStream in) {
        super(new PushbackInputStream(in, 2));
        this.ba = new byte[2];
        this.spaces = 0;
    }
    
    public int read() throws IOException {
        if (this.spaces > 0) {
            --this.spaces;
            return 32;
        }
        int c = this.in.read();
        if (c == 32) {
            while ((c = this.in.read()) == 32) {
                ++this.spaces;
            }
            if (c == 13 || c == 10 || c == -1) {
                this.spaces = 0;
            }
            else {
                ((PushbackInputStream)this.in).unread(c);
                c = 32;
            }
            return c;
        }
        if (c == 61) {
            final int a = this.in.read();
            if (a == 10) {
                return this.read();
            }
            if (a == 13) {
                final int b = this.in.read();
                if (b != 10) {
                    ((PushbackInputStream)this.in).unread(b);
                }
                return this.read();
            }
            if (a == -1) {
                return -1;
            }
            this.ba[0] = (byte)a;
            this.ba[1] = (byte)this.in.read();
            try {
                return ASCIIUtility.parseInt(this.ba, 0, 2, 16);
            }
            catch (NumberFormatException nex) {
                ((PushbackInputStream)this.in).unread(this.ba);
                return c;
            }
        }
        return c;
    }
    
    public int read(final byte[] buf, final int off, final int len) throws IOException {
        int i = 0;
        while (i < len) {
            final int c;
            if ((c = this.read()) == -1) {
                if (i == 0) {
                    i = -1;
                    break;
                }
                break;
            }
            else {
                buf[off + i] = (byte)c;
                ++i;
            }
        }
        return i;
    }
    
    public boolean markSupported() {
        return false;
    }
    
    public int available() throws IOException {
        return this.in.available();
    }
}
