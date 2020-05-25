package com.sun.mail.imap.protocol;

import java.text.*;

public class BASE64MailboxDecoder
{
    protected static final char[] pem_array;
    protected static final byte[] pem_convert_array;
    
    public static String decode(final String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        boolean changedString = false;
        int copyTo = 0;
        final char[] chars = new char[original.length()];
        final StringCharacterIterator iter = new StringCharacterIterator(original);
        for (char c = iter.first(); c != '\uffff'; c = iter.next()) {
            if (c == '&') {
                changedString = true;
                copyTo = base64decode(chars, copyTo, iter);
            }
            else {
                chars[copyTo++] = c;
            }
        }
        if (changedString) {
            return new String(chars, 0, copyTo);
        }
        return original;
    }
    
    protected static int base64decode(final char[] buffer, int offset, final CharacterIterator iter) {
        boolean firsttime = true;
        int leftover = -1;
        char testing = '\0';
        while (true) {
            final byte orig_0 = (byte)iter.next();
            if (orig_0 == -1) {
                break;
            }
            if (orig_0 == 45) {
                if (firsttime) {
                    buffer[offset++] = '&';
                    break;
                }
                break;
            }
            else {
                firsttime = false;
                final byte orig_2 = (byte)iter.next();
                if (orig_2 == -1) {
                    break;
                }
                if (orig_2 == 45) {
                    break;
                }
                byte a = BASE64MailboxDecoder.pem_convert_array[orig_0 & 0xFF];
                byte b = BASE64MailboxDecoder.pem_convert_array[orig_2 & 0xFF];
                byte current = (byte)((a << 2 & 0xFC) | (b >>> 4 & 0x3));
                if (leftover != -1) {
                    buffer[offset++] = (char)(leftover << 8 | (current & 0xFF));
                    leftover = -1;
                }
                else {
                    leftover = (current & 0xFF);
                }
                final byte orig_3 = (byte)iter.next();
                if (orig_3 == 61) {
                    continue;
                }
                if (orig_3 == -1) {
                    break;
                }
                if (orig_3 == 45) {
                    break;
                }
                a = b;
                b = BASE64MailboxDecoder.pem_convert_array[orig_3 & 0xFF];
                current = (byte)((a << 4 & 0xF0) | (b >>> 2 & 0xF));
                if (leftover != -1) {
                    buffer[offset++] = (char)(leftover << 8 | (current & 0xFF));
                    leftover = -1;
                }
                else {
                    leftover = (current & 0xFF);
                }
                final byte orig_4 = (byte)iter.next();
                if (orig_4 == 61) {
                    continue;
                }
                if (orig_4 == -1) {
                    break;
                }
                if (orig_4 == 45) {
                    break;
                }
                a = b;
                b = BASE64MailboxDecoder.pem_convert_array[orig_4 & 0xFF];
                current = (byte)((a << 6 & 0xC0) | (b & 0x3F));
                if (leftover != -1) {
                    testing = (char)(leftover << 8 | (current & 0xFF));
                    buffer[offset++] = (char)(leftover << 8 | (current & 0xFF));
                    leftover = -1;
                }
                else {
                    leftover = (current & 0xFF);
                }
            }
        }
        return offset;
    }
    
    static {
        pem_array = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', ',' };
        pem_convert_array = new byte[256];
        for (int i = 0; i < 255; ++i) {
            BASE64MailboxDecoder.pem_convert_array[i] = -1;
        }
        for (int i = 0; i < BASE64MailboxDecoder.pem_array.length; ++i) {
            BASE64MailboxDecoder.pem_convert_array[BASE64MailboxDecoder.pem_array[i]] = (byte)i;
        }
    }
}
