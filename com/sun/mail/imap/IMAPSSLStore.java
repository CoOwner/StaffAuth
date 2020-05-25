package com.sun.mail.imap;

import javax.mail.*;

public class IMAPSSLStore extends IMAPStore
{
    public IMAPSSLStore(final Session session, final URLName url) {
        super(session, url, "imaps", 993, true);
    }
}
