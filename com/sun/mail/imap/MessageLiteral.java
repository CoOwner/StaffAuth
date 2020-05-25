package com.sun.mail.imap;

import com.sun.mail.iap.*;
import com.sun.mail.util.*;
import javax.mail.*;
import java.io.*;

class MessageLiteral implements Literal
{
    private Message msg;
    private int msgSize;
    private byte[] buf;
    
    public MessageLiteral(final Message msg, final int maxsize) throws MessagingException, IOException {
        this.msgSize = -1;
        this.msg = msg;
        final LengthCounter lc = new LengthCounter(maxsize);
        final OutputStream os = new CRLFOutputStream(lc);
        msg.writeTo(os);
        os.flush();
        this.msgSize = lc.getSize();
        this.buf = lc.getBytes();
    }
    
    public int size() {
        return this.msgSize;
    }
    
    public void writeTo(OutputStream os) throws IOException {
        try {
            if (this.buf != null) {
                os.write(this.buf, 0, this.msgSize);
            }
            else {
                os = new CRLFOutputStream(os);
                this.msg.writeTo(os);
            }
        }
        catch (MessagingException mex) {
            throw new IOException("MessagingException while appending message: " + mex);
        }
    }
}
