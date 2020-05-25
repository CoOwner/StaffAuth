package com.sun.mail.util;

import java.io.*;

public class UUDecoderStream extends FilterInputStream
{
    private String name;
    private int mode;
    private byte[] buffer;
    private int bufsize;
    private int index;
    private boolean gotPrefix;
    private boolean gotEnd;
    private LineInputStream lin;
    
    public UUDecoderStream(final InputStream in) {
        super(in);
        this.bufsize = 0;
        this.index = 0;
        this.gotPrefix = false;
        this.gotEnd = false;
        this.lin = new LineInputStream(in);
        this.buffer = new byte[45];
    }
    
    public int read() throws IOException {
        if (this.index >= this.bufsize) {
            this.readPrefix();
            if (!this.decode()) {
                return -1;
            }
            this.index = 0;
        }
        return this.buffer[this.index++] & 0xFF;
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
        return this.in.available() * 3 / 4 + (this.bufsize - this.index);
    }
    
    public String getName() throws IOException {
        this.readPrefix();
        return this.name;
    }
    
    public int getMode() throws IOException {
        this.readPrefix();
        return this.mode;
    }
    
    private void readPrefix() throws IOException {
        if (this.gotPrefix) {
            return;
        }
        String s;
        do {
            s = this.lin.readLine();
            if (s == null) {
                throw new IOException("UUDecoder error: No Begin");
            }
        } while (!s.regionMatches(true, 0, "begin", 0, 5));
        try {
            this.mode = Integer.parseInt(s.substring(6, 9));
        }
        catch (NumberFormatException ex) {
            throw new IOException("UUDecoder error: " + ex.toString());
        }
        this.name = s.substring(10);
        this.gotPrefix = true;
    }
    
    private boolean decode() throws IOException {
        if (this.gotEnd) {
            return false;
        }
        this.bufsize = 0;
        String line;
        do {
            line = this.lin.readLine();
            if (line == null) {
                throw new IOException("Missing End");
            }
            if (line.regionMatches(true, 0, "end", 0, 3)) {
                this.gotEnd = true;
                return false;
            }
        } while (line.length() == 0);
        int count = line.charAt(0);
        if (count < 32) {
            throw new IOException("Buffer format error");
        }
        count = (count - 32 & 0x3F);
        if (count == 0) {
            line = this.lin.readLine();
            if (line == null || !line.regionMatches(true, 0, "end", 0, 3)) {
                throw new IOException("Missing End");
            }
            this.gotEnd = true;
            return false;
        }
        else {
            final int need = (count * 8 + 5) / 6;
            if (line.length() < need + 1) {
                throw new IOException("Short buffer error");
            }
            int i = 1;
            while (this.bufsize < count) {
                byte a = (byte)(line.charAt(i++) - ' ' & 0x3F);
                byte b = (byte)(line.charAt(i++) - ' ' & 0x3F);
                this.buffer[this.bufsize++] = (byte)((a << 2 & 0xFC) | (b >>> 4 & 0x3));
                if (this.bufsize < count) {
                    a = b;
                    b = (byte)(line.charAt(i++) - ' ' & 0x3F);
                    this.buffer[this.bufsize++] = (byte)((a << 4 & 0xF0) | (b >>> 2 & 0xF));
                }
                if (this.bufsize < count) {
                    a = b;
                    b = (byte)(line.charAt(i++) - ' ' & 0x3F);
                    this.buffer[this.bufsize++] = (byte)((a << 6 & 0xC0) | (b & 0x3F));
                }
            }
            return true;
        }
    }
}
