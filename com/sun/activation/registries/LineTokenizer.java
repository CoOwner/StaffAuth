package com.sun.activation.registries;

import java.util.*;

class LineTokenizer
{
    private int currentPosition;
    private int maxPosition;
    private String str;
    private Vector stack;
    private static final String singles = "=";
    
    public LineTokenizer(final String str) {
        this.stack = new Vector();
        this.currentPosition = 0;
        this.str = str;
        this.maxPosition = str.length();
    }
    
    private void skipWhiteSpace() {
        while (this.currentPosition < this.maxPosition && Character.isWhitespace(this.str.charAt(this.currentPosition))) {
            ++this.currentPosition;
        }
    }
    
    public boolean hasMoreTokens() {
        if (this.stack.size() > 0) {
            return true;
        }
        this.skipWhiteSpace();
        return this.currentPosition < this.maxPosition;
    }
    
    public String nextToken() {
        final int size = this.stack.size();
        if (size > 0) {
            final String s = this.stack.elementAt(size - 1);
            this.stack.removeElementAt(size - 1);
            return s;
        }
        this.skipWhiteSpace();
        if (this.currentPosition >= this.maxPosition) {
            throw new NoSuchElementException();
        }
        final int currentPosition = this.currentPosition;
        final char char1 = this.str.charAt(currentPosition);
        if (char1 == '\"') {
            ++this.currentPosition;
            boolean b = false;
            while (this.currentPosition < this.maxPosition) {
                final char char2 = this.str.charAt(this.currentPosition++);
                if (char2 == '\\') {
                    ++this.currentPosition;
                    b = true;
                }
                else {
                    if (char2 == '\"') {
                        String s2;
                        if (b) {
                            final StringBuffer sb = new StringBuffer();
                            for (int i = currentPosition + 1; i < this.currentPosition - 1; ++i) {
                                final char char3 = this.str.charAt(i);
                                if (char3 != '\\') {
                                    sb.append(char3);
                                }
                            }
                            s2 = sb.toString();
                        }
                        else {
                            s2 = this.str.substring(currentPosition + 1, this.currentPosition - 1);
                        }
                        return s2;
                    }
                    continue;
                }
            }
        }
        else if ("=".indexOf(char1) >= 0) {
            ++this.currentPosition;
        }
        else {
            while (this.currentPosition < this.maxPosition && "=".indexOf(this.str.charAt(this.currentPosition)) < 0 && !Character.isWhitespace(this.str.charAt(this.currentPosition))) {
                ++this.currentPosition;
            }
        }
        return this.str.substring(currentPosition, this.currentPosition);
    }
    
    public void pushToken(final String s) {
        this.stack.addElement(s);
    }
}
