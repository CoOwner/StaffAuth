package com.sun.mail.util;

import java.io.*;

public class UUEncoderStream extends FilterOutputStream
{
    private byte[] buffer;
    private int bufsize;
    private boolean wrotePrefix;
    protected String name;
    protected int mode;
    
    public UUEncoderStream(final OutputStream out) {
        this(out, "encoder.buf", 644);
    }
    
    public UUEncoderStream(final OutputStream out, final String name) {
        this(out, name, 644);
    }
    
    public UUEncoderStream(final OutputStream out, final String name, final int mode) {
        super(out);
        this.bufsize = 0;
        this.wrotePrefix = false;
        this.name = name;
        this.mode = mode;
        this.buffer = new byte[45];
    }
    
    public void setNameMode(final String name, final int mode) {
        this.name = name;
        this.mode = mode;
    }
    
    public void write(final byte[] b, final int off, final int len) throws IOException {
        for (int i = 0; i < len; ++i) {
            this.write(b[off + i]);
        }
    }
    
    public void write(final byte[] data) throws IOException {
        this.write(data, 0, data.length);
    }
    
    public void write(final int c) throws IOException {
        this.buffer[this.bufsize++] = (byte)c;
        if (this.bufsize == 45) {
            this.writePrefix();
            this.encode();
            this.bufsize = 0;
        }
    }
    
    public void flush() throws IOException {
        if (this.bufsize > 0) {
            this.writePrefix();
            this.encode();
        }
        this.writeSuffix();
        this.out.flush();
    }
    
    public void close() throws IOException {
        this.flush();
        this.out.close();
    }
    
    private void writePrefix() throws IOException {
        if (!this.wrotePrefix) {
            final PrintStream ps = new PrintStream(this.out);
            ps.println("begin " + this.mode + " " + this.name);
            ps.flush();
            this.wrotePrefix = true;
        }
    }
    
    private void writeSuffix() throws IOException {
        final PrintStream ps = new PrintStream(this.out);
        ps.println(" \nend");
        ps.flush();
    }
    
    private void encode() throws IOException {
        int i = 0;
        this.out.write((this.bufsize & 0x3F) + 32);
        while (i < this.bufsize) {
            final byte a = this.buffer[i++];
            byte b;
            byte c;
            if (i < this.bufsize) {
                b = this.buffer[i++];
                if (i < this.bufsize) {
                    c = this.buffer[i++];
                }
                else {
                    c = 1;
                }
            }
            else {
                b = 1;
                c = 1;
            }
            final int c2 = a >>> 2 & 0x3F;
            final int c3 = (a << 4 & 0x30) | (b >>> 4 & 0xF);
            final int c4 = (b << 2 & 0x3C) | (c >>> 6 & 0x3);
            final int c5 = c & 0x3F;
            this.out.write(c2 + 32);
            this.out.write(c3 + 32);
            this.out.write(c4 + 32);
            this.out.write(c5 + 32);
        }
        this.out.write(10);
    }
}
