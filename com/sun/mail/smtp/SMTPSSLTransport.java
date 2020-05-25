package com.sun.mail.smtp;

import javax.mail.*;

public class SMTPSSLTransport extends SMTPTransport
{
    public SMTPSSLTransport(final Session session, final URLName urlname) {
        super(session, urlname, "smtps", 465, true);
    }
}
