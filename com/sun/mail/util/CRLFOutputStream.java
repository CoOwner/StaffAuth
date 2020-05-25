package com.sun.mail.util;

import java.io.*;

public class CRLFOutputStream extends FilterOutputStream
{
    protected int lastb;
    protected static byte[] newline;
    
    public CRLFOutputStream(final OutputStream os) {
        super(os);
        this.lastb = -1;
    }
    
    public void write(final int b) throws IOException {
        if (b == 13) {
            this.out.write(CRLFOutputStream.newline);
        }
        else if (b == 10) {
            if (this.lastb != 13) {
                this.out.write(CRLFOutputStream.newline);
            }
        }
        else {
            this.out.write(b);
        }
        this.lastb = b;
    }
    
    public void write(final byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }
    
    public void write(final byte[] b, final int off, int len) throws IOException {
        int start = off;
        len += off;
        for (int i = start; i < len; ++i) {
            if (b[i] == 13) {
                this.out.write(b, start, i - start);
                this.out.write(CRLFOutputStream.newline);
                start = i + 1;
            }
            else if (b[i] == 10) {
                if (this.lastb != 13) {
                    this.out.write(b, start, i - start);
                    this.out.write(CRLFOutputStream.newline);
                }
                start = i + 1;
            }
            this.lastb = b[i];
        }
        if (len - start > 0) {
            this.out.write(b, start, len - start);
        }
    }
    
    public void writeln() throws IOException {
        this.out.write(CRLFOutputStream.newline);
    }
    
    static {
        (CRLFOutputStream.newline = new byte[2])[0] = 13;
        CRLFOutputStream.newline[1] = 10;
    }
}
