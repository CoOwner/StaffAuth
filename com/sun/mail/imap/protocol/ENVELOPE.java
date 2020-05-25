package com.sun.mail.imap.protocol;

import javax.mail.internet.*;
import com.sun.mail.iap.*;
import java.util.*;

public class ENVELOPE implements Item
{
    public static char[] name;
    public int msgno;
    public Date date;
    public String subject;
    public InternetAddress[] from;
    public InternetAddress[] sender;
    public InternetAddress[] replyTo;
    public InternetAddress[] to;
    public InternetAddress[] cc;
    public InternetAddress[] bcc;
    public String inReplyTo;
    public String messageId;
    private static MailDateFormat mailDateFormat;
    
    public ENVELOPE(final FetchResponse r) throws ParsingException {
        this.date = null;
        this.msgno = r.getNumber();
        r.skipSpaces();
        if (r.readByte() != 40) {
            throw new ParsingException("ENVELOPE parse error");
        }
        final String s = r.readString();
        if (s != null) {
            try {
                this.date = ENVELOPE.mailDateFormat.parse(s);
            }
            catch (Exception ex) {}
        }
        this.subject = r.readString();
        this.from = this.parseAddressList(r);
        this.sender = this.parseAddressList(r);
        this.replyTo = this.parseAddressList(r);
        this.to = this.parseAddressList(r);
        this.cc = this.parseAddressList(r);
        this.bcc = this.parseAddressList(r);
        this.inReplyTo = r.readString();
        this.messageId = r.readString();
        if (r.readByte() != 41) {
            throw new ParsingException("ENVELOPE parse error");
        }
    }
    
    private InternetAddress[] parseAddressList(final Response r) throws ParsingException {
        r.skipSpaces();
        final byte b = r.readByte();
        if (b == 40) {
            final Vector v = new Vector();
            do {
                final IMAPAddress a = new IMAPAddress(r);
                if (!a.isEndOfGroup()) {
                    v.addElement(a);
                }
            } while (r.peekByte() != 41);
            r.skip(1);
            final InternetAddress[] a2 = new InternetAddress[v.size()];
            v.copyInto(a2);
            return a2;
        }
        if (b == 78 || b == 110) {
            r.skip(2);
            return null;
        }
        throw new ParsingException("ADDRESS parse error");
    }
    
    static {
        ENVELOPE.name = new char[] { 'E', 'N', 'V', 'E', 'L', 'O', 'P', 'E' };
        ENVELOPE.mailDateFormat = new MailDateFormat();
    }
}
