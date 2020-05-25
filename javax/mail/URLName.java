package javax.mail;

import java.util.*;
import java.net.*;
import java.io.*;

public class URLName
{
    protected String fullURL;
    private String protocol;
    private String username;
    private String password;
    private String host;
    private InetAddress hostAddress;
    private boolean hostAddressKnown;
    private int port;
    private String file;
    private String ref;
    private int hashCode;
    private static boolean doEncode;
    static BitSet dontNeedEncoding;
    static final int caseDiff = 32;
    
    public URLName(final String protocol, final String host, final int port, final String file, final String username, final String password) {
        this.hostAddressKnown = false;
        this.port = -1;
        this.hashCode = 0;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        final int refStart;
        if (file != null && (refStart = file.indexOf(35)) != -1) {
            this.file = file.substring(0, refStart);
            this.ref = file.substring(refStart + 1);
        }
        else {
            this.file = file;
            this.ref = null;
        }
        this.username = (URLName.doEncode ? encode(username) : username);
        this.password = (URLName.doEncode ? encode(password) : password);
    }
    
    public URLName(final URL url) {
        this(url.toString());
    }
    
    public URLName(final String url) {
        this.hostAddressKnown = false;
        this.port = -1;
        this.hashCode = 0;
        this.parseString(url);
    }
    
    public String toString() {
        if (this.fullURL == null) {
            final StringBuffer tempURL = new StringBuffer();
            if (this.protocol != null) {
                tempURL.append(this.protocol);
                tempURL.append(":");
            }
            if (this.username != null || this.host != null) {
                tempURL.append("//");
                if (this.username != null) {
                    tempURL.append(this.username);
                    if (this.password != null) {
                        tempURL.append(":");
                        tempURL.append(this.password);
                    }
                    tempURL.append("@");
                }
                if (this.host != null) {
                    tempURL.append(this.host);
                }
                if (this.port != -1) {
                    tempURL.append(":");
                    tempURL.append(Integer.toString(this.port));
                }
                if (this.file != null) {
                    tempURL.append("/");
                }
            }
            if (this.file != null) {
                tempURL.append(this.file);
            }
            if (this.ref != null) {
                tempURL.append("#");
                tempURL.append(this.ref);
            }
            this.fullURL = tempURL.toString();
        }
        return this.fullURL;
    }
    
    protected void parseString(final String url) {
        final String s = null;
        this.password = s;
        this.username = s;
        this.host = s;
        this.ref = s;
        this.file = s;
        this.protocol = s;
        this.port = -1;
        final int len = url.length();
        final int protocolEnd = url.indexOf(58);
        if (protocolEnd != -1) {
            this.protocol = url.substring(0, protocolEnd);
        }
        if (url.regionMatches(protocolEnd + 1, "//", 0, 2)) {
            String fullhost = null;
            final int fileStart = url.indexOf(47, protocolEnd + 3);
            if (fileStart != -1) {
                fullhost = url.substring(protocolEnd + 3, fileStart);
                if (fileStart + 1 < len) {
                    this.file = url.substring(fileStart + 1);
                }
                else {
                    this.file = "";
                }
            }
            else {
                fullhost = url.substring(protocolEnd + 3);
            }
            final int i = fullhost.indexOf(64);
            if (i != -1) {
                final String fulluserpass = fullhost.substring(0, i);
                fullhost = fullhost.substring(i + 1);
                final int passindex = fulluserpass.indexOf(58);
                if (passindex != -1) {
                    this.username = fulluserpass.substring(0, passindex);
                    this.password = fulluserpass.substring(passindex + 1);
                }
                else {
                    this.username = fulluserpass;
                }
            }
            int portindex;
            if (fullhost.length() > 0 && fullhost.charAt(0) == '[') {
                portindex = fullhost.indexOf(58, fullhost.indexOf(93));
            }
            else {
                portindex = fullhost.indexOf(58);
            }
            if (portindex != -1) {
                final String portstring = fullhost.substring(portindex + 1);
                if (portstring.length() > 0) {
                    try {
                        this.port = Integer.parseInt(portstring);
                    }
                    catch (NumberFormatException nfex) {
                        this.port = -1;
                    }
                }
                this.host = fullhost.substring(0, portindex);
            }
            else {
                this.host = fullhost;
            }
        }
        else if (protocolEnd + 1 < len) {
            this.file = url.substring(protocolEnd + 1);
        }
        final int refStart;
        if (this.file != null && (refStart = this.file.indexOf(35)) != -1) {
            this.ref = this.file.substring(refStart + 1);
            this.file = this.file.substring(0, refStart);
        }
    }
    
    public int getPort() {
        return this.port;
    }
    
    public String getProtocol() {
        return this.protocol;
    }
    
    public String getFile() {
        return this.file;
    }
    
    public String getRef() {
        return this.ref;
    }
    
    public String getHost() {
        return this.host;
    }
    
    public String getUsername() {
        return URLName.doEncode ? decode(this.username) : this.username;
    }
    
    public String getPassword() {
        return URLName.doEncode ? decode(this.password) : this.password;
    }
    
    public URL getURL() throws MalformedURLException {
        return new URL(this.getProtocol(), this.getHost(), this.getPort(), this.getFile());
    }
    
