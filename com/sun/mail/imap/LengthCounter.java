package com.sun.mail.imap;

import java.io.*;

class LengthCounter extends OutputStream
{
    private int size;
    private byte[] buf;
    private int maxsize;
    
    public LengthCounter(final int maxsize) {
        this.size = 0;
        this.buf = new byte[8192];
        this.maxsize = maxsize;
    }
    
    public void write(final int b) {
        final int newsize = this.size + 1;
        if (this.buf != null) {
            if (newsize > this.maxsize && this.maxsize >= 0) {
                this.buf = null;
            }
            else if (newsize > this.buf.length) {
                final byte[] newbuf = new byte[Math.max(this.buf.length << 1, newsize)];
                System.arraycopy(this.buf, 0, newbuf, 0, this.size);
                (this.buf = newbuf)[this.size] = (byte)b;
            }
            else {
                this.buf[this.size] = (byte)b;
            }
        }
        this.size = newsize;
    }
    
    public void write(final byte[] b, final int off, final int len) {
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        final int newsize = this.size + len;
        if (this.buf != null) {
            if (newsize > this.maxsize && this.maxsize >= 0) {
                this.buf = null;
            }
            else if (newsize > this.buf.length) {
                final byte[] newbuf = new byte[Math.max(this.buf.length << 1, newsize)];
                System.arraycopy(this.buf, 0, newbuf, 0, this.size);
                System.arraycopy(b, off, this.buf = newbuf, this.size, len);
            }
            else {
                System.arraycopy(b, off, this.buf, this.size, len);
            }
        }
        this.size = newsize;
    }
    
    public void write(final byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }
    
    public int getSize() {
        return this.size;
    }
    
    public byte[] getBytes() {
        return this.buf;
    }
}
