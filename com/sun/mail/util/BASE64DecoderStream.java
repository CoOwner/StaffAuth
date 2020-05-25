package com.sun.mail.util;

import java.io.*;

public class BASE64DecoderStream extends FilterInputStream
{
    private byte[] buffer;
    private int bufsize;
    private int index;
    private byte[] input_buffer;
    private int input_pos;
    private int input_len;
    private boolean ignoreErrors;
    private static final char[] pem_array;
    private static final byte[] pem_convert_array;
    
    public BASE64DecoderStream(final InputStream in) {
        super(in);
        this.buffer = new byte[3];
        this.bufsize = 0;
        this.index = 0;
        this.input_buffer = new byte[8190];
        this.input_pos = 0;
        this.input_len = 0;
        this.ignoreErrors = false;
        try {
            final String s = System.getProperty("mail.mime.base64.ignoreerrors");
            this.ignoreErrors = (s != null && !s.equalsIgnoreCase("false"));
        }
        catch (SecurityException ex) {}
    }
    
    public BASE64DecoderStream(final InputStream in, final boolean ignoreErrors) {
        super(in);
        this.buffer = new byte[3];
        this.bufsize = 0;
        this.index = 0;
        this.input_buffer = new byte[8190];
        this.input_pos = 0;
        this.input_len = 0;
        this.ignoreErrors = false;
        this.ignoreErrors = ignoreErrors;
    }
    
    public int read() throws IOException {
        if (this.index >= this.bufsize) {
            this.bufsize = this.decode(this.buffer, 0, this.buffer.length);
            if (this.bufsize <= 0) {
                return -1;
            }
            this.index = 0;
        }
        return this.buffer[this.index++] & 0xFF;
    }
    
    public int read(final byte[] buf, int off, int len) throws IOException {
        final int off2 = off;
        while (this.index < this.bufsize && len > 0) {
            buf[off++] = this.buffer[this.index++];
            --len;
        }
        if (this.index >= this.bufsize) {
            final int n = 0;
            this.index = n;
            this.bufsize = n;
        }
        final int bsize = len / 3 * 3;
        if (bsize > 0) {
            final int size = this.decode(buf, off, bsize);
            off += size;
            len -= size;
            if (size != bsize) {
                if (off == off2) {
                    return -1;
                }
                return off - off2;
            }
        }
        while (len > 0) {
            final int c = this.read();
            if (c == -1) {
                break;
            }
            buf[off++] = (byte)c;
            --len;
        }
        if (off == off2) {
            return -1;
        }
        return off - off2;
    }
    
    public boolean markSupported() {
        return false;
    }
    
    public int available() throws IOException {
        return this.in.available() * 3 / 4 + (this.bufsize - this.index);
    }
    
    private int decode(final byte[] outbuf, int pos, int len) throws IOException {
        final int pos2 = pos;
        while (len >= 3) {
            int got;
            int val;
            int i;
            for (got = 0, val = 0; got < 4; ++got, val |= i) {
                i = this.getByte();
                if (i == -1 || i == -2) {
                    boolean atEOF;
                    if (i == -1) {
                        if (got == 0) {
                            return pos - pos2;
                        }
                        if (!this.ignoreErrors) {
                            throw new IOException("Error in encoded stream: needed 4 valid base64 characters but only got " + got + " before EOF" + this.recentChars());
                        }
                        atEOF = true;
                    }
                    else {
                        if (got < 2 && !this.ignoreErrors) {
                            throw new IOException("Error in encoded stream: needed at least 2 valid base64 characters, but only got " + got + " before padding character (=)" + this.recentChars());
                        }
                        if (got == 0) {
                            return pos - pos2;
                        }
                        atEOF = false;
                    }
                    int size = got - 1;
                    if (size == 0) {
                        size = 1;
                    }
                    ++got;
                    val <<= 6;
                    while (got < 4) {
                        if (!atEOF) {
                            i = this.getByte();
                            if (i == -1) {
                                if (!this.ignoreErrors) {
                                    throw new IOException("Error in encoded stream: hit EOF while looking for padding characters (=)" + this.recentChars());
                                }
                            }
                            else if (i != -2 && !this.ignoreErrors) {
                                throw new IOException("Error in encoded stream: found valid base64 character after a padding character (=)" + this.recentChars());
                            }
                        }
                        val <<= 6;
                        ++got;
                    }
                    val >>= 8;
                    if (size == 2) {
                        outbuf[pos + 1] = (byte)(val & 0xFF);
                    }
                    val >>= 8;
                    outbuf[pos] = (byte)(val & 0xFF);
                    len -= size;
                    pos += size;
                    return pos - pos2;
                }
                val <<= 6;
            }
            outbuf[pos + 2] = (byte)(val & 0xFF);
            val >>= 8;
            outbuf[pos + 1] = (byte)(val & 0xFF);
            val >>= 8;
            outbuf[pos] = (byte)(val & 0xFF);
            len -= 3;
            pos += 3;
        }
        return pos - pos2;
    }
    
