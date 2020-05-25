package com.sun.mail.pop3;

import javax.mail.*;

public class DefaultFolder extends Folder
{
    DefaultFolder(final POP3Store store) {
        super(store);
    }
    
    public String getName() {
        return "";
    }
    
    public String getFullName() {
        return "";
    }
    
    public Folder getParent() {
        return null;
    }
    
    public boolean exists() {
        return true;
    }
    
    public Folder[] list(final String pattern) throws MessagingException {
        final Folder[] f = { this.getInbox() };
        return f;
    }
    
    public char getSeparator() {
        return '/';
    }
    
    public int getType() {
        return 2;
    }
    
    public boolean create(final int type) throws MessagingException {
        return false;
    }
    
    public boolean hasNewMessages() throws MessagingException {
        return false;
    }
    
    public Folder getFolder(final String name) throws MessagingException {
        if (!name.equalsIgnoreCase("INBOX")) {
            throw new MessagingException("only INBOX supported");
        }
        return this.getInbox();
    }
    
    protected Folder getInbox() throws MessagingException {
        return this.getStore().getFolder("INBOX");
    }
    
    public boolean delete(final boolean recurse) throws MessagingException {
        throw new MethodNotSupportedException("delete");
    }
    
    public boolean renameTo(final Folder f) throws MessagingException {
        throw new MethodNotSupportedException("renameTo");
    }
    
    public void open(final int mode) throws MessagingException {
        throw new MethodNotSupportedException("open");
    }
    
    public void close(final boolean expunge) throws MessagingException {
        throw new MethodNotSupportedException("close");
    }
    
    public boolean isOpen() {
        return false;
    }
    
    public Flags getPermanentFlags() {
        return new Flags();
    }
    
    public int getMessageCount() throws MessagingException {
        return 0;
    }
    
    public Message getMessage(final int msgno) throws MessagingException {
        throw new MethodNotSupportedException("getMessage");
    }
    
    public void appendMessages(final Message[] msgs) throws MessagingException {
        throw new MethodNotSupportedException("Append not supported");
    }
    
    public Message[] expunge() throws MessagingException {
        throw new MethodNotSupportedException("expunge");
    }
}
