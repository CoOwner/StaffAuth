package com.sun.mail.util;

import java.io.*;

public class ASCIIUtility
{
    private ASCIIUtility() {
    }
    
    public static int parseInt(final byte[] b, final int start, final int end, final int radix) throws NumberFormatException {
        if (b == null) {
            throw new NumberFormatException("null");
        }
        int result = 0;
        boolean negative = false;
        int i;
        if (end <= (i = start)) {
            throw new NumberFormatException("illegal number");
        }
        int limit;
        if (b[i] == 45) {
            negative = true;
            limit = Integer.MIN_VALUE;
            ++i;
        }
        else {
            limit = -2147483647;
        }
        final int multmin = limit / radix;
        if (i < end) {
            final int digit = Character.digit((char)b[i++], radix);
            if (digit < 0) {
                throw new NumberFormatException("illegal number: " + toString(b, start, end));
            }
            result = -digit;
        }
        while (i < end) {
            final int digit = Character.digit((char)b[i++], radix);
            if (digit < 0) {
                throw new NumberFormatException("illegal number");
            }
            if (result < multmin) {
                throw new NumberFormatException("illegal number");
            }
            result *= radix;
            if (result < limit + digit) {
                throw new NumberFormatException("illegal number");
            }
            result -= digit;
        }
        if (!negative) {
            return -result;
        }
        if (i > start + 1) {
            return result;
        }
        throw new NumberFormatException("illegal number");
    }
    
    public static int parseInt(final byte[] b, final int start, final int end) throws NumberFormatException {
        return parseInt(b, start, end, 10);
    }
    
    public static long parseLong(final byte[] b, final int start, final int end, final int radix) throws NumberFormatException {
        if (b == null) {
            throw new NumberFormatException("null");
        }
        long result = 0L;
        boolean negative = false;
        int i;
        if (end <= (i = start)) {
            throw new NumberFormatException("illegal number");
        }
        long limit;
        if (b[i] == 45) {
            negative = true;
            limit = Long.MIN_VALUE;
            ++i;
        }
        else {
            limit = -9223372036854775807L;
        }
        final long multmin = limit / radix;
        if (i < end) {
            final int digit = Character.digit((char)b[i++], radix);
            if (digit < 0) {
                throw new NumberFormatException("illegal number: " + toString(b, start, end));
            }
            result = -digit;
        }
        while (i < end) {
            final int digit = Character.digit((char)b[i++], radix);
            if (digit < 0) {
                throw new NumberFormatException("illegal number");
            }
            if (result < multmin) {
                throw new NumberFormatException("illegal number");
            }
            result *= radix;
            if (result < limit + digit) {
                throw new NumberFormatException("illegal number");
            }
            result -= digit;
        }
        if (!negative) {
            return -result;
        }
        if (i > start + 1) {
            return result;
        }
        throw new NumberFormatException("illegal number");
    }
    
    public static long parseLong(final byte[] b, final int start, final int end) throws NumberFormatException {
        return parseLong(b, start, end, 10);
    }
    
    public static String toString(final byte[] b, final int start, final int end) {
        final int size = end - start;
        final char[] theChars = new char[size];
        for (int i = 0, j = start; i < size; theChars[i++] = (char)(b[j++] & 0xFF)) {}
        return new String(theChars);
    }
    
    public static String toString(final ByteArrayInputStream is) {
        final int size = is.available();
        final char[] theChars = new char[size];
        final byte[] bytes = new byte[size];
        is.read(bytes, 0, size);
        for (int i = 0; i < size; theChars[i] = (char)(bytes[i++] & 0xFF)) {}
        return new String(theChars);
    }
    
    public static byte[] getBytes(final String s) {
        final char[] chars = s.toCharArray();
        final int size = chars.length;
        final byte[] bytes = new byte[size];
        for (int i = 0; i < size; bytes[i] = (byte)chars[i++]) {}
        return bytes;
    }
    
    public static byte[] getBytes(final InputStream is) throws IOException {
        int size = 1024;
        byte[] buf;
        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            final int len = is.read(buf, 0, size);
        }
        else {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            int len;
            while ((len = is.read(buf, 0, size)) != -1) {
                bos.write(buf, 0, len);
            }
            buf = bos.toByteArray();
        }
        return buf;
    }
}
