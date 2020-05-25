package javax.mail;

import java.util.*;
import javax.mail.search.*;
import java.io.*;

public abstract class Message implements Part
{
    protected int msgnum;
    protected boolean expunged;
    protected Folder folder;
    protected Session session;
    
    protected Message() {
        this.msgnum = 0;
        this.expunged = false;
        this.folder = null;
        this.session = null;
    }
    
    protected Message(final Folder folder, final int msgnum) {
        this.msgnum = 0;
        this.expunged = false;
        this.folder = null;
        this.session = null;
        this.folder = folder;
        this.msgnum = msgnum;
        this.session = folder.store.session;
    }
    
    protected Message(final Session session) {
        this.msgnum = 0;
        this.expunged = false;
        this.folder = null;
        this.session = null;
        this.session = session;
    }
    
    public abstract Address[] getFrom() throws MessagingException;
    
    public abstract void setFrom() throws MessagingException;
    
    public abstract void setFrom(final Address p0) throws MessagingException;
    
    public abstract void addFrom(final Address[] p0) throws MessagingException;
    
    public abstract Address[] getRecipients(final RecipientType p0) throws MessagingException;
    
    public Address[] getAllRecipients() throws MessagingException {
        final Address[] to = this.getRecipients(RecipientType.TO);
        final Address[] cc = this.getRecipients(RecipientType.CC);
        final Address[] bcc = this.getRecipients(RecipientType.BCC);
        if (cc == null && bcc == null) {
            return to;
        }
        final int numRecip = ((to != null) ? to.length : 0) + ((cc != null) ? cc.length : 0) + ((bcc != null) ? bcc.length : 0);
        final Address[] addresses = new Address[numRecip];
        int pos = 0;
        if (to != null) {
            System.arraycopy(to, 0, addresses, pos, to.length);
            pos += to.length;
        }
        if (cc != null) {
            System.arraycopy(cc, 0, addresses, pos, cc.length);
            pos += cc.length;
        }
        if (bcc != null) {
            System.arraycopy(bcc, 0, addresses, pos, bcc.length);
            pos += bcc.length;
        }
        return addresses;
    }
    
    public abstract void setRecipients(final RecipientType p0, final Address[] p1) throws MessagingException;
    
    public void setRecipient(final RecipientType type, final Address address) throws MessagingException {
        final Address[] a = { address };
        this.setRecipients(type, a);
    }
    
    public abstract void addRecipients(final RecipientType p0, final Address[] p1) throws MessagingException;
    
    public void addRecipient(final RecipientType type, final Address address) throws MessagingException {
        final Address[] a = { address };
        this.addRecipients(type, a);
    }
    
    public Address[] getReplyTo() throws MessagingException {
        return this.getFrom();
    }
    
    public void setReplyTo(final Address[] addresses) throws MessagingException {
        throw new MethodNotSupportedException("setReplyTo not supported");
    }
    
    public abstract String getSubject() throws MessagingException;
    
    public abstract void setSubject(final String p0) throws MessagingException;
    
    public abstract Date getSentDate() throws MessagingException;
    
    public abstract void setSentDate(final Date p0) throws MessagingException;
    
    public abstract Date getReceivedDate() throws MessagingException;
    
    public abstract Flags getFlags() throws MessagingException;
    
    public boolean isSet(final Flags.Flag flag) throws MessagingException {
        return this.getFlags().contains(flag);
    }
    
    public abstract void setFlags(final Flags p0, final boolean p1) throws MessagingException;
    
    public void setFlag(final Flags.Flag flag, final boolean set) throws MessagingException {
        final Flags f = new Flags(flag);
        this.setFlags(f, set);
    }
    
    public int getMessageNumber() {
        return this.msgnum;
    }
    
    protected void setMessageNumber(final int msgnum) {
        this.msgnum = msgnum;
    }
    
    public Folder getFolder() {
        return this.folder;
    }
    
    public boolean isExpunged() {
        return this.expunged;
    }
    
    protected void setExpunged(final boolean expunged) {
        this.expunged = expunged;
    }
    
    public abstract Message reply(final boolean p0) throws MessagingException;
    
    public abstract void saveChanges() throws MessagingException;
    
    public boolean match(final SearchTerm term) throws MessagingException {
        return term.match(this);
    }
    
    public static class RecipientType implements Serializable
    {
        public static final RecipientType TO;
        public static final RecipientType CC;
        public static final RecipientType BCC;
        protected String type;
        private static final long serialVersionUID = -7479791750606340008L;
        
        protected RecipientType(final String type) {
            this.type = type;
        }
        
        protected Object readResolve() throws ObjectStreamException {
            if (this.type.equals("To")) {
                return RecipientType.TO;
            }
            if (this.type.equals("Cc")) {
                return RecipientType.CC;
            }
            if (this.type.equals("Bcc")) {
                return RecipientType.BCC;
            }
            throw new InvalidObjectException("Attempt to resolve unknown RecipientType: " + this.type);
        }
        
        public String toString() {
            return this.type;
        }
        
        static {
            TO = new RecipientType("To");
            CC = new RecipientType("Cc");
            BCC = new RecipientType("Bcc");
        }
    }
}