    private int getByte() throws IOException {
        int c;
        do {
            if (this.input_pos >= this.input_len) {
                try {
                    this.input_len = this.in.read(this.input_buffer);
                }
                catch (EOFException ex) {
                    return -1;
                }
                if (this.input_len <= 0) {
                    return -1;
                }
                this.input_pos = 0;
            }
            c = (this.input_buffer[this.input_pos++] & 0xFF);
            if (c == 61) {
                return -2;
            }
            c = BASE64DecoderStream.pem_convert_array[c];
        } while (c == -1);
        return c;
    }
    
    private String recentChars() {
        String errstr = "";
        final int nc = (this.input_pos > 10) ? 10 : this.input_pos;
        if (nc > 0) {
            errstr = errstr + ", the " + nc + " most recent characters were: \"";
            for (int k = this.input_pos - nc; k < this.input_pos; ++k) {
                final char c = (char)(this.input_buffer[k] & 0xFF);
                switch (c) {
                    case '\r': {
                        errstr += "\\r";
                        break;
                    }
                    case '\n': {
                        errstr += "\\n";
                        break;
                    }
                    case '\t': {
                        errstr += "\\t";
                        break;
                    }
                    default: {
                        if (c >= ' ' && c < '\u007f') {
                            errstr += c;
                            break;
                        }
                        errstr = errstr + "\\" + (int)c;
                        break;
                    }
                }
            }
            errstr += "\"";
        }
        return errstr;
    }
    
    public static byte[] decode(final byte[] inbuf) {
        int size = inbuf.length / 4 * 3;
        if (size == 0) {
            return inbuf;
        }
        if (inbuf[inbuf.length - 1] == 61) {
            --size;
            if (inbuf[inbuf.length - 2] == 61) {
                --size;
            }
        }
        final byte[] outbuf = new byte[size];
        int inpos = 0;
        int outpos = 0;
        for (size = inbuf.length; size > 0; size -= 4) {
            int osize = 3;
            int val = BASE64DecoderStream.pem_convert_array[inbuf[inpos++] & 0xFF];
            val <<= 6;
            val |= BASE64DecoderStream.pem_convert_array[inbuf[inpos++] & 0xFF];
            val <<= 6;
            if (inbuf[inpos] != 61) {
                val |= BASE64DecoderStream.pem_convert_array[inbuf[inpos++] & 0xFF];
            }
            else {
                --osize;
            }
            val <<= 6;
            if (inbuf[inpos] != 61) {
                val |= BASE64DecoderStream.pem_convert_array[inbuf[inpos++] & 0xFF];
            }
            else {
                --osize;
            }
            if (osize > 2) {
                outbuf[outpos + 2] = (byte)(val & 0xFF);
            }
            val >>= 8;
            if (osize > 1) {
                outbuf[outpos + 1] = (byte)(val & 0xFF);
            }
            val >>= 8;
            outbuf[outpos] = (byte)(val & 0xFF);
            outpos += osize;
        }
        return outbuf;
    }
    
    static {
        pem_array = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };
        pem_convert_array = new byte[256];
        for (int i = 0; i < 255; ++i) {
            BASE64DecoderStream.pem_convert_array[i] = -1;
        }
        for (int i = 0; i < BASE64DecoderStream.pem_array.length; ++i) {
            BASE64DecoderStream.pem_convert_array[BASE64DecoderStream.pem_array[i]] = (byte)i;
        }
    }
}
