package com.sun.mail.imap.protocol;

import java.io.*;

public class BASE64MailboxEncoder
{
    protected byte[] buffer;
    protected int bufsize;
    protected boolean started;
    protected Writer out;
    private static final char[] pem_array;
    
    public static String encode(final String original) {
        BASE64MailboxEncoder base64stream = null;
        final char[] origchars = original.toCharArray();
        final int length = origchars.length;
        boolean changedString = false;
        final CharArrayWriter writer = new CharArrayWriter(length);
        for (final char current : origchars) {
            if (current >= ' ' && current <= '~') {
                if (base64stream != null) {
                    base64stream.flush();
                }
                if (current == '&') {
                    changedString = true;
                    writer.write(38);
                    writer.write(45);
                }
                else {
                    writer.write(current);
                }
            }
            else {
                if (base64stream == null) {
                    base64stream = new BASE64MailboxEncoder(writer);
                    changedString = true;
                }
                base64stream.write(current);
            }
        }
        if (base64stream != null) {
            base64stream.flush();
        }
        if (changedString) {
            return writer.toString();
        }
        return original;
    }
    
    public BASE64MailboxEncoder(final Writer what) {
        this.buffer = new byte[4];
        this.bufsize = 0;
        this.started = false;
        this.out = null;
        this.out = what;
    }
    
    public void write(final int c) {
        try {
            if (!this.started) {
                this.started = true;
                this.out.write(38);
            }
            this.buffer[this.bufsize++] = (byte)(c >> 8);
            this.buffer[this.bufsize++] = (byte)(c & 0xFF);
            if (this.bufsize >= 3) {
                this.encode();
                this.bufsize -= 3;
            }
        }
        catch (IOException ex) {}
    }
    
    public void flush() {
        try {
            if (this.bufsize > 0) {
                this.encode();
                this.bufsize = 0;
            }
            if (this.started) {
                this.out.write(45);
                this.started = false;
            }
        }
        catch (IOException ex) {}
    }
    
    protected void encode() throws IOException {
        if (this.bufsize == 1) {
            final byte a = this.buffer[0];
            final byte b = 0;
            final byte c = 0;
            this.out.write(BASE64MailboxEncoder.pem_array[a >>> 2 & 0x3F]);
            this.out.write(BASE64MailboxEncoder.pem_array[(a << 4 & 0x30) + (b >>> 4 & 0xF)]);
        }
        else if (this.bufsize == 2) {
            final byte a = this.buffer[0];
            final byte b = this.buffer[1];
            final byte c = 0;
            this.out.write(BASE64MailboxEncoder.pem_array[a >>> 2 & 0x3F]);
            this.out.write(BASE64MailboxEncoder.pem_array[(a << 4 & 0x30) + (b >>> 4 & 0xF)]);
            this.out.write(BASE64MailboxEncoder.pem_array[(b << 2 & 0x3C) + (c >>> 6 & 0x3)]);
        }
        else {
            final byte a = this.buffer[0];
            final byte b = this.buffer[1];
            final byte c = this.buffer[2];
            this.out.write(BASE64MailboxEncoder.pem_array[a >>> 2 & 0x3F]);
            this.out.write(BASE64MailboxEncoder.pem_array[(a << 4 & 0x30) + (b >>> 4 & 0xF)]);
            this.out.write(BASE64MailboxEncoder.pem_array[(b << 2 & 0x3C) + (c >>> 6 & 0x3)]);
            this.out.write(BASE64MailboxEncoder.pem_array[c & 0x3F]);
            if (this.bufsize == 4) {
                this.buffer[0] = this.buffer[3];
            }
        }
    }
    
    static {
        pem_array = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', ',' };
    }
}
