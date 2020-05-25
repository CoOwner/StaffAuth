package javax.activation;

import java.util.*;

public class MimeTypeParameterList
{
    private Hashtable parameters;
    private static final String TSPECIALS = "()<>@,;:/[]?=\\\"";
    
    public MimeTypeParameterList() {
        this.parameters = new Hashtable();
    }
    
    public MimeTypeParameterList(final String s) throws MimeTypeParseException {
        this.parameters = new Hashtable();
        this.parse(s);
    }
    
    protected void parse(final String s) throws MimeTypeParseException {
        if (s == null) {
            return;
        }
        final int length = s.length();
        if (length <= 0) {
            return;
        }
        int n;
        char char1;
        int i;
        for (n = skipWhiteSpace(s, 0); n < length && (char1 = s.charAt(n)) == ';'; n = skipWhiteSpace(s, i)) {
            ++n;
            int skipWhiteSpace = skipWhiteSpace(s, n);
            if (skipWhiteSpace >= length) {
                return;
            }
            final int n2 = skipWhiteSpace;
            while (skipWhiteSpace < length && isTokenChar(s.charAt(skipWhiteSpace))) {
                ++skipWhiteSpace;
            }
            final String lowerCase = s.substring(n2, skipWhiteSpace).toLowerCase(Locale.ENGLISH);
            int skipWhiteSpace2 = skipWhiteSpace(s, skipWhiteSpace);
            if (skipWhiteSpace2 >= length || s.charAt(skipWhiteSpace2) != '=') {
                throw new MimeTypeParseException("Couldn't find the '=' that separates a parameter name from its value.");
            }
            ++skipWhiteSpace2;
            i = skipWhiteSpace(s, skipWhiteSpace2);
            if (i >= length) {
                throw new MimeTypeParseException("Couldn't find a value for parameter named " + lowerCase);
            }
            char c = s.charAt(i);
            String s2;
            if (c == '\"') {
                if (++i >= length) {
                    throw new MimeTypeParseException("Encountered unterminated quoted parameter value.");
                }
                final int n3 = i;
                while (i < length) {
                    c = s.charAt(i);
                    if (c == '\"') {
                        break;
                    }
                    if (c == '\\') {
                        ++i;
                    }
                    ++i;
                }
                if (c != '\"') {
                    throw new MimeTypeParseException("Encountered unterminated quoted parameter value.");
                }
                s2 = unquote(s.substring(n3, i));
                ++i;
            }
            else {
                if (!isTokenChar(c)) {
                    throw new MimeTypeParseException("Unexpected character encountered at index " + i);
                }
                final int n4 = i;
                while (i < length && isTokenChar(s.charAt(i))) {
                    ++i;
                }
                s2 = s.substring(n4, i);
            }
            this.parameters.put(lowerCase, s2);
        }
        if (n < length) {
            throw new MimeTypeParseException("More characters encountered in input than expected.");
        }
    }
    
    public int size() {
        return this.parameters.size();
    }
    
    public boolean isEmpty() {
        return this.parameters.isEmpty();
    }
    
    public String get(final String s) {
        return this.parameters.get(s.trim().toLowerCase(Locale.ENGLISH));
    }
    
    public void set(final String s, final String s2) {
        this.parameters.put(s.trim().toLowerCase(Locale.ENGLISH), s2);
    }
    
    public void remove(final String s) {
        this.parameters.remove(s.trim().toLowerCase(Locale.ENGLISH));
    }
    
    public Enumeration getNames() {
        return this.parameters.keys();
    }
    
    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.ensureCapacity(this.parameters.size() * 16);
        final Enumeration<String> keys = this.parameters.keys();
        while (keys.hasMoreElements()) {
            final String s = keys.nextElement();
            sb.append("; ");
            sb.append(s);
            sb.append('=');
            sb.append(quote((String)this.parameters.get(s)));
        }
        return sb.toString();
    }
    
    private static boolean isTokenChar(final char c) {
        return c > ' ' && c < '\u007f' && "()<>@,;:/[]?=\\\"".indexOf(c) < 0;
    }
    
    private static int skipWhiteSpace(final String s, int n) {
        while (n < s.length() && Character.isWhitespace(s.charAt(n))) {
            ++n;
        }
        return n;
    }
    
    private static String quote(final String s) {
        boolean b = false;
        final int length = s.length();
        for (int n = 0; n < length && !b; b = !isTokenChar(s.charAt(n)), ++n) {}
        if (b) {
            final StringBuffer sb = new StringBuffer();
            sb.ensureCapacity((int)(length * 1.5));
            sb.append('\"');
            for (int i = 0; i < length; ++i) {
                final char char1 = s.charAt(i);
                if (char1 == '\\' || char1 == '\"') {
                    sb.append('\\');
                }
                sb.append(char1);
            }
            sb.append('\"');
            return sb.toString();
        }
        return s;
    }
    
    private static String unquote(final String s) {
        final int length = s.length();
        final StringBuffer sb = new StringBuffer();
        sb.ensureCapacity(length);
        int n = 0;
        for (int i = 0; i < length; ++i) {
            final char char1 = s.charAt(i);
            if (n == 0 && char1 != '\\') {
                sb.append(char1);
            }
            else if (n != 0) {
                sb.append(char1);
                n = 0;
            }
            else {
                n = 1;
            }
        }
        return sb.toString();
    }
}
