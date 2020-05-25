package com.sun.mail.handlers;

import java.awt.datatransfer.*;
import javax.activation.*;
import javax.mail.internet.*;
import javax.mail.*;
import java.io.*;

public class multipart_mixed implements DataContentHandler
{
    private ActivationDataFlavor myDF;
    
    public multipart_mixed() {
        this.myDF = new ActivationDataFlavor(MimeMultipart.class, "multipart/mixed", "Multipart");
    }
    
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { this.myDF };
    }
    
    public Object getTransferData(final DataFlavor df, final DataSource ds) {
        if (this.myDF.equals(df)) {
            return this.getContent(ds);
        }
        return null;
    }
    
    public Object getContent(final DataSource ds) {
        try {
            return new MimeMultipart(ds);
        }
        catch (MessagingException e) {
            return null;
        }
    }
    
    public void writeTo(final Object obj, final String mimeType, final OutputStream os) throws IOException {
        if (obj instanceof MimeMultipart) {
            try {
                ((MimeMultipart)obj).writeTo(os);
            }
            catch (MessagingException e) {
                throw new IOException(e.toString());
            }
        }
    }
}
