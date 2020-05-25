package com.sun.mail.handlers;

import java.awt.datatransfer.*;
import javax.activation.*;
import java.io.*;
import javax.mail.internet.*;

public class text_plain implements DataContentHandler
{
    private static ActivationDataFlavor myDF;
    
    protected ActivationDataFlavor getDF() {
        return text_plain.myDF;
    }
    
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { this.getDF() };
    }
    
    public Object getTransferData(final DataFlavor df, final DataSource ds) throws IOException {
        if (this.getDF().equals(df)) {
            return this.getContent(ds);
        }
        return null;
    }
    
    public Object getContent(final DataSource ds) throws IOException {
        String enc = null;
        InputStreamReader is = null;
        try {
            enc = this.getCharset(ds.getContentType());
            is = new InputStreamReader(ds.getInputStream(), enc);
        }
        catch (IllegalArgumentException iex) {
            throw new UnsupportedEncodingException(enc);
        }
        int pos;
        char[] buf;
        int count;
        int size;
        char[] tbuf = null;
        for (pos = 0, buf = new char[1024]; (count = is.read(buf, pos, buf.length - pos)) != -1; buf = tbuf) {
            pos += count;
            if (pos >= buf.length) {
                size = buf.length;
                if (size < 262144) {
                    size += size;
                }
                else {
                    size += 262144;
                }
                tbuf = new char[size];
                System.arraycopy(buf, 0, tbuf, 0, pos);
            }
        }
        return new String(buf, 0, pos);
    }
    
    public void writeTo(final Object obj, final String type, final OutputStream os) throws IOException {
        if (!(obj instanceof String)) {
            throw new IOException("\"" + this.getDF().getMimeType() + "\" DataContentHandler requires String object, " + "was given object of type " + obj.getClass().toString());
        }
        String enc = null;
        OutputStreamWriter osw = null;
        try {
            enc = this.getCharset(type);
            osw = new OutputStreamWriter(os, enc);
        }
        catch (IllegalArgumentException iex) {
            throw new UnsupportedEncodingException(enc);
        }
        final String s = (String)obj;
        osw.write(s, 0, s.length());
        osw.flush();
    }
    
    private String getCharset(final String type) {
        try {
            final ContentType ct = new ContentType(type);
            String charset = ct.getParameter("charset");
            if (charset == null) {
                charset = "us-ascii";
            }
            return MimeUtility.javaCharset(charset);
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    static {
        text_plain.myDF = new ActivationDataFlavor(String.class, "text/plain", "Text String");
    }
}
