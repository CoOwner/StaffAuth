package com.sun.mail.imap.protocol;

import com.sun.mail.iap.*;
import java.util.*;
import javax.mail.internet.*;

class IMAPAddress extends InternetAddress
{
    private boolean group;
    private InternetAddress[] grouplist;
    private String groupname;
    private static final long serialVersionUID = -3835822029483122232L;
    
    IMAPAddress(final Response r) throws ParsingException {
        this.group = false;
        r.skipSpaces();
        if (r.readByte() != 40) {
            throw new ParsingException("ADDRESS parse error");
        }
        this.encodedPersonal = r.readString();
        r.readString();
        final String mb = r.readString();
        final String host = r.readString();
        if (r.readByte() != 41) {
            throw new ParsingException("ADDRESS parse error");
        }
        if (host == null) {
            this.group = true;
            this.groupname = mb;
            if (this.groupname == null) {
                return;
            }
            final StringBuffer sb = new StringBuffer();
            sb.append(this.groupname).append(':');
            final Vector v = new Vector();
            while (r.peekByte() != 41) {
                final IMAPAddress a = new IMAPAddress(r);
                if (a.isEndOfGroup()) {
                    break;
                }
                if (v.size() != 0) {
                    sb.append(',');
                }
                sb.append(a.toString());
                v.addElement(a);
            }
            sb.append(';');
            this.address = sb.toString();
            v.copyInto(this.grouplist = new IMAPAddress[v.size()]);
        }
        else if (mb == null || mb.length() == 0) {
            this.address = host;
        }
        else if (host == null || host.length() == 0) {
            this.address = mb;
        }
        else {
            this.address = mb + "@" + host;
        }
    }
    
    boolean isEndOfGroup() {
        return this.group && this.groupname == null;
    }
    
    public boolean isGroup() {
        return this.group;
    }
    
    public InternetAddress[] getGroup(final boolean strict) throws AddressException {
        if (this.grouplist == null) {
            return null;
        }
        return this.grouplist.clone();
    }
}
