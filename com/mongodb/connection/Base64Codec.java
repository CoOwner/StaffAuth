package com.mongodb.connection;

import com.mongodb.*;
import java.io.*;

class Base64Codec
{
    private static final int BYTES_PER_UNENCODED_BLOCK = 3;
    private static final int BYTES_PER_ENCODED_BLOCK = 4;
    private static final int SIX_BIT_MASK = 63;
    private static final byte PAD = 61;
    private static final byte[] ENCODE_TABLE;
    private static final int[] DECODE_TABLE;
    
    public byte[] decode(final String s) {
        final int delta = s.endsWith("==") ? 2 : (s.endsWith("=") ? 1 : 0);
        final byte[] buffer = new byte[s.length() * 3 / 4 - delta];
        final int mask = 255;
        int pos = 0;
        for (int i = 0; i < s.length(); i += 4) {
            final int c0 = Base64Codec.DECODE_TABLE[s.charAt(i)];
            final int c2 = Base64Codec.DECODE_TABLE[s.charAt(i + 1)];
            buffer[pos++] = (byte)((c0 << 2 | c2 >> 4) & mask);
            if (pos >= buffer.length) {
                return buffer;
            }
            final int c3 = Base64Codec.DECODE_TABLE[s.charAt(i + 2)];
            buffer[pos++] = (byte)((c2 << 4 | c3 >> 2) & mask);
            if (pos >= buffer.length) {
                return buffer;
            }
            final int c4 = Base64Codec.DECODE_TABLE[s.charAt(i + 3)];
            buffer[pos++] = (byte)((c3 << 6 | c4) & mask);
        }
        return buffer;
    }
    
    public String encode(final byte[] in) {
        int modulus = 0;
        int bitWorkArea = 0;
        final int numEncodedBytes = in.length / 3 * 4 + ((in.length % 3 == 0) ? 0 : 4);
        final byte[] buffer = new byte[numEncodedBytes];
        int pos = 0;
        for (final int b : in) {
            modulus = (modulus + 1) % 3;
            if (b < 0) {
                b += 256;
            }
            bitWorkArea = (bitWorkArea << 8) + b;
            if (0 == modulus) {
                buffer[pos++] = Base64Codec.ENCODE_TABLE[bitWorkArea >> 18 & 0x3F];
                buffer[pos++] = Base64Codec.ENCODE_TABLE[bitWorkArea >> 12 & 0x3F];
                buffer[pos++] = Base64Codec.ENCODE_TABLE[bitWorkArea >> 6 & 0x3F];
                buffer[pos++] = Base64Codec.ENCODE_TABLE[bitWorkArea & 0x3F];
            }
        }
        switch (modulus) {
            case 1: {
                buffer[pos++] = Base64Codec.ENCODE_TABLE[bitWorkArea >> 2 & 0x3F];
                buffer[pos++] = Base64Codec.ENCODE_TABLE[bitWorkArea << 4 & 0x3F];
                buffer[pos] = (buffer[pos++] = 61);
                break;
            }
            case 2: {
                buffer[pos++] = Base64Codec.ENCODE_TABLE[bitWorkArea >> 10 & 0x3F];
                buffer[pos++] = Base64Codec.ENCODE_TABLE[bitWorkArea >> 4 & 0x3F];
                buffer[pos++] = Base64Codec.ENCODE_TABLE[bitWorkArea << 2 & 0x3F];
                buffer[pos] = 61;
                break;
            }
        }
        try {
            return new String(buffer, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new MongoException("UTF-8 Charset is not available");
        }
    }
    
    static {
        ENCODE_TABLE = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };
        DECODE_TABLE = new int[128];
        for (int i = 0; i < Base64Codec.ENCODE_TABLE.length; ++i) {
            Base64Codec.DECODE_TABLE[Base64Codec.ENCODE_TABLE[i]] = i;
        }
    }
}
