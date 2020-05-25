package javax.mail.internet;

import java.io.*;
import javax.mail.*;
import java.net.*;
import java.util.*;

public class InternetAddress extends Address implements Cloneable
{
    protected String address;
    protected String personal;
    protected String encodedPersonal;
    private static final long serialVersionUID = -7507595530758302903L;
    private static final String rfc822phrase;
    private static final String specialsNoDotNoAt = "()<>,;:\\\"[]";
    private static final String specialsNoDot = "()<>,;:\\\"[]@";
    
    public InternetAddress() {
    }
    
    public InternetAddress(final String address) throws AddressException {
        final InternetAddress[] a = parse(address, true);
        if (a.length != 1) {
            throw new AddressException("Illegal address", address);
        }
        this.address = a[0].address;
        this.personal = a[0].personal;
        this.encodedPersonal = a[0].encodedPersonal;
    }
    
    public InternetAddress(final String address, final boolean strict) throws AddressException {
        this(address);
        if (strict) {
            checkAddress(this.address, true, true);
        }
    }
    
    public InternetAddress(final String address, final String personal) throws UnsupportedEncodingException {
        this(address, personal, null);
    }
    
    public InternetAddress(final String address, final String personal, final String charset) throws UnsupportedEncodingException {
        this.address = address;
        this.setPersonal(personal, charset);
    }
    
    public Object clone() {
        InternetAddress a = null;
        try {
            a = (InternetAddress)super.clone();
        }
        catch (CloneNotSupportedException ex) {}
        return a;
    }
    
    public String getType() {
        return "rfc822";
    }
    
    public void setAddress(final String address) {
        this.address = address;
    }
    
    public void setPersonal(final String name, final String charset) throws UnsupportedEncodingException {
        this.personal = name;
        if (name != null) {
            this.encodedPersonal = MimeUtility.encodeWord(name, charset, null);
        }
        else {
            this.encodedPersonal = null;
        }
    }
    
