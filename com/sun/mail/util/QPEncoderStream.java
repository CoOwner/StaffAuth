package com.sun.mail.util;

import java.io.*;

public class QPEncoderStream extends FilterOutputStream
{
    private int count;
    private int bytesPerLine;
    private boolean gotSpace;
    private boolean gotCR;
    private static final char[] hex;
    
    public QPEncoderStream(final OutputStream out, final int bytesPerLine) {
        super(out);
        this.count = 0;
        this.gotSpace = false;
        this.gotCR = false;
        this.bytesPerLine = bytesPerLine - 1;
    }
    
    public QPEncoderStream(final OutputStream out) {
        this(out, 76);
    }
    
    public void write(final byte[] b, final int off, final int len) throws IOException {
        for (int i = 0; i < len; ++i) {
            this.write(b[off + i]);
        }
    }
    
    public void write(final byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }
    
    public void write(int c) throws IOException {
        c &= 0xFF;
        if (this.gotSpace) {
            if (c == 13 || c == 10) {
                this.output(32, true);
            }
            else {
                this.output(32, false);
            }
            this.gotSpace = false;
        }
        if (c == 13) {
            this.gotCR = true;
            this.outputCRLF();
        }
        else {
            if (c == 10) {
                if (!this.gotCR) {
                    this.outputCRLF();
                }
            }
            else if (c == 32) {
                this.gotSpace = true;
            }
            else if (c < 32 || c >= 127 || c == 61) {
                this.output(c, true);
            }
            else {
                this.output(c, false);
            }
            this.gotCR = false;
        }
    }
    
    public void flush() throws IOException {
        this.out.flush();
    }
    
    public void close() throws IOException {
        this.out.close();
    }
    
    private void outputCRLF() throws IOException {
        this.out.write(13);
        this.out.write(10);
        this.count = 0;
    }
    
    protected void output(final int c, final boolean encode) throws IOException {
        if (encode) {
            if ((this.count += 3) > this.bytesPerLine) {
                this.out.write(61);
                this.out.write(13);
                this.out.write(10);
                this.count = 3;
            }
            this.out.write(61);
            this.out.write(QPEncoderStream.hex[c >> 4]);
            this.out.write(QPEncoderStream.hex[c & 0xF]);
        }
        else {
            if (++this.count > this.bytesPerLine) {
                this.out.write(61);
                this.out.write(13);
                this.out.write(10);
                this.count = 1;
            }
            this.out.write(c);
        }
    }
    
    static {
        hex = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    }
}
