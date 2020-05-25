package javax.mail.internet;

import javax.mail.*;
import java.util.*;

public interface MimePart extends Part
{
    String getHeader(final String p0, final String p1) throws MessagingException;
    
    void addHeaderLine(final String p0) throws MessagingException;
    
    Enumeration getAllHeaderLines() throws MessagingException;
    
    Enumeration getMatchingHeaderLines(final String[] p0) throws MessagingException;
    
    Enumeration getNonMatchingHeaderLines(final String[] p0) throws MessagingException;
    
    String getEncoding() throws MessagingException;
    
    String getContentID() throws MessagingException;
    
    String getContentMD5() throws MessagingException;
    
    void setContentMD5(final String p0) throws MessagingException;
    
    String[] getContentLanguage() throws MessagingException;
    
    void setContentLanguage(final String[] p0) throws MessagingException;
    
    void setText(final String p0) throws MessagingException;
    
    void setText(final String p0, final String p1) throws MessagingException;
    
    void setText(final String p0, final String p1, final String p2) throws MessagingException;
}
