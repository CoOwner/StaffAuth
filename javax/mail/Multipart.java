package javax.mail;

import java.util.*;
import java.io.*;

public abstract class Multipart
{
    protected Vector parts;
    protected String contentType;
    protected Part parent;
    
    protected Multipart() {
        this.parts = new Vector();
        this.contentType = "multipart/mixed";
    }
    
    protected void setMultipartDataSource(final MultipartDataSource mp) throws MessagingException {
        this.contentType = mp.getContentType();
        for (int count = mp.getCount(), i = 0; i < count; ++i) {
            this.addBodyPart(mp.getBodyPart(i));
        }
    }
    
    public String getContentType() {
        return this.contentType;
    }
    
    public int getCount() throws MessagingException {
        if (this.parts == null) {
            return 0;
        }
        return this.parts.size();
    }
    
    public BodyPart getBodyPart(final int index) throws MessagingException {
        if (this.parts == null) {
            throw new IndexOutOfBoundsException("No such BodyPart");
        }
        return this.parts.elementAt(index);
    }
    
    public boolean removeBodyPart(final BodyPart part) throws MessagingException {
        if (this.parts == null) {
            throw new MessagingException("No such body part");
        }
        final boolean ret = this.parts.removeElement(part);
        part.setParent(null);
        return ret;
    }
    
    public void removeBodyPart(final int index) throws MessagingException {
        if (this.parts == null) {
            throw new IndexOutOfBoundsException("No such BodyPart");
        }
        final BodyPart part = this.parts.elementAt(index);
        this.parts.removeElementAt(index);
        part.setParent(null);
    }
    
    public synchronized void addBodyPart(final BodyPart part) throws MessagingException {
        if (this.parts == null) {
            this.parts = new Vector();
        }
        this.parts.addElement(part);
        part.setParent(this);
    }
    
    public synchronized void addBodyPart(final BodyPart part, final int index) throws MessagingException {
        if (this.parts == null) {
            this.parts = new Vector();
        }
        this.parts.insertElementAt(part, index);
        part.setParent(this);
    }
    
    public abstract void writeTo(final OutputStream p0) throws IOException, MessagingException;
    
    public Part getParent() {
        return this.parent;
    }
    
    public void setParent(final Part parent) {
        this.parent = parent;
    }
}
