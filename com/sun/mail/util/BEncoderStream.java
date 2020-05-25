package com.sun.mail.util;

import java.io.*;

public class BEncoderStream extends BASE64EncoderStream
{
    public BEncoderStream(final OutputStream out) {
        super(out, Integer.MAX_VALUE);
    }
    
    public static int encodedLength(final byte[] b) {
        return (b.length + 2) / 3 * 4;
    }
}
