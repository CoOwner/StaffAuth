package com.sun.mail.imap;

public class AppendUID
{
    public long uidvalidity;
    public long uid;
    
    public AppendUID(final long uidvalidity, final long uid) {
        this.uidvalidity = -1L;
        this.uid = -1L;
        this.uidvalidity = uidvalidity;
        this.uid = uid;
    }
}
