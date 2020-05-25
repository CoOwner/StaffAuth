package com.sun.mail.pop3;

import javax.mail.*;

public class POP3SSLStore extends POP3Store
{
    public POP3SSLStore(final Session session, final URLName url) {
        super(session, url, "pop3s", 995, true);
    }
}
