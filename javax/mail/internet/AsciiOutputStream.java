package javax.mail.internet;

import java.io.*;

class AsciiOutputStream extends OutputStream
{
    private boolean breakOnNonAscii;
    private int ascii;
    private int non_ascii;
    private int linelen;
    private boolean longLine;
    private boolean badEOL;
    private boolean checkEOL;
    private int lastb;
    private int ret;
    
    public AsciiOutputStream(final boolean breakOnNonAscii, final boolean encodeEolStrict) {
        this.ascii = 0;
        this.non_ascii = 0;
        this.linelen = 0;
        this.longLine = false;
        this.badEOL = false;
        this.checkEOL = false;
        this.lastb = 0;
        this.ret = 0;
        this.breakOnNonAscii = breakOnNonAscii;
        this.checkEOL = (encodeEolStrict && breakOnNonAscii);
    }
    
    public void write(final int b) throws IOException {
        this.check(b);
    }
    
    public void write(final byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }
    
    public void write(final byte[] b, final int off, int len) throws IOException {
        len += off;
        for (int i = off; i < len; ++i) {
            this.check(b[i]);
        }
    }
    
    private final void check(int b) throws IOException {
        b &= 0xFF;
        if (this.checkEOL && ((this.lastb == 13 && b != 10) || (this.lastb != 13 && b == 10))) {
            this.badEOL = true;
        }
        if (b == 13 || b == 10) {
            this.linelen = 0;
        }
        else {
            ++this.linelen;
            if (this.linelen > 998) {
                this.longLine = true;
            }
        }
        if (MimeUtility.nonascii(b)) {
            ++this.non_ascii;
            if (this.breakOnNonAscii) {
                this.ret = 3;
                throw new EOFException();
            }
        }
        else {
            ++this.ascii;
        }
        this.lastb = b;
    }
    
    public int getAscii() {
        if (this.ret != 0) {
            return this.ret;
        }
        if (this.badEOL) {
            return 3;
        }
        if (this.non_ascii == 0) {
            if (this.longLine) {
                return 2;
            }
            return 1;
        }
        else {
            if (this.ascii > this.non_ascii) {
                return 2;
            }
            return 3;
        }
    }
}
