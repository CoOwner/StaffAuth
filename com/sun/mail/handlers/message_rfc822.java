package com.sun.mail.handlers;

import java.awt.datatransfer.*;
import javax.activation.*;
import java.util.*;
import javax.mail.internet.*;
import java.io.*;
import javax.mail.*;

public class message_rfc822 implements DataContentHandler
{
    ActivationDataFlavor ourDataFlavor;
    
    public message_rfc822() {
        this.ourDataFlavor = new ActivationDataFlavor(Message.class, "message/rfc822", "Message");
    }
    
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { this.ourDataFlavor };
    }
    
    public Object getTransferData(final DataFlavor df, final DataSource ds) throws IOException {
        if (this.ourDataFlavor.equals(df)) {
            return this.getContent(ds);
        }
        return null;
    }
    
    public Object getContent(final DataSource ds) throws IOException {
        try {
            Session session;
            if (ds instanceof MessageAware) {
                final MessageContext mc = ((MessageAware)ds).getMessageContext();
                session = mc.getSession();
            }
            else {
                session = Session.getDefaultInstance(new Properties(), null);
            }
            return new MimeMessage(session, ds.getInputStream());
        }
        catch (MessagingException me) {
            throw new IOException("Exception creating MimeMessage in message/rfc822 DataContentHandler: " + me.toString());
        }
    }
    
    public void writeTo(final Object obj, final String mimeType, final OutputStream os) throws IOException {
        if (obj instanceof Message) {
            final Message m = (Message)obj;
            try {
                m.writeTo(os);
            }
            catch (MessagingException me) {
                throw new IOException(me.toString());
            }
            return;
        }
        throw new IOException("unsupported object");
    }
}
