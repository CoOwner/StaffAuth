package com.sun.mail.util;

import java.io.*;

public class QEncoderStream extends QPEncoderStream
{
    private String specials;
    private static String WORD_SPECIALS;
    private static String TEXT_SPECIALS;
    
    public QEncoderStream(final OutputStream out, final boolean encodingWord) {
        super(out, Integer.MAX_VALUE);
        this.specials = (encodingWord ? QEncoderStream.WORD_SPECIALS : QEncoderStream.TEXT_SPECIALS);
    }
    
    public void write(int c) throws IOException {
        c &= 0xFF;
        if (c == 32) {
            this.output(95, false);
        }
        else if (c < 32 || c >= 127 || this.specials.indexOf(c) >= 0) {
            this.output(c, true);
        }
        else {
            this.output(c, false);
        }
    }
    
    public static int encodedLength(final byte[] b, final boolean encodingWord) {
        int len = 0;
        final String specials = encodingWord ? QEncoderStream.WORD_SPECIALS : QEncoderStream.TEXT_SPECIALS;
        for (int i = 0; i < b.length; ++i) {
            final int c = b[i] & 0xFF;
            if (c < 32 || c >= 127 || specials.indexOf(c) >= 0) {
                len += 3;
            }
            else {
                ++len;
            }
        }
        return len;
    }
    
    static {
        QEncoderStream.WORD_SPECIALS = "=_?\"#$%&'(),.:;<>@[\\]^`{|}~";
        QEncoderStream.TEXT_SPECIALS = "=_?";
    }
}
