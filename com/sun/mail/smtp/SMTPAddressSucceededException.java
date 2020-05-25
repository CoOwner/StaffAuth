package com.sun.mail.smtp;

import javax.mail.*;
import javax.mail.internet.*;

public class SMTPAddressSucceededException extends MessagingException
{
    protected InternetAddress addr;
    protected String cmd;
    protected int rc;
    private static final long serialVersionUID = -1168335848623096749L;
    
    public SMTPAddressSucceededException(final InternetAddress addr, final String cmd, final int rc, final String err) {
        super(err);
        this.addr = addr;
        this.cmd = cmd;
        this.rc = rc;
    }
    
    public InternetAddress getAddress() {
        return this.addr;
    }
    
    public String getCommand() {
        return this.cmd;
    }
    
    public int getReturnCode() {
        return this.rc;
    }
}
