package javax.mail.internet;

import com.sun.mail.util.*;
import java.io.*;
import java.util.*;
import javax.mail.*;

public class InternetHeaders
{
    protected List headers;
    
    public InternetHeaders() {
        (this.headers = new ArrayList(40)).add(new InternetHeader("Return-Path", null));
        this.headers.add(new InternetHeader("Received", null));
        this.headers.add(new InternetHeader("Resent-Date", null));
        this.headers.add(new InternetHeader("Resent-From", null));
        this.headers.add(new InternetHeader("Resent-Sender", null));
        this.headers.add(new InternetHeader("Resent-To", null));
        this.headers.add(new InternetHeader("Resent-Cc", null));
        this.headers.add(new InternetHeader("Resent-Bcc", null));
        this.headers.add(new InternetHeader("Resent-Message-Id", null));
        this.headers.add(new InternetHeader("Date", null));
        this.headers.add(new InternetHeader("From", null));
        this.headers.add(new InternetHeader("Sender", null));
        this.headers.add(new InternetHeader("Reply-To", null));
        this.headers.add(new InternetHeader("To", null));
        this.headers.add(new InternetHeader("Cc", null));
        this.headers.add(new InternetHeader("Bcc", null));
        this.headers.add(new InternetHeader("Message-Id", null));
        this.headers.add(new InternetHeader("In-Reply-To", null));
        this.headers.add(new InternetHeader("References", null));
        this.headers.add(new InternetHeader("Subject", null));
        this.headers.add(new InternetHeader("Comments", null));
        this.headers.add(new InternetHeader("Keywords", null));
        this.headers.add(new InternetHeader("Errors-To", null));
        this.headers.add(new InternetHeader("MIME-Version", null));
        this.headers.add(new InternetHeader("Content-Type", null));
        this.headers.add(new InternetHeader("Content-Transfer-Encoding", null));
        this.headers.add(new InternetHeader("Content-MD5", null));
        this.headers.add(new InternetHeader(":", null));
        this.headers.add(new InternetHeader("Content-Length", null));
        this.headers.add(new InternetHeader("Status", null));
    }
    
    public InternetHeaders(final InputStream is) throws MessagingException {
        this.headers = new ArrayList(40);
        this.load(is);
    }
    
    public void load(final InputStream is) throws MessagingException {
        final LineInputStream lis = new LineInputStream(is);
        String prevline = null;
        final StringBuffer lineBuffer = new StringBuffer();
        try {
            String line;
            do {
                line = lis.readLine();
                if (line != null && (line.startsWith(" ") || line.startsWith("\t"))) {
                    if (prevline != null) {
                        lineBuffer.append(prevline);
                        prevline = null;
                    }
                    lineBuffer.append("\r\n");
                    lineBuffer.append(line);
                }
                else {
                    if (prevline != null) {
                        this.addHeaderLine(prevline);
                    }
                    else if (lineBuffer.length() > 0) {
                        this.addHeaderLine(lineBuffer.toString());
                        lineBuffer.setLength(0);
                    }
                    prevline = line;
                }
            } while (line != null && line.length() > 0);
        }
        catch (IOException ioex) {
            throw new MessagingException("Error in input stream", ioex);
        }
    }
    
    public String[] getHeader(final String name) {
        final Iterator e = this.headers.iterator();
        final List v = new ArrayList();
        while (e.hasNext()) {
            final InternetHeader h = e.next();
            if (name.equalsIgnoreCase(h.getName()) && h.line != null) {
                v.add(h.getValue());
            }
        }
        if (v.size() == 0) {
            return null;
        }
        String[] r = new String[v.size()];
        r = v.toArray(r);
        return r;
    }
    
    public String getHeader(final String name, final String delimiter) {
        final String[] s = this.getHeader(name);
        if (s == null) {
            return null;
        }
        if (s.length == 1 || delimiter == null) {
            return s[0];
        }
        final StringBuffer r = new StringBuffer(s[0]);
        for (int i = 1; i < s.length; ++i) {
            r.append(delimiter);
            r.append(s[i]);
        }
        return r.toString();
    }
    
    public void setHeader(final String name, final String value) {
        boolean found = false;
        for (int i = 0; i < this.headers.size(); ++i) {
            final InternetHeader h = this.headers.get(i);
            if (name.equalsIgnoreCase(h.getName())) {
                if (!found) {
                    final int j;
                    if (h.line != null && (j = h.line.indexOf(58)) >= 0) {
                        h.line = h.line.substring(0, j + 1) + " " + value;
                    }
                    else {
                        h.line = name + ": " + value;
                    }
                    found = true;
                }
                else {
                    this.headers.remove(i);
                    --i;
                }
            }
        }
        if (!found) {
            this.addHeader(name, value);
        }
    }
    
