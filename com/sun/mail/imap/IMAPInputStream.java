package com.sun.mail.imap;

import java.io.*;
import com.sun.mail.imap.protocol.*;
import com.sun.mail.iap.*;
import javax.mail.*;

public class IMAPInputStream extends InputStream
{
    private IMAPMessage msg;
    private String section;
    private int pos;
    private int blksize;
    private int max;
    private byte[] buf;
    private int bufcount;
    private int bufpos;
    private boolean peek;
    
    public IMAPInputStream(final IMAPMessage msg, final String section, final int max, final boolean peek) {
        this.msg = msg;
        this.section = section;
        this.max = max;
        this.peek = peek;
        this.pos = 0;
        this.blksize = msg.getFetchBlockSize();
    }
    
    private void fill() throws IOException {
        if (this.max != -1 && this.pos >= this.max) {
            if (this.pos == 0) {
                this.checkSeen();
            }
            return;
        }
        BODY b = null;
        synchronized (this.msg.getMessageCacheLock()) {
            if (this.msg.isExpunged()) {
                throw new IOException("No content for expunged message");
            }
            final int seqnum = this.msg.getSequenceNumber();
            int cnt = this.blksize;
            if (this.max != -1 && this.pos + this.blksize > this.max) {
                cnt = this.max - this.pos;
            }
            try {
                final IMAPProtocol p = this.msg.getProtocol();
                if (this.peek) {
                    b = p.peekBody(seqnum, this.section, this.pos, cnt);
                }
                else {
                    b = p.fetchBody(seqnum, this.section, this.pos, cnt);
                }
            }
            catch (ProtocolException pex) {
                throw new IOException(pex.getMessage());
            }
            catch (FolderClosedException fex) {
                throw new IOException(fex.getMessage());
            }
        }
        final ByteArray ba;
        if (b == null || (ba = b.getByteArray()) == null) {
            throw new IOException("No content");
        }
        if (this.pos == 0) {
            this.checkSeen();
        }
        this.buf = ba.getBytes();
        this.bufpos = ba.getStart();
        final int n = ba.getCount();
        this.bufcount = this.bufpos + n;
        this.pos += n;
    }
    
    public synchronized int read() throws IOException {
        if (this.bufpos >= this.bufcount) {
            this.fill();
            if (this.bufpos >= this.bufcount) {
                return -1;
            }
        }
        return this.buf[this.bufpos++] & 0xFF;
    }
    
    public synchronized int read(final byte[] b, final int off, final int len) throws IOException {
        int avail = this.bufcount - this.bufpos;
        if (avail <= 0) {
            this.fill();
            avail = this.bufcount - this.bufpos;
            if (avail <= 0) {
                return -1;
            }
        }
        final int cnt = (avail < len) ? avail : len;
        System.arraycopy(this.buf, this.bufpos, b, off, cnt);
        this.bufpos += cnt;
        return cnt;
    }
    
    public int read(final byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }
    
    public synchronized int available() throws IOException {
        return this.bufcount - this.bufpos;
    }
    
    private void checkSeen() {
        if (this.peek) {
            return;
        }
        try {
            final Folder f = this.msg.getFolder();
            if (f != null && f.getMode() != 1 && !this.msg.isSet(Flags.Flag.SEEN)) {
                this.msg.setFlag(Flags.Flag.SEEN, true);
            }
        }
        catch (MessagingException ex) {}
    }
}