    public void setPersonal(final String name) throws UnsupportedEncodingException {
        this.personal = name;
        if (name != null) {
            this.encodedPersonal = MimeUtility.encodeWord(name);
        }
        else {
            this.encodedPersonal = null;
        }
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public String getPersonal() {
        if (this.personal != null) {
            return this.personal;
        }
        if (this.encodedPersonal != null) {
            try {
                return this.personal = MimeUtility.decodeText(this.encodedPersonal);
            }
            catch (Exception ex) {
                return this.encodedPersonal;
            }
        }
        return null;
    }
    
    public String toString() {
        if (this.encodedPersonal == null && this.personal != null) {
            try {
                this.encodedPersonal = MimeUtility.encodeWord(this.personal);
            }
            catch (UnsupportedEncodingException ex) {}
        }
        if (this.encodedPersonal != null) {
            return quotePhrase(this.encodedPersonal) + " <" + this.address + ">";
        }
        if (this.isGroup() || this.isSimple()) {
            return this.address;
        }
        return "<" + this.address + ">";
    }
    
    public String toUnicodeString() {
        final String p = this.getPersonal();
        if (p != null) {
            return quotePhrase(p) + " <" + this.address + ">";
        }
        if (this.isGroup() || this.isSimple()) {
            return this.address;
        }
        return "<" + this.address + ">";
    }
    
    private static String quotePhrase(final String phrase) {
        final int len = phrase.length();
        boolean needQuoting = false;
        for (int i = 0; i < len; ++i) {
            final char c = phrase.charAt(i);
            if (c == '\"' || c == '\\') {
                final StringBuffer sb = new StringBuffer(len + 3);
                sb.append('\"');
                for (int j = 0; j < len; ++j) {
                    final char cc = phrase.charAt(j);
                    if (cc == '\"' || cc == '\\') {
                        sb.append('\\');
                    }
                    sb.append(cc);
                }
                sb.append('\"');
                return sb.toString();
            }
            if ((c < ' ' && c != '\r' && c != '\n' && c != '\t') || c >= '\u007f' || InternetAddress.rfc822phrase.indexOf(c) >= 0) {
                needQuoting = true;
            }
        }
        if (needQuoting) {
            final StringBuffer sb2 = new StringBuffer(len + 2);
            sb2.append('\"').append(phrase).append('\"');
            return sb2.toString();
        }
        return phrase;
    }
    
    private static String unquote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
            if (s.indexOf(92) >= 0) {
                final StringBuffer sb = new StringBuffer(s.length());
                for (int i = 0; i < s.length(); ++i) {
                    char c = s.charAt(i);
                    if (c == '\\' && i < s.length() - 1) {
                        c = s.charAt(++i);
                    }
                    sb.append(c);
                }
                s = sb.toString();
            }
        }
        return s;
    }
    
    public boolean equals(final Object a) {
        if (!(a instanceof InternetAddress)) {
            return false;
        }
        final String s = ((InternetAddress)a).getAddress();
        return s == this.address || (this.address != null && this.address.equalsIgnoreCase(s));
    }
    
    public int hashCode() {
        if (this.address == null) {
            return 0;
        }
        return this.address.toLowerCase().hashCode();
    }
    
    public static String toString(final Address[] addresses) {
        return toString(addresses, 0);
    }
    
    public static String toString(final Address[] addresses, int used) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < addresses.length; ++i) {
            if (i != 0) {
                sb.append(", ");
                used += 2;
            }
            final String s = addresses[i].toString();
            final int len = lengthOfFirstSegment(s);
            if (used + len > 76) {
                sb.append("\r\n\t");
                used = 8;
            }
            sb.append(s);
            used = lengthOfLastSegment(s, used);
        }
        return sb.toString();
    }
    
    private static int lengthOfFirstSegment(final String s) {
        final int pos;
        if ((pos = s.indexOf("\r\n")) != -1) {
            return pos;
        }
        return s.length();
    }
    
    private static int lengthOfLastSegment(final String s, final int used) {
        final int pos;
        if ((pos = s.lastIndexOf("\r\n")) != -1) {
            return s.length() - pos - 2;
        }
        return s.length() + used;
    }
    
    public static InternetAddress getLocalAddress(final Session session) {
        String user = null;
        String host = null;
        String address = null;
        try {
            if (session == null) {
                user = System.getProperty("user.name");
                host = InetAddress.getLocalHost().getHostName();
            }
            else {
                address = session.getProperty("mail.from");
                if (address == null) {
                    user = session.getProperty("mail.user");
                    if (user == null || user.length() == 0) {
                        user = session.getProperty("user.name");
                    }
                    if (user == null || user.length() == 0) {
                        user = System.getProperty("user.name");
                    }
                    host = session.getProperty("mail.host");
                    if (host == null || host.length() == 0) {
                        final InetAddress me = InetAddress.getLocalHost();
                        if (me != null) {
                            host = me.getHostName();
                        }
                    }
                }
            }
            if (address == null && user != null && user.length() != 0 && host != null && host.length() != 0) {
                address = user + "@" + host;
            }
            if (address != null) {
                return new InternetAddress(address);
            }
        }
        catch (SecurityException sex) {}
        catch (AddressException ex) {}
        catch (UnknownHostException ex2) {}
        return null;
    }
    
    public static InternetAddress[] parse(final String addresslist) throws AddressException {
        return parse(addresslist, true);
    }
    
    public static InternetAddress[] parse(final String addresslist, final boolean strict) throws AddressException {
        return parse(addresslist, strict, false);
    }
    
    public static InternetAddress[] parseHeader(final String addresslist, final boolean strict) throws AddressException {
        return parse(addresslist, strict, true);
    }
    
    private static InternetAddress[] parse(final String s, final boolean strict, final boolean parseHdr) throws AddressException {
        int start_personal = -1;
        int end_personal = -1;
        final int length = s.length();
        boolean in_group = false;
        boolean route_addr = false;
        boolean rfc822 = false;
        final Vector v = new Vector();
        int start;
        int end = start = -1;
        int index;
        for (index = 0; index < length; ++index) {
            char c = s.charAt(index);
            switch (c) {
                case '(': {
                    rfc822 = true;
                    if (start >= 0 && end == -1) {
                        end = index;
                    }
                    if (start_personal == -1) {
                        start_personal = index + 1;
                    }
                    ++index;
                    int nesting;
                    for (nesting = 1; index < length && nesting > 0; ++index) {
                        c = s.charAt(index);
                        switch (c) {
                            case '\\': {
                                ++index;
                                break;
                            }
                            case '(': {
                                ++nesting;
                                break;
                            }
                            case ')': {
                                --nesting;
                                break;
                            }
                        }
                    }
                    if (nesting > 0) {
                        throw new AddressException("Missing ')'", s, index);
                    }
                    --index;
                    if (end_personal == -1) {
                        end_personal = index;
                        break;
                    }
                    break;
                }
                case ')': {
                    throw new AddressException("Missing '('", s, index);
                }
                case '<': {
                    rfc822 = true;
                    if (route_addr) {
                        throw new AddressException("Extra route-addr", s, index);
                    }
                    if (!in_group) {
                        start_personal = start;
                        if (start_personal >= 0) {
                            end_personal = index;
                        }
                        start = index + 1;
                    }
                    boolean inquote = false;
                    ++index;
                Label_0467:
                    while (index < length) {
                        c = s.charAt(index);
                        switch (c) {
                            case '\\': {
                                ++index;
                                break;
                            }
                            case '\"': {
                                inquote = !inquote;
                                break;
                            }
                            case '>': {
                                if (inquote) {
                                    break;
                                }
                                break Label_0467;
                            }
                        }
                        ++index;
                    }
                    if (index < length) {
                        route_addr = true;
                        end = index;
                        break;
                    }
                    if (inquote) {
                        throw new AddressException("Missing '\"'", s, index);
                    }
                    throw new AddressException("Missing '>'", s, index);
                }
                case '>': {
                    throw new AddressException("Missing '<'", s, index);
                }
                case '\"': {
                    rfc822 = true;
                    if (start == -1) {
                        start = index;
                    }
                    ++index;
                Label_0599:
                    while (index < length) {
                        c = s.charAt(index);
                        switch (c) {
                            case '\\': {
                                ++index;
                                break;
                            }
                            case '\"': {
                                break Label_0599;
                            }
                        }
                        ++index;
                    }
                    if (index >= length) {
                        throw new AddressException("Missing '\"'", s, index);
                    }
                    break;
                }
                case '[': {
                    rfc822 = true;
                    ++index;
                Label_0683:
                    while (index < length) {
                        c = s.charAt(index);
                        switch (c) {
                            case '\\': {
                                ++index;
                                break;
                            }
                            case ']': {
                                break Label_0683;
                            }
                        }
                        ++index;
                    }
                    if (index >= length) {
                        throw new AddressException("Missing ']'", s, index);
                    }
                    break;
                }
                case ',': {
                    if (start == -1) {
                        route_addr = false;
                        rfc822 = false;
                        end = (start = -1);
                        break;
                    }
                    if (in_group) {
                        route_addr = false;
                        break;
                    }
                    if (end == -1) {
                        end = index;
                    }
                    final String addr = s.substring(start, end).trim();
                    if (rfc822 || strict || parseHdr) {
                        if (strict || !parseHdr) {
                            checkAddress(addr, route_addr, false);
                        }
                        final InternetAddress ma = new InternetAddress();
                        ma.setAddress(addr);
                        if (start_personal >= 0) {
                            ma.encodedPersonal = unquote(s.substring(start_personal, end_personal).trim());
                            end_personal = (start_personal = -1);
                        }
                        v.addElement(ma);
                    }
                    else {
                        final StringTokenizer st = new StringTokenizer(addr);
                        while (st.hasMoreTokens()) {
                            final String a = st.nextToken();
                            checkAddress(a, false, false);
                            final InternetAddress ma = new InternetAddress();
                            ma.setAddress(a);
                            v.addElement(ma);
                        }
                    }
                    route_addr = false;
                    rfc822 = false;
                    end = (start = -1);
                    break;
                }
                case ':': {
                    rfc822 = true;
                    if (in_group) {
                        throw new AddressException("Nested group", s, index);
                    }
                    in_group = true;
                    if (start == -1) {
                        start = index;
                        break;
                    }
                    break;
                }
                case ';': {
                    if (start == -1) {
                        start = index;
                    }
                    if (!in_group) {
                        throw new AddressException("Illegal semicolon, not in group", s, index);
                    }
                    in_group = false;
                    if (start == -1) {
                        start = index;
                    }
                    final InternetAddress ma = new InternetAddress();
                    end = index + 1;
                    ma.setAddress(s.substring(start, end).trim());
                    v.addElement(ma);
                    route_addr = false;
                    end = (start = -1);
                    break;
                }
                case '\t':
                case '\n':
                case '\r':
                case ' ': {
                    break;
                }
                default: {
                    if (start == -1) {
                        start = index;
                        break;
                    }
                    break;
                }
            }
        }
        if (start >= 0) {
            if (end == -1) {
                end = index;
            }
            final String addr2 = s.substring(start, end).trim();
            if (rfc822 || strict || parseHdr) {
                if (strict || !parseHdr) {
                    checkAddress(addr2, route_addr, false);
                }
                final InternetAddress ma = new InternetAddress();
                ma.setAddress(addr2);
                if (start_personal >= 0) {
                    ma.encodedPersonal = unquote(s.substring(start_personal, end_personal).trim());
                }
                v.addElement(ma);
            }
            else {
                final StringTokenizer st2 = new StringTokenizer(addr2);
                while (st2.hasMoreTokens()) {
                    final String a2 = st2.nextToken();
                    checkAddress(a2, false, false);
                    final InternetAddress ma = new InternetAddress();
                    ma.setAddress(a2);
                    v.addElement(ma);
                }
            }
        }
        final InternetAddress[] a3 = new InternetAddress[v.size()];
        v.copyInto(a3);
        return a3;
    }
    
    public void validate() throws AddressException {
        checkAddress(this.getAddress(), true, true);
    }
    
    private static void checkAddress(final String addr, final boolean routeAddr, final boolean validate) throws AddressException {
        int start = 0;
        if (addr.indexOf(34) >= 0) {
            return;
        }
        if (routeAddr) {
            int i;
            for (start = 0; (i = indexOfAny(addr, ",:", start)) >= 0; start = i + 1) {
                if (addr.charAt(start) != '@') {
                    throw new AddressException("Illegal route-addr", addr);
                }
                if (addr.charAt(i) == ':') {
                    start = i + 1;
                    break;
                }
            }
        }
        int i;
        String local;
        String domain;
        if ((i = addr.indexOf(64, start)) >= 0) {
            if (i == start) {
                throw new AddressException("Missing local name", addr);
            }
            if (i == addr.length() - 1) {
                throw new AddressException("Missing domain", addr);
            }
            local = addr.substring(start, i);
            domain = addr.substring(i + 1);
        }
        else {
            if (validate) {
                throw new AddressException("Missing final '@domain'", addr);
            }
            local = addr;
            domain = null;
        }
        if (indexOfAny(addr, " \t\n\r") >= 0) {
            throw new AddressException("Illegal whitespace in address", addr);
        }
        if (indexOfAny(local, "()<>,;:\\\"[]@") >= 0) {
            throw new AddressException("Illegal character in local name", addr);
        }
        if (domain != null && domain.indexOf(91) < 0 && indexOfAny(domain, "()<>,;:\\\"[]@") >= 0) {
            throw new AddressException("Illegal character in domain", addr);
        }
    }
    
    private boolean isSimple() {
        return this.address == null || indexOfAny(this.address, "()<>,;:\\\"[]") < 0;
    }
    
    public boolean isGroup() {
        return this.address != null && this.address.endsWith(";") && this.address.indexOf(58) > 0;
    }
    
    public InternetAddress[] getGroup(final boolean strict) throws AddressException {
        final Vector groups = null;
        final String addr = this.getAddress();
        if (!addr.endsWith(";")) {
            return null;
        }
        final int ix = addr.indexOf(58);
        if (ix < 0) {
            return null;
        }
        final String list = addr.substring(ix + 1, addr.length() - 1);
        return parseHeader(list, strict);
    }
    
    private static int indexOfAny(final String s, final String any) {
        return indexOfAny(s, any, 0);
    }
    
    private static int indexOfAny(final String s, final String any, final int start) {
        try {
            for (int len = s.length(), i = start; i < len; ++i) {
                if (any.indexOf(s.charAt(i)) >= 0) {
                    return i;
                }
            }
            return -1;
        }
        catch (StringIndexOutOfBoundsException e) {
            return -1;
        }
    }
    
    static {
        rfc822phrase = "()<>@,;:\\\"\t .[]".replace(' ', '\0').replace('\t', '\0');
    }
}
