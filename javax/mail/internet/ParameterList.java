package javax.mail.internet;

import java.util.*;
import java.io.*;

public class ParameterList
{
    private Map list;
    private static boolean encodeParameters;
    private static boolean decodeParameters;
    private static boolean decodeParametersStrict;
    private static final char[] hex;
    
    public ParameterList() {
        this.list = new LinkedHashMap();
    }
    
    public ParameterList(final String s) throws ParseException {
        this.list = new LinkedHashMap();
        final HeaderTokenizer h = new HeaderTokenizer(s, "()<>@,;:\\\"\t []/?=");
        while (true) {
            HeaderTokenizer.Token tk = h.next();
            int type = tk.getType();
            if (type == -4) {
                return;
            }
            if ((char)type != ';') {
                throw new ParseException("Expected ';', got \"" + tk.getValue() + "\"");
            }
            tk = h.next();
            if (tk.getType() == -4) {
                return;
            }
            if (tk.getType() != -1) {
                throw new ParseException("Expected parameter name, got \"" + tk.getValue() + "\"");
            }
            String name = tk.getValue().toLowerCase();
            tk = h.next();
            if ((char)tk.getType() != '=') {
                throw new ParseException("Expected '=', got \"" + tk.getValue() + "\"");
            }
            tk = h.next();
            type = tk.getType();
            if (type != -1 && type != -2) {
                throw new ParseException("Expected parameter value, got \"" + tk.getValue() + "\"");
            }
            final String value = tk.getValue();
            if (ParameterList.decodeParameters && name.endsWith("*")) {
                name = name.substring(0, name.length() - 1);
                this.list.put(name, this.decodeValue(value));
            }
            else {
                this.list.put(name, value);
            }
        }
    }
    
    public int size() {
        return this.list.size();
    }
    
    public String get(final String name) {
        final Object v = this.list.get(name.trim().toLowerCase());
        String value;
        if (v instanceof Value) {
            value = ((Value)v).value;
        }
        else {
            value = (String)v;
        }
        return value;
    }
    
    public void set(final String name, final String value) {
        this.list.put(name.trim().toLowerCase(), value);
    }
    
    public void set(final String name, final String value, final String charset) {
        if (ParameterList.encodeParameters) {
            final Value ev = this.encodeValue(value, charset);
            if (ev != null) {
                this.list.put(name.trim().toLowerCase(), ev);
            }
            else {
                this.set(name, value);
            }
        }
        else {
            this.set(name, value);
        }
    }
    
    public void remove(final String name) {
        this.list.remove(name.trim().toLowerCase());
    }
    
    public Enumeration getNames() {
        return new ParamEnum(this.list.keySet().iterator());
    }
    
    public String toString() {
        return this.toString(0);
    }
    
    public String toString(int used) {
        final StringBuffer sb = new StringBuffer();
        for (String name : this.list.keySet()) {
            final Object v = this.list.get(name);
            String value;
            if (v instanceof Value) {
                value = ((Value)v).encodedValue;
                name += '*';
            }
            else {
                value = (String)v;
            }
            value = this.quote(value);
            sb.append("; ");
            used += 2;
            final int len = name.length() + value.length() + 1;
            if (used + len > 76) {
                sb.append("\r\n\t");
                used = 8;
            }
            sb.append(name).append('=');
            used += name.length() + 1;
            if (used + value.length() > 76) {
                final String s = MimeUtility.fold(used, value);
                sb.append(s);
                final int lastlf = s.lastIndexOf(10);
                if (lastlf >= 0) {
                    used += s.length() - lastlf - 1;
                }
                else {
                    used += s.length();
                }
            }
            else {
                sb.append(value);
                used += value.length();
            }
        }
        return sb.toString();
    }
    
    private String quote(final String value) {
        return MimeUtility.quote(value, "()<>@,;:\\\"\t []/?=");
    }
    
    private Value encodeValue(final String value, final String charset) {
        if (MimeUtility.checkAscii(value) == 1) {
            return null;
        }
        byte[] b;
        try {
            b = value.getBytes(MimeUtility.javaCharset(charset));
        }
        catch (UnsupportedEncodingException ex) {
            return null;
        }
        final StringBuffer sb = new StringBuffer(b.length + charset.length() + 2);
        sb.append(charset).append("''");
        for (int i = 0; i < b.length; ++i) {
            final char c = (char)(b[i] & 0xFF);
            if (c <= ' ' || c >= '\u007f' || c == '*' || c == '\'' || c == '%' || "()<>@,;:\\\"\t []/?=".indexOf(c) >= 0) {
                sb.append('%').append(ParameterList.hex[c >> 4]).append(ParameterList.hex[c & '\u000f']);
            }
            else {
                sb.append(c);
            }
        }
        final Value v = new Value();
        v.value = value;
        v.encodedValue = sb.toString();
        return v;
    }
    
    private Value decodeValue(String value) throws ParseException {
        final Value v = new Value();
        v.encodedValue = value;
        v.value = value;
        try {
            int i = value.indexOf(39);
            if (i <= 0) {
                if (ParameterList.decodeParametersStrict) {
                    throw new ParseException("Missing charset in encoded value: " + value);
                }
                return v;
            }
            else {
                final String charset = value.substring(0, i);
                final int li = value.indexOf(39, i + 1);
                if (li < 0) {
                    if (ParameterList.decodeParametersStrict) {
                        throw new ParseException("Missing language in encoded value: " + value);
                    }
                    return v;
                }
                else {
                    final String lang = value.substring(i + 1, li);
                    value = value.substring(li + 1);
                    final byte[] b = new byte[value.length()];
                    i = 0;
                    int bi = 0;
                    while (i < value.length()) {
                        char c = value.charAt(i);
                        if (c == '%') {
                            final String hex = value.substring(i + 1, i + 3);
                            c = (char)Integer.parseInt(hex, 16);
                            i += 2;
                        }
                        b[bi++] = (byte)c;
                        ++i;
                    }
                    v.value = new String(b, 0, bi, MimeUtility.javaCharset(charset));
                }
            }
        }
        catch (NumberFormatException nex) {
            if (ParameterList.decodeParametersStrict) {
                throw new ParseException(nex.toString());
            }
        }
        catch (UnsupportedEncodingException uex) {
            if (ParameterList.decodeParametersStrict) {
                throw new ParseException(uex.toString());
            }
        }
        catch (StringIndexOutOfBoundsException ex) {
            if (ParameterList.decodeParametersStrict) {
                throw new ParseException(ex.toString());
            }
        }
        return v;
    }
    
    static {
        ParameterList.encodeParameters = false;
        ParameterList.decodeParameters = false;
        ParameterList.decodeParametersStrict = false;
        try {
            String s = System.getProperty("mail.mime.encodeparameters");
            ParameterList.encodeParameters = (s != null && s.equalsIgnoreCase("true"));
            s = System.getProperty("mail.mime.decodeparameters");
            ParameterList.decodeParameters = (s != null && s.equalsIgnoreCase("true"));
            s = System.getProperty("mail.mime.decodeparameters.strict");
            ParameterList.decodeParametersStrict = (s != null && s.equalsIgnoreCase("true"));
        }
        catch (SecurityException ex) {}
        hex = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    }
    
    private static class Value
    {
        String value;
        String encodedValue;
    }
    
    private static class ParamEnum implements Enumeration
    {
        private Iterator it;
        
        ParamEnum(final Iterator it) {
            this.it = it;
        }
        
        public boolean hasMoreElements() {
            return this.it.hasNext();
        }
        
        public Object nextElement() {
            return this.it.next();
        }
    }
}
