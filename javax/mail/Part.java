package javax.mail;

import javax.activation.*;
import java.io.*;
import java.util.*;

public interface Part
{
    public static final String ATTACHMENT = "attachment";
    public static final String INLINE = "inline";
    
    int getSize() throws MessagingException;
    
    int getLineCount() throws MessagingException;
    
    String getContentType() throws MessagingException;
    
    boolean isMimeType(final String p0) throws MessagingException;
    
    String getDisposition() throws MessagingException;
    
    void setDisposition(final String p0) throws MessagingException;
    
    String getDescription() throws MessagingException;
    
    void setDescription(final String p0) throws MessagingException;
    
    String getFileName() throws MessagingException;
    
    void setFileName(final String p0) throws MessagingException;
    
    InputStream getInputStream() throws IOException, MessagingException;
    
    DataHandler getDataHandler() throws MessagingException;
    
    Object getContent() throws IOException, MessagingException;
    
    void setDataHandler(final DataHandler p0) throws MessagingException;
    
    void setContent(final Object p0, final String p1) throws MessagingException;
    
    void setText(final String p0) throws MessagingException;
    
    void setContent(final Multipart p0) throws MessagingException;
    
    void writeTo(final OutputStream p0) throws IOException, MessagingException;
    
    String[] getHeader(final String p0) throws MessagingException;
    
    void setHeader(final String p0, final String p1) throws MessagingException;
    
    void addHeader(final String p0, final String p1) throws MessagingException;
    
    void removeHeader(final String p0) throws MessagingException;
    
    Enumeration getAllHeaders() throws MessagingException;
    
    Enumeration getMatchingHeaders(final String[] p0) throws MessagingException;
    
    Enumeration getNonMatchingHeaders(final String[] p0) throws MessagingException;
}
