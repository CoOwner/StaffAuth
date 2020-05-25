package javax.mail.internet;

public class HeaderTokenizer
{
    private String string;
    private boolean skipComments;
    private String delimiters;
    private int currentPos;
    private int maxPos;
    private int nextPos;
    private int peekPos;
    public static final String RFC822 = "()<>@,;:\\\"\t .[]";
    public static final String MIME = "()<>@,;:\\\"\t []/?=";
    private static final Token EOFToken;
    
    public HeaderTokenizer(final String header, final String delimiters, final boolean skipComments) {
        this.string = ((header == null) ? "" : header);
        this.skipComments = skipComments;
        this.delimiters = delimiters;
        final int currentPos = 0;
        this.peekPos = currentPos;
        this.nextPos = currentPos;
        this.currentPos = currentPos;
        this.maxPos = this.string.length();
    }
    
    public HeaderTokenizer(final String header, final String delimiters) {
        this(header, delimiters, true);
    }
    
    public HeaderTokenizer(final String header) {
        this(header, "()<>@,;:\\\"\t .[]");
    }
    
    public Token next() throws ParseException {
        this.currentPos = this.nextPos;
        final Token tk = this.getNext();
        final int currentPos = this.currentPos;
        this.peekPos = currentPos;
        this.nextPos = currentPos;
        return tk;
    }
    
    public Token peek() throws ParseException {
        this.currentPos = this.peekPos;
        final Token tk = this.getNext();
        this.peekPos = this.currentPos;
        return tk;
    }
    
    public String getRemainder() {
        return this.string.substring(this.nextPos);
    }
    
    private Token getNext() throws ParseException {
        if (this.currentPos >= this.maxPos) {
            return HeaderTokenizer.EOFToken;
        }
        if (this.skipWhiteSpace() == -4) {
            return HeaderTokenizer.EOFToken;
        }
        boolean filter = false;
        char c;
        for (c = this.string.charAt(this.currentPos); c == '('; c = this.string.charAt(this.currentPos)) {
            final int start = ++this.currentPos;
            int nesting = 1;
            while (nesting > 0 && this.currentPos < this.maxPos) {
                c = this.string.charAt(this.currentPos);
                if (c == '\\') {
                    ++this.currentPos;
                    filter = true;
                }
                else if (c == '\r') {
                    filter = true;
                }
                else if (c == '(') {
                    ++nesting;
                }
                else if (c == ')') {
                    --nesting;
                }
                ++this.currentPos;
            }
            if (nesting != 0) {
                throw new ParseException("Unbalanced comments");
            }
            if (!this.skipComments) {
                String s;
                if (filter) {
                    s = filterToken(this.string, start, this.currentPos - 1);
                }
                else {
                    s = this.string.substring(start, this.currentPos - 1);
                }
                return new Token(-3, s);
            }
            if (this.skipWhiteSpace() == -4) {
                return HeaderTokenizer.EOFToken;
            }
        }
        if (c == '\"') {
            final int start = ++this.currentPos;
            while (this.currentPos < this.maxPos) {
                c = this.string.charAt(this.currentPos);
                if (c == '\\') {
                    ++this.currentPos;
                    filter = true;
                }
                else if (c == '\r') {
                    filter = true;
                }
                else if (c == '\"') {
                    ++this.currentPos;
                    String s2;
                    if (filter) {
                        s2 = filterToken(this.string, start, this.currentPos - 1);
                    }
                    else {
                        s2 = this.string.substring(start, this.currentPos - 1);
                    }
                    return new Token(-2, s2);
                }
                ++this.currentPos;
            }
            throw new ParseException("Unbalanced quoted string");
        }
        if (c < ' ' || c >= '\u007f' || this.delimiters.indexOf(c) >= 0) {
            ++this.currentPos;
            final char[] ch = { c };
            return new Token(c, new String(ch));
        }
        final int start = this.currentPos;
        while (this.currentPos < this.maxPos) {
            c = this.string.charAt(this.currentPos);
            if (c < ' ' || c >= '\u007f' || c == '(' || c == ' ' || c == '\"') {
                break;
            }
            if (this.delimiters.indexOf(c) >= 0) {
                break;
            }
            ++this.currentPos;
        }
        return new Token(-1, this.string.substring(start, this.currentPos));
    }
    
    private int skipWhiteSpace() {
        while (this.currentPos < this.maxPos) {
            final char c;
            if ((c = this.string.charAt(this.currentPos)) != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return this.currentPos;
            }
            ++this.currentPos;
        }
        return -4;
    }
    
    private static String filterToken(final String s, final int start, final int end) {
        final StringBuffer sb = new StringBuffer();
        boolean gotEscape = false;
        boolean gotCR = false;
        for (int i = start; i < end; ++i) {
            final char c = s.charAt(i);
            if (c == '\n' && gotCR) {
                gotCR = false;
            }
            else {
                gotCR = false;
                if (!gotEscape) {
                    if (c == '\\') {
                        gotEscape = true;
                    }
                    else if (c == '\r') {
                        gotCR = true;
                    }
                    else {
                        sb.append(c);
                    }
                }
                else {
                    sb.append(c);
                    gotEscape = false;
                }
            }
        }
        return sb.toString();
    }
    
    static {
        EOFToken = new Token(-4, null);
    }
    
    public static class Token
    {
        private int type;
        private String value;
        public static final int ATOM = -1;
        public static final int QUOTEDSTRING = -2;
        public static final int COMMENT = -3;
        public static final int EOF = -4;
        
        public Token(final int type, final String value) {
            this.type = type;
            this.value = value;
        }
        
        public int getType() {
            return this.type;
        }
        
        public String getValue() {
            return this.value;
        }
    }
}
