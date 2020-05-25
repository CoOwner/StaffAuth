package com.sun.mail.imap.protocol;

import com.sun.mail.iap.*;

public class Status
{
    public String mbox;
    public int total;
    public int recent;
    public long uidnext;
    public long uidvalidity;
    public int unseen;
    public static String[] standardItems;
    
    public Status(final Response r) throws ParsingException {
        this.mbox = null;
        this.total = -1;
        this.recent = -1;
        this.uidnext = -1L;
        this.uidvalidity = -1L;
        this.unseen = -1;
        this.mbox = r.readAtomString();
        r.skipSpaces();
        if (r.readByte() != 40) {
            throw new ParsingException("parse error in STATUS");
        }
        do {
            final String attr = r.readAtom();
            if (attr.equalsIgnoreCase("MESSAGES")) {
                this.total = r.readNumber();
            }
            else if (attr.equalsIgnoreCase("RECENT")) {
                this.recent = r.readNumber();
            }
            else if (attr.equalsIgnoreCase("UIDNEXT")) {
                this.uidnext = r.readLong();
            }
            else if (attr.equalsIgnoreCase("UIDVALIDITY")) {
                this.uidvalidity = r.readLong();
            }
            else {
                if (!attr.equalsIgnoreCase("UNSEEN")) {
                    continue;
                }
                this.unseen = r.readNumber();
            }
        } while (r.readByte() != 41);
    }
    
    public static void add(final Status s1, final Status s2) {
        if (s2.total != -1) {
            s1.total = s2.total;
        }
        if (s2.recent != -1) {
            s1.recent = s2.recent;
        }
        if (s2.uidnext != -1L) {
            s1.uidnext = s2.uidnext;
        }
        if (s2.uidvalidity != -1L) {
            s1.uidvalidity = s2.uidvalidity;
        }
        if (s2.unseen != -1) {
            s1.unseen = s2.unseen;
        }
    }
    
    static {
        Status.standardItems = new String[] { "MESSAGES", "RECENT", "UNSEEN", "UIDNEXT", "UIDVALIDITY" };
    }
}
