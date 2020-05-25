package com.sun.mail.smtp;

import javax.mail.internet.*;
import javax.mail.*;

public class SMTPSendFailedException extends SendFailedException
{
    protected InternetAddress addr;
    protected String cmd;
    protected int rc;
    private static final long serialVersionUID = 8049122628728932894L;
    
    public SMTPSendFailedException(final String cmd, final int rc, final String err, final Exception ex, final Address[] vs, final Address[] vus, final Address[] inv) {
        super(err, ex, vs, vus, inv);
        this.cmd = cmd;
        this.rc = rc;
    }
    
    public String getCommand() {
        return this.cmd;
    }
    
    public int getReturnCode() {
        return this.rc;
    }
}
