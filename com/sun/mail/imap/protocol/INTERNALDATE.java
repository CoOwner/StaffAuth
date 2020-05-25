package com.sun.mail.imap.protocol;

import javax.mail.internet.*;
import com.sun.mail.iap.*;
import java.text.*;
import java.util.*;

public class INTERNALDATE implements Item
{
    public static char[] name;
    public int msgno;
    protected Date date;
    private static MailDateFormat mailDateFormat;
    private static SimpleDateFormat df;
    
    public INTERNALDATE(final FetchResponse r) throws ParsingException {
        this.msgno = r.getNumber();
        r.skipSpaces();
        final String s = r.readString();
        try {
            this.date = INTERNALDATE.mailDateFormat.parse(s);
        }
        catch (ParseException pex) {
            throw new ParsingException("INTERNALDATE parse error");
        }
    }
    
    public Date getDate() {
        return this.date;
    }
    
    public static String format(final Date d) {
        final StringBuffer sb = new StringBuffer();
        synchronized (INTERNALDATE.df) {
            INTERNALDATE.df.format(d, sb, new FieldPosition(0));
        }
        int rawOffsetInMins = -d.getTimezoneOffset();
        if (rawOffsetInMins < 0) {
            sb.append('-');
            rawOffsetInMins = -rawOffsetInMins;
        }
        else {
            sb.append('+');
        }
        final int offsetInHrs = rawOffsetInMins / 60;
        final int offsetInMins = rawOffsetInMins % 60;
        sb.append(Character.forDigit(offsetInHrs / 10, 10));
        sb.append(Character.forDigit(offsetInHrs % 10, 10));
        sb.append(Character.forDigit(offsetInMins / 10, 10));
        sb.append(Character.forDigit(offsetInMins % 10, 10));
        return sb.toString();
    }
    
    static {
        INTERNALDATE.name = new char[] { 'I', 'N', 'T', 'E', 'R', 'N', 'A', 'L', 'D', 'A', 'T', 'E' };
        INTERNALDATE.mailDateFormat = new MailDateFormat();
        INTERNALDATE.df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss ", Locale.US);
    }
}
