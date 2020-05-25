package com.sun.mail.util;

import java.io.*;
import javax.mail.*;

public class LineOutputStream extends FilterOutputStream
{
    private static byte[] newline;
    
    public LineOutputStream(final OutputStream out) {
        super(out);
    }
    
    public void writeln(final String s) throws MessagingException {
        try {
            final byte[] bytes = ASCIIUtility.getBytes(s);
            this.out.write(bytes);
            this.out.write(LineOutputStream.newline);
        }
        catch (Exception ex) {
            throw new MessagingException("IOException", ex);
        }
    }
    
    public void writeln() throws MessagingException {
        try {
            this.out.write(LineOutputStream.newline);
        }
        catch (Exception ex) {
            throw new MessagingException("IOException", ex);
        }
    }
    
    static {
        (LineOutputStream.newline = new byte[2])[0] = 13;
        LineOutputStream.newline[1] = 10;
    }
}
