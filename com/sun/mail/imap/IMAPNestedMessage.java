package com.sun.mail.imap;

import com.sun.mail.imap.protocol.*;
import javax.mail.*;

public class IMAPNestedMessage extends IMAPMessage
{
    private IMAPMessage msg;
    
    IMAPNestedMessage(final IMAPMessage m, final BODYSTRUCTURE b, final ENVELOPE e, final String sid) {
        super(m._getSession());
        this.msg = m;
        this.bs = b;
        this.envelope = e;
        this.sectionId = sid;
    }
    
    protected IMAPProtocol getProtocol() throws FolderClosedException {
        return this.msg.getProtocol();
    }
    
    protected Object getMessageCacheLock() {
        return this.msg.getMessageCacheLock();
    }
    
    protected int getSequenceNumber() {
        return this.msg.getSequenceNumber();
    }
    
    protected void checkExpunged() throws MessageRemovedException {
        this.msg.checkExpunged();
    }
    
    public boolean isExpunged() {
        return this.msg.isExpunged();
    }
    
    protected int getFetchBlockSize() {
        return this.msg.getFetchBlockSize();
    }
    
    public int getSize() throws MessagingException {
        return this.bs.size;
    }
    
    public synchronized void setFlags(final Flags flag, final boolean set) throws MessagingException {
        throw new MethodNotSupportedException("Cannot set flags on this nested message");
    }
}
