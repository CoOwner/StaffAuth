package com.sun.mail.util;

import java.io.*;

public class BASE64EncoderStream extends FilterOutputStream
{
    private byte[] buffer;
    private int bufsize;
    private int count;
    private int bytesPerLine;
    private int lineLimit;
    private boolean noCRLF;
    private static byte[] newline;
    private static final char[] pem_array;
    
    public BASE64EncoderStream(final OutputStream out, int bytesPerLine) {
        super(out);
        this.bufsize = 0;
        this.count = 0;
        this.noCRLF = false;
        this.buffer = new byte[3];
        if (bytesPerLine == Integer.MAX_VALUE || bytesPerLine < 4) {
            this.noCRLF = true;
            bytesPerLine = 76;
        }
        this.bytesPerLine = bytesPerLine / 4 * 4;
        this.lineLimit = bytesPerLine / 4 * 3;
    }
    
    public BASE64EncoderStream(final OutputStream out) {
        this(out, 76);
    }
    
    public void write(final byte[] b, int off, int len) throws IOException {
        while ((this.bufsize != 0 || this.count != 0) && len > 0) {
            this.write(b[off++]);
            --len;
        }
        byte[] outbuf;
        if (this.noCRLF) {
            outbuf = new byte[this.bytesPerLine];
        }
        else {
            outbuf = new byte[this.bytesPerLine + 2];
            outbuf[this.bytesPerLine] = 13;
            outbuf[this.bytesPerLine + 1] = 10;
        }
        int inx;
        for (inx = 0; inx + this.lineLimit < len; inx += this.lineLimit) {
            this.out.write(encode(b, off + inx, this.lineLimit, outbuf));
        }
        while (inx < len) {
            this.write(b[off + inx]);
            ++inx;
        }
    }
    
    public void write(final byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }
    
    public void write(final int c) throws IOException {
        this.buffer[this.bufsize++] = (byte)c;
        if (this.bufsize == 3) {
            this.encode();
            this.bufsize = 0;
        }
    }
    
    public void flush() throws IOException {
        if (this.bufsize > 0) {
            this.encode();
            this.bufsize = 0;
        }
        this.out.flush();
    }
    
    public void close() throws IOException {
        this.flush();
        this.out.close();
    }
    
    private void encode() throws IOException {
        if (this.count + 4 > this.bytesPerLine) {
            if (!this.noCRLF) {
                this.out.write(BASE64EncoderStream.newline);
            }
            this.count = 0;
        }
        this.out.write(encode(this.buffer, 0, this.bufsize, null));
        this.count += 4;
    }
    
    public static byte[] encode(final byte[] inbuf) {
        if (inbuf.length == 0) {
            return inbuf;
        }
        return encode(inbuf, 0, inbuf.length, null);
    }
    
    private static byte[] encode(final byte[] inbuf, final int off, int size, byte[] outbuf) {
        if (outbuf == null) {
            outbuf = new byte[(size + 2) / 3 * 4];
        }
        int inpos = off;
        int outpos;
        for (outpos = 0; size >= 3; size -= 3, outpos += 4) {
            int val = inbuf[inpos++] & 0xFF;
            val <<= 8;
            val |= (inbuf[inpos++] & 0xFF);
            val <<= 8;
            val |= (inbuf[inpos++] & 0xFF);
            outbuf[outpos + 3] = (byte)BASE64EncoderStream.pem_array[val & 0x3F];
            val >>= 6;
            outbuf[outpos + 2] = (byte)BASE64EncoderStream.pem_array[val & 0x3F];
            val >>= 6;
            outbuf[outpos + 1] = (byte)BASE64EncoderStream.pem_array[val & 0x3F];
            val >>= 6;
            outbuf[outpos + 0] = (byte)BASE64EncoderStream.pem_array[val & 0x3F];
        }
        if (size == 1) {
            int val = inbuf[inpos++] & 0xFF;
            val <<= 4;
            outbuf[outpos + 2] = (outbuf[outpos + 3] = 61);
            outbuf[outpos + 1] = (byte)BASE64EncoderStream.pem_array[val & 0x3F];
            val >>= 6;
            outbuf[outpos + 0] = (byte)BASE64EncoderStream.pem_array[val & 0x3F];
        }
        else if (size == 2) {
            int val = inbuf[inpos++] & 0xFF;
            val <<= 8;
            val |= (inbuf[inpos++] & 0xFF);
            val <<= 2;
            outbuf[outpos + 3] = 61;
            outbuf[outpos + 2] = (byte)BASE64EncoderStream.pem_array[val & 0x3F];
            val >>= 6;
            outbuf[outpos + 1] = (byte)BASE64EncoderStream.pem_array[val & 0x3F];
            val >>= 6;
            outbuf[outpos + 0] = (byte)BASE64EncoderStream.pem_array[val & 0x3F];
        }
        return outbuf;
    }
    
    static {
        BASE64EncoderStream.newline = new byte[] { 13, 10 };
        pem_array = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };
    }
}
