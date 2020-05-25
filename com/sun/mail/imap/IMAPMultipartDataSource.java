package com.sun.mail.imap;

import java.util.*;
import javax.mail.internet.*;
import com.sun.mail.imap.protocol.*;
import javax.mail.*;

public class IMAPMultipartDataSource extends MimePartDataSource implements MultipartDataSource
{
    private Vector parts;
    
    protected IMAPMultipartDataSource(final MimePart part, final BODYSTRUCTURE[] bs, final String sectionId, final IMAPMessage msg) {
        super(part);
        this.parts = new Vector(bs.length);
        for (int i = 0; i < bs.length; ++i) {
            this.parts.addElement(new IMAPBodyPart(bs[i], (sectionId == null) ? Integer.toString(i + 1) : (sectionId + "." + Integer.toString(i + 1)), msg));
        }
    }
    
    public int getCount() {
        return this.parts.size();
    }
    
    public BodyPart getBodyPart(final int index) throws MessagingException {
        return this.parts.elementAt(index);
    }
}
