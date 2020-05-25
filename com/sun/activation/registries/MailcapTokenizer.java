package com.sun.activation.registries;

public class MailcapTokenizer
{
    public static final int UNKNOWN_TOKEN = 0;
    public static final int START_TOKEN = 1;
    public static final int STRING_TOKEN = 2;
    public static final int EOI_TOKEN = 5;
    public static final int SLASH_TOKEN = 47;
    public static final int SEMICOLON_TOKEN = 59;
    public static final int EQUALS_TOKEN = 61;
    private String data;
    private int dataIndex;
    private int dataLength;
    private int currentToken;
    private String currentTokenValue;
    private boolean isAutoquoting;
    private char autoquoteChar;
    
    public MailcapTokenizer(final String data) {
        this.data = data;
        this.dataIndex = 0;
        this.dataLength = data.length();
        this.currentToken = 1;
        this.currentTokenValue = "";
        this.isAutoquoting = false;
        this.autoquoteChar = ';';
    }
    
    public void setIsAutoquoting(final boolean isAutoquoting) {
        this.isAutoquoting = isAutoquoting;
    }
    
    public int getCurrentToken() {
        return this.currentToken;
    }
    
    public static String nameForToken(final int n) {
        String s = "really unknown";
        switch (n) {
            case 0: {
                s = "unknown";
                break;
            }
            case 1: {
                s = "start";
                break;
            }
            case 2: {
                s = "string";
                break;
            }
            case 5: {
                s = "EOI";
                break;
            }
            case 47: {
                s = "'/'";
                break;
            }
            case 59: {
                s = "';'";
                break;
            }
            case 61: {
                s = "'='";
                break;
            }
        }
        return s;
    }
    
    public String getCurrentTokenValue() {
        return this.currentTokenValue;
    }
    
    public int nextToken() {
        if (this.dataIndex < this.dataLength) {
            while (this.dataIndex < this.dataLength && isWhiteSpaceChar(this.data.charAt(this.dataIndex))) {
                ++this.dataIndex;
            }
            if (this.dataIndex < this.dataLength) {
                final char char1 = this.data.charAt(this.dataIndex);
                if (this.isAutoquoting) {
                    if (char1 == ';' || char1 == '=') {
                        this.currentToken = char1;
                        this.currentTokenValue = new Character(char1).toString();
                        ++this.dataIndex;
                    }
                    else {
                        this.processAutoquoteToken();
                    }
                }
                else if (isStringTokenChar(char1)) {
                    this.processStringToken();
                }
                else if (char1 == '/' || char1 == ';' || char1 == '=') {
                    this.currentToken = char1;
                    this.currentTokenValue = new Character(char1).toString();
                    ++this.dataIndex;
                }
                else {
                    this.currentToken = 0;
                    this.currentTokenValue = new Character(char1).toString();
                    ++this.dataIndex;
                }
            }
            else {
                this.currentToken = 5;
                this.currentTokenValue = null;
            }
        }
        else {
            this.currentToken = 5;
            this.currentTokenValue = null;
        }
        return this.currentToken;
    }
    
    private void processStringToken() {
        final int dataIndex = this.dataIndex;
        while (this.dataIndex < this.dataLength && isStringTokenChar(this.data.charAt(this.dataIndex))) {
            ++this.dataIndex;
        }
        this.currentToken = 2;
        this.currentTokenValue = this.data.substring(dataIndex, this.dataIndex);
    }
    
    private void processAutoquoteToken() {
        final int dataIndex = this.dataIndex;
        int n = 0;
        while (this.dataIndex < this.dataLength && n == 0) {
            if (this.data.charAt(this.dataIndex) != this.autoquoteChar) {
                ++this.dataIndex;
            }
            else {
                n = 1;
            }
        }
        this.currentToken = 2;
        this.currentTokenValue = fixEscapeSequences(this.data.substring(dataIndex, this.dataIndex));
    }
    
    private static boolean isSpecialChar(final char c) {
        boolean b = false;
        switch (c) {
            case '\"':
            case '(':
            case ')':
            case ',':
            case '/':
            case ':':
            case ';':
            case '<':
            case '=':
            case '>':
            case '?':
            case '@':
            case '[':
            case '\\':
            case ']': {
                b = true;
                break;
            }
        }
        return b;
    }
    
    private static boolean isControlChar(final char c) {
        return Character.isISOControl(c);
    }
    
    private static boolean isWhiteSpaceChar(final char c) {
        return Character.isWhitespace(c);
    }
    
    private static boolean isStringTokenChar(final char c) {
        return !isSpecialChar(c) && !isControlChar(c) && !isWhiteSpaceChar(c);
    }
    
    private static String fixEscapeSequences(final String s) {
        final int length = s.length();
        final StringBuffer sb = new StringBuffer();
        sb.ensureCapacity(length);
        for (int i = 0; i < length; ++i) {
            final char char1 = s.charAt(i);
            if (char1 != '\\') {
                sb.append(char1);
            }
            else if (i < length - 1) {
                sb.append(s.charAt(i + 1));
                ++i;
            }
            else {
                sb.append(char1);
            }
        }
        return sb.toString();
    }
}
