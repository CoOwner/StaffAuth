package com.sun.mail.imap;

import com.sun.mail.iap.*;
import com.sun.mail.imap.protocol.*;
import javax.activation.*;
import javax.mail.*;
import java.util.*;
import javax.mail.internet.*;
import java.io.*;

public class IMAPBodyPart extends MimeBodyPart
{
    private IMAPMessage message;
    private BODYSTRUCTURE bs;
    private String sectionId;
    private String type;
    private String description;
    private boolean headersLoaded;
    
    protected IMAPBodyPart(final BODYSTRUCTURE bs, final String sid, final IMAPMessage message) {
        this.headersLoaded = false;
        this.bs = bs;
        this.sectionId = sid;
        this.message = message;
        final ContentType ct = new ContentType(bs.type, bs.subtype, bs.cParams);
        this.type = ct.toString();
    }
    
    protected void updateHeaders() {
    }
    
    public int getSize() throws MessagingException {
        return this.bs.size;
    }
    
    public int getLineCount() throws MessagingException {
        return this.bs.lines;
    }
    
    public String getContentType() throws MessagingException {
        return this.type;
    }
    
    public String getDisposition() throws MessagingException {
        return this.bs.disposition;
    }
    
    public void setDisposition(final String disposition) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }
    
    public String getEncoding() throws MessagingException {
        return this.bs.encoding;
    }
    
    public String getContentID() throws MessagingException {
        return this.bs.id;
    }
    
    public String getContentMD5() throws MessagingException {
        return this.bs.md5;
    }
    
    public void setContentMD5(final String md5) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }
    
    public String getDescription() throws MessagingException {
        if (this.description != null) {
            return this.description;
        }
        if (this.bs.description == null) {
            return null;
        }
        try {
            this.description = MimeUtility.decodeText(this.bs.description);
        }
        catch (UnsupportedEncodingException ex) {
            this.description = this.bs.description;
        }
        return this.description;
    }
    
    public void setDescription(final String description, final String charset) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }
    
    public String getFileName() throws MessagingException {
        String filename = null;
        if (this.bs.dParams != null) {
            filename = this.bs.dParams.get("filename");
        }
        if (filename == null && this.bs.cParams != null) {
            filename = this.bs.cParams.get("name");
        }
        return filename;
    }
    
    public void setFileName(final String filename) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }
    
    protected InputStream getContentStream() throws MessagingException {
        InputStream is = null;
        final boolean pk = this.message.getPeek();
        synchronized (this.message.getMessageCacheLock()) {
            final IMAPProtocol p = this.message.getProtocol();
            if (p.isREV1() && this.message.getFetchBlockSize() != -1) {
                return new IMAPInputStream(this.message, this.sectionId, this.bs.size, pk);
            }
            this.message.checkExpunged();
            final int seqnum = this.message.getSequenceNumber();
            try {
                BODY b;
                if (this.message.getPeek()) {
                    b = p.peekBody(seqnum, this.sectionId);
                }
                else {
                    b = p.fetchBody(seqnum, this.sectionId);
                }
                if (b != null) {
                    is = b.getByteArrayInputStream();
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.message.getFolder(), cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is == null) {
            throw new MessagingException("No content");
        }
        return is;
    }
    
    public synchronized DataHandler getDataHandler() throws MessagingException {
        if (this.dh == null) {
            if (this.bs.isMulti()) {
                this.dh = new DataHandler(new IMAPMultipartDataSource(this, this.bs.bodies, this.sectionId, this.message));
            }
            else if (this.bs.isNested() && this.message.getProtocol().isREV1()) {
                this.dh = new DataHandler(new IMAPNestedMessage(this.message, this.bs.bodies[0], this.bs.envelope, this.sectionId), this.type);
            }
        }
        return super.getDataHandler();
    }
    
    public void setDataHandler(final DataHandler content) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }
    
    public void setContent(final Object o, final String type) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }
    
    public void setContent(final Multipart mp) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }
    
    public String[] getHeader(final String name) throws MessagingException {
        this.loadHeaders();
        return super.getHeader(name);
    }
    
    public void setHeader(final String name, final String value) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }
    
    public void addHeader(final String name, final String value) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }
    
    public void removeHeader(final String name) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }
    
    public Enumeration getAllHeaders() throws MessagingException {
        this.loadHeaders();
        return super.getAllHeaders();
    }
    
    public Enumeration getMatchingHeaders(final String[] names) throws MessagingException {
        this.loadHeaders();
        return super.getMatchingHeaders(names);
    }
    
    public Enumeration getNonMatchingHeaders(final String[] names) throws MessagingException {
        this.loadHeaders();
        return super.getNonMatchingHeaders(names);
    }
    
    public void addHeaderLine(final String line) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }
    
    public Enumeration getAllHeaderLines() throws MessagingException {
        this.loadHeaders();
        return super.getAllHeaderLines();
    }
    
    public Enumeration getMatchingHeaderLines(final String[] names) throws MessagingException {
        this.loadHeaders();
        return super.getMatchingHeaderLines(names);
    }
    
    public Enumeration getNonMatchingHeaderLines(final String[] names) throws MessagingException {
        this.loadHeaders();
        return super.getNonMatchingHeaderLines(names);
    }
    
    private synchronized void loadHeaders() throws MessagingException {
        if (this.headersLoaded) {
            return;
        }
        if (this.headers == null) {
            this.headers = new InternetHeaders();
        }
        IMAPProtocol p = this.message.getProtocol();
        if (p.isREV1()) {
            BODY b = null;
            synchronized (this.message.getMessageCacheLock()) {
                p = this.message.getProtocol();
                this.message.checkExpunged();
                final int seqnum = this.message.getSequenceNumber();
                try {
                    b = p.peekBody(seqnum, this.sectionId + ".MIME");
                }
                catch (ConnectionException cex) {
                    throw new FolderClosedException(this.message.getFolder(), cex.getMessage());
                }
                catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            if (b == null) {
                throw new MessagingException("Failed to fetch headers");
            }
            final ByteArrayInputStream bis = b.getByteArrayInputStream();
            if (bis == null) {
                throw new MessagingException("Failed to fetch headers");
            }
            this.headers.load(bis);
        }
        else {
            this.headers.addHeader("Content-Type", this.type);
            this.headers.addHeader("Content-Transfer-Encoding", this.bs.encoding);
            if (this.bs.description != null) {
                this.headers.addHeader("Content-Description", this.bs.description);
            }
            if (this.bs.id != null) {
                this.headers.addHeader("Content-ID", this.bs.id);
            }
            if (this.bs.md5 != null) {
                this.headers.addHeader("Content-MD5", this.bs.md5);
            }
        }
        this.headersLoaded = true;
    }
}