    public void addHeader(final String name, final String value) {
        int pos = this.headers.size();
        final boolean addReverse = name.equalsIgnoreCase("Received") || name.equalsIgnoreCase("Return-Path");
        if (addReverse) {
            pos = 0;
        }
        for (int i = this.headers.size() - 1; i >= 0; --i) {
            final InternetHeader h = this.headers.get(i);
            if (name.equalsIgnoreCase(h.getName())) {
                if (!addReverse) {
                    this.headers.add(i + 1, new InternetHeader(name, value));
                    return;
                }
                pos = i;
            }
            if (h.getName().equals(":")) {
                pos = i;
            }
        }
        this.headers.add(pos, new InternetHeader(name, value));
    }
    
    public void removeHeader(final String name) {
        for (int i = 0; i < this.headers.size(); ++i) {
            final InternetHeader h = this.headers.get(i);
            if (name.equalsIgnoreCase(h.getName())) {
                h.line = null;
            }
        }
    }
    
    public Enumeration getAllHeaders() {
        return new matchEnum(this.headers, null, false, false);
    }
    
    public Enumeration getMatchingHeaders(final String[] names) {
        return new matchEnum(this.headers, names, true, false);
    }
    
    public Enumeration getNonMatchingHeaders(final String[] names) {
        return new matchEnum(this.headers, names, false, false);
    }
    
    public void addHeaderLine(final String line) {
        try {
            final char c = line.charAt(0);
            if (c == ' ' || c == '\t') {
                final InternetHeader h = this.headers.get(this.headers.size() - 1);
                final StringBuffer sb = new StringBuffer();
                final InternetHeader internetHeader = h;
                internetHeader.line = sb.append(internetHeader.line).append("\r\n").append(line).toString();
            }
            else {
                this.headers.add(new InternetHeader(line));
            }
        }
        catch (StringIndexOutOfBoundsException e) {}
        catch (NoSuchElementException ex) {}
    }
    
    public Enumeration getAllHeaderLines() {
        return this.getNonMatchingHeaderLines(null);
    }
    
    public Enumeration getMatchingHeaderLines(final String[] names) {
        return new matchEnum(this.headers, names, true, true);
    }
    
    public Enumeration getNonMatchingHeaderLines(final String[] names) {
        return new matchEnum(this.headers, names, false, true);
    }
    
    protected static final class InternetHeader extends Header
    {
        String line;
        
        public InternetHeader(final String l) {
            super("", "");
            final int i = l.indexOf(58);
            if (i < 0) {
                this.name = l.trim();
            }
            else {
                this.name = l.substring(0, i).trim();
            }
            this.line = l;
        }
        
        public InternetHeader(final String n, final String v) {
            super(n, "");
            if (v != null) {
                this.line = n + ": " + v;
            }
            else {
                this.line = null;
            }
        }
        
        public String getValue() {
            final int i = this.line.indexOf(58);
            if (i < 0) {
                return this.line;
            }
            int j;
            for (j = i + 1; j < this.line.length(); ++j) {
                final char c = this.line.charAt(j);
                if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                    break;
                }
            }
            return this.line.substring(j);
        }
    }
    
    static class matchEnum implements Enumeration
    {
        private Iterator e;
        private String[] names;
        private boolean match;
        private boolean want_line;
        private InternetHeader next_header;
        
        matchEnum(final List v, final String[] n, final boolean m, final boolean l) {
            this.e = v.iterator();
            this.names = n;
            this.match = m;
            this.want_line = l;
            this.next_header = null;
        }
        
        public boolean hasMoreElements() {
            if (this.next_header == null) {
                this.next_header = this.nextMatch();
            }
            return this.next_header != null;
        }
        
        public Object nextElement() {
            if (this.next_header == null) {
                this.next_header = this.nextMatch();
            }
            if (this.next_header == null) {
                throw new NoSuchElementException("No more headers");
            }
            final InternetHeader h = this.next_header;
            this.next_header = null;
            if (this.want_line) {
                return h.line;
            }
            return new Header(h.getName(), h.getValue());
        }
        
        private InternetHeader nextMatch() {
        Label_0000:
            while (this.e.hasNext()) {
                final InternetHeader h = this.e.next();
                if (h.line == null) {
                    continue;
                }
                if (this.names == null) {
                    return this.match ? null : h;
                }
                int i = 0;
                while (i < this.names.length) {
                    if (this.names[i].equalsIgnoreCase(h.getName())) {
                        if (this.match) {
                            return h;
                        }
                        continue Label_0000;
                    }
                    else {
                        ++i;
                    }
                }
                if (!this.match) {
                    return h;
                }
            }
            return null;
        }
    }
}
