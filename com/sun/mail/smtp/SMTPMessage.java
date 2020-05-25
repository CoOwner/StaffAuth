package com.sun.mail.smtp;

import javax.mail.internet.*;
import java.io.*;
import javax.mail.*;

public class SMTPMessage extends MimeMessage
{
    public static final int NOTIFY_NEVER = -1;
    public static final int NOTIFY_SUCCESS = 1;
    public static final int NOTIFY_FAILURE = 2;
    public static final int NOTIFY_DELAY = 4;
    public static final int RETURN_FULL = 1;
    public static final int RETURN_HDRS = 2;
    private static final String[] returnOptionString;
    private String envelopeFrom;
    private int notifyOptions;
    private int returnOption;
    private boolean sendPartial;
    private boolean allow8bitMIME;
    private String submitter;
    private String extension;
    
    public SMTPMessage(final Session session) {
        super(session);
        this.notifyOptions = 0;
        this.returnOption = 0;
        this.sendPartial = false;
        this.allow8bitMIME = false;
        this.submitter = null;
        this.extension = null;
    }
    
    public SMTPMessage(final Session session, final InputStream is) throws MessagingException {
        super(session, is);
        this.notifyOptions = 0;
        this.returnOption = 0;
        this.sendPartial = false;
        this.allow8bitMIME = false;
        this.submitter = null;
        this.extension = null;
    }
    
    public SMTPMessage(final MimeMessage source) throws MessagingException {
        super(source);
        this.notifyOptions = 0;
        this.returnOption = 0;
        this.sendPartial = false;
        this.allow8bitMIME = false;
        this.submitter = null;
        this.extension = null;
    }
    
    public void setEnvelopeFrom(final String from) {
        this.envelopeFrom = from;
    }
    
    public String getEnvelopeFrom() {
        return this.envelopeFrom;
    }
    
    public void setNotifyOptions(final int options) {
        if (options < -1 || options >= 8) {
            throw new IllegalArgumentException("Bad return option");
        }
        this.notifyOptions = options;
    }
    
    public int getNotifyOptions() {
        return this.notifyOptions;
    }
    
    String getDSNNotify() {
        if (this.notifyOptions == 0) {
            return null;
        }
        if (this.notifyOptions == -1) {
            return "NEVER";
        }
        final StringBuffer sb = new StringBuffer();
        if ((this.notifyOptions & 0x1) != 0x0) {
            sb.append("SUCCESS");
        }
        if ((this.notifyOptions & 0x2) != 0x0) {
            if (sb.length() != 0) {
                sb.append(',');
            }
            sb.append("FAILURE");
        }
        if ((this.notifyOptions & 0x4) != 0x0) {
            if (sb.length() != 0) {
                sb.append(',');
            }
            sb.append("DELAY");
        }
        return sb.toString();
    }
    
    public void setReturnOption(final int option) {
        if (option < 0 || option > 2) {
            throw new IllegalArgumentException("Bad return option");
        }
        this.returnOption = option;
    }
    
    public int getReturnOption() {
        return this.returnOption;
    }
    
    String getDSNRet() {
        return SMTPMessage.returnOptionString[this.returnOption];
    }
    
    public void setAllow8bitMIME(final boolean allow) {
        this.allow8bitMIME = allow;
    }
    
    public boolean getAllow8bitMIME() {
        return this.allow8bitMIME;
    }
    
    public void setSendPartial(final boolean partial) {
        this.sendPartial = partial;
    }
    
    public boolean getSendPartial() {
        return this.sendPartial;
    }
    
    public String getSubmitter() {
        return this.submitter;
    }
    
    public void setSubmitter(final String submitter) {
        this.submitter = submitter;
    }
    
    public String getMailExtension() {
        return this.extension;
    }
    
    public void setMailExtension(final String extension) {
        this.extension = extension;
    }
    
    static {
        returnOptionString = new String[] { null, "FULL", "HDRS" };
    }
}
