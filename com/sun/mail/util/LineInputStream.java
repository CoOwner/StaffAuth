package com.sun.mail.util;

import java.io.*;

public class LineInputStream extends FilterInputStream
{
    private char[] lineBuffer;
    
    public LineInputStream(final InputStream in) {
        super(in);
        this.lineBuffer = null;
    }
    
    public String readLine() throws IOException {
        InputStream in = this.in;
        char[] buf = this.lineBuffer;
        if (buf == null) {
            final char[] lineBuffer = new char[128];
            this.lineBuffer = lineBuffer;
            buf = lineBuffer;
        }
        int room = buf.length;
        int offset = 0;
        int c1;
        while ((c1 = in.read()) != -1) {
            if (c1 == 10) {
                break;
            }
            if (c1 == 13) {
                int c2 = in.read();
                if (c2 == 13) {
                    c2 = in.read();
                }
                if (c2 != 10) {
                    if (!(in instanceof PushbackInputStream)) {
                        final PushbackInputStream in2 = new PushbackInputStream(in);
                        this.in = in2;
                        in = in2;
                    }
                    ((PushbackInputStream)in).unread(c2);
                    break;
                }
                break;
            }
            else {
                if (--room < 0) {
                    buf = new char[offset + 128];
                    room = buf.length - offset - 1;
                    System.arraycopy(this.lineBuffer, 0, buf, 0, offset);
                    this.lineBuffer = buf;
                }
                buf[offset++] = (char)c1;
            }
        }
        if (c1 == -1 && offset == 0) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }
}