    public boolean equals(final Object obj) {
        if (!(obj instanceof URLName)) {
            return false;
        }
        final URLName u2 = (URLName)obj;
        if (u2.protocol == null || !u2.protocol.equals(this.protocol)) {
            return false;
        }
        final InetAddress a1 = this.getHostAddress();
        final InetAddress a2 = u2.getHostAddress();
        if (a1 != null && a2 != null) {
            if (!a1.equals(a2)) {
                return false;
            }
        }
        else if (this.host != null && u2.host != null) {
            if (!this.host.equalsIgnoreCase(u2.host)) {
                return false;
            }
        }
        else if (this.host != u2.host) {
            return false;
        }
        if (this.username != u2.username && (this.username == null || !this.username.equals(u2.username))) {
            return false;
        }
        final String f1 = (this.file == null) ? "" : this.file;
        final String f2 = (u2.file == null) ? "" : u2.file;
        return f1.equals(f2) && this.port == u2.port;
    }
    
    public int hashCode() {
        if (this.hashCode != 0) {
            return this.hashCode;
        }
        if (this.protocol != null) {
            this.hashCode += this.protocol.hashCode();
        }
        final InetAddress addr = this.getHostAddress();
        if (addr != null) {
            this.hashCode += addr.hashCode();
        }
        else if (this.host != null) {
            this.hashCode += this.host.toLowerCase().hashCode();
        }
        if (this.username != null) {
            this.hashCode += this.username.hashCode();
        }
        if (this.file != null) {
            this.hashCode += this.file.hashCode();
        }
        return this.hashCode += this.port;
    }
    
    private synchronized InetAddress getHostAddress() {
        if (this.hostAddressKnown) {
            return this.hostAddress;
        }
        if (this.host == null) {
            return null;
        }
        try {
            this.hostAddress = InetAddress.getByName(this.host);
        }
        catch (UnknownHostException ex) {
            this.hostAddress = null;
        }
        this.hostAddressKnown = true;
        return this.hostAddress;
    }
    
    static String encode(final String s) {
        if (s == null) {
            return null;
        }
        for (int i = 0; i < s.length(); ++i) {
            final int c = s.charAt(i);
            if (c == 32 || !URLName.dontNeedEncoding.get(c)) {
                return _encode(s);
            }
        }
        return s;
    }
    
    private static String _encode(final String s) {
        final int maxBytesPerChar = 10;
        final StringBuffer out = new StringBuffer(s.length());
        final ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
        final OutputStreamWriter writer = new OutputStreamWriter(buf);
        for (int i = 0; i < s.length(); ++i) {
            int c = s.charAt(i);
            if (URLName.dontNeedEncoding.get(c)) {
                if (c == 32) {
                    c = 43;
                }
                out.append((char)c);
            }
            else {
                try {
                    writer.write(c);
                    writer.flush();
                }
                catch (IOException e) {
                    buf.reset();
                    continue;
                }
                final byte[] ba = buf.toByteArray();
                for (int j = 0; j < ba.length; ++j) {
                    out.append('%');
                    char ch = Character.forDigit(ba[j] >> 4 & 0xF, 16);
                    if (Character.isLetter(ch)) {
                        ch -= ' ';
                    }
                    out.append(ch);
                    ch = Character.forDigit(ba[j] & 0xF, 16);
                    if (Character.isLetter(ch)) {
                        ch -= ' ';
                    }
                    out.append(ch);
                }
                buf.reset();
            }
        }
        return out.toString();
    }
    
    static String decode(final String s) {
        if (s == null) {
            return null;
        }
        if (indexOfAny(s, "+%") == -1) {
            return s;
        }
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); ++i) {
            final char c = s.charAt(i);
            switch (c) {
                case '+': {
                    sb.append(' ');
                    break;
                }
                case '%': {
                    try {
                        sb.append((char)Integer.parseInt(s.substring(i + 1, i + 3), 16));
                    }
                    catch (NumberFormatException e) {
                        throw new IllegalArgumentException();
                    }
                    i += 2;
                    break;
                }
                default: {
                    sb.append(c);
                    break;
                }
            }
        }
        String result = sb.toString();
        try {
            final byte[] inputBytes = result.getBytes("8859_1");
            result = new String(inputBytes);
        }
        catch (UnsupportedEncodingException ex) {}
        return result;
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
        URLName.doEncode = true;
        try {
            URLName.doEncode = !Boolean.getBoolean("mail.URLName.dontencode");
        }
        catch (Exception ex) {}
        URLName.dontNeedEncoding = new BitSet(256);
        for (int i = 97; i <= 122; ++i) {
            URLName.dontNeedEncoding.set(i);
        }
        for (int i = 65; i <= 90; ++i) {
            URLName.dontNeedEncoding.set(i);
        }
        for (int i = 48; i <= 57; ++i) {
            URLName.dontNeedEncoding.set(i);
        }
        URLName.dontNeedEncoding.set(32);
        URLName.dontNeedEncoding.set(45);
        URLName.dontNeedEncoding.set(95);
        URLName.dontNeedEncoding.set(46);
        URLName.dontNeedEncoding.set(42);
    }
}
