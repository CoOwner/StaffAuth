package com.sun.mail.iap;

import java.io.*;

public class ByteArray
{
    private byte[] bytes;
    private int start;
    private int count;
    
    public ByteArray(final byte[] b, final int start, final int count) {
        this.bytes = b;
        this.start = start;
        this.count = count;
    }
    
    public byte[] getBytes() {
        return this.bytes;
    }
    
    public byte[] getNewBytes() {
        final byte[] b = new byte[this.count];
        System.arraycopy(this.bytes, this.start, b, 0, this.count);
        return b;
    }
    
    public int getStart() {
        return this.start;
    }
    
    public int getCount() {
        return this.count;
    }
    
    public ByteArrayInputStream toByteArrayInputStream() {
        return new ByteArrayInputStream(this.bytes, this.start, this.count);
    }
}
