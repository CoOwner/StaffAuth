package com.sun.mail.imap;

import javax.activation.*;
import java.io.*;
import java.util.*;
import javax.mail.*;
import com.sun.mail.iap.*;
import com.sun.mail.imap.protocol.*;
import javax.mail.internet.*;

public class IMAPMessage extends MimeMessage
{
    protected BODYSTRUCTURE bs;
    protected ENVELOPE envelope;
    private Date receivedDate;
    private int size;
    private boolean peek;
    private int seqnum;
    private long uid;
    protected String sectionId;
    private String type;
    private String subject;
    private String description;
    private boolean headersLoaded;
    private Hashtable loadedHeaders;
    private static String EnvelopeCmd;
    
    protected IMAPMessage(final IMAPFolder folder, final int msgnum, final int seqnum) {
        super(folder, msgnum);
        this.size = -1;
        this.uid = -1L;
        this.headersLoaded = false;
        this.seqnum = seqnum;
        this.flags = null;
    }
    
    protected IMAPMessage(final Session session) {
        super(session);
        this.size = -1;
        this.uid = -1L;
        this.headersLoaded = false;
    }
    
    protected IMAPProtocol getProtocol() throws FolderClosedException {
        final IMAPProtocol p = ((IMAPFolder)this.folder).protocol;
        if (p == null) {
            throw new FolderClosedException(this.folder);
        }
        return p;
    }
    
    protected Object getMessageCacheLock() {
        return ((IMAPFolder)this.folder).messageCacheLock;
    }
    
    protected int getSequenceNumber() {
        return this.seqnum;
    }
    
    protected void setSequenceNumber(final int seqnum) {
        this.seqnum = seqnum;
    }
    
    protected void setMessageNumber(final int msgnum) {
        super.setMessageNumber(msgnum);
    }
    
    protected long getUID() {
        return this.uid;
    }
    
    protected void setUID(final long uid) {
        this.uid = uid;
    }
    
    protected void setExpunged(final boolean set) {
        super.setExpunged(set);
        this.seqnum = -1;
    }
    
    protected void checkExpunged() throws MessageRemovedException {
        if (this.expunged) {
            throw new MessageRemovedException();
        }
    }
    
    protected void forceCheckExpunged() throws MessageRemovedException, FolderClosedException {
        synchronized (this.getMessageCacheLock()) {
            try {
                this.getProtocol().noop();
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException ex) {}
        }
        if (this.expunged) {
            throw new MessageRemovedException();
        }
    }
    
    protected int getFetchBlockSize() {
        return ((IMAPStore)this.folder.getStore()).getFetchBlockSize();
    }
    
    public Address[] getFrom() throws MessagingException {
        this.checkExpunged();
        this.loadEnvelope();
        return this.aaclone(this.envelope.from);
    }
    
    public void setFrom(final Address address) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public void addFrom(final Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public Address getSender() throws MessagingException {
        this.checkExpunged();
        this.loadEnvelope();
        if (this.envelope.sender != null) {
            return this.envelope.sender[0];
        }
        return null;
    }
    
    public void setSender(final Address address) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public Address[] getRecipients(final Message.RecipientType type) throws MessagingException {
        this.checkExpunged();
        this.loadEnvelope();
        if (type == Message.RecipientType.TO) {
            return this.aaclone(this.envelope.to);
        }
        if (type == Message.RecipientType.CC) {
            return this.aaclone(this.envelope.cc);
        }
        if (type == Message.RecipientType.BCC) {
            return this.aaclone(this.envelope.bcc);
        }
        return super.getRecipients(type);
    }
    
    public void setRecipients(final Message.RecipientType type, final Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public void addRecipients(final Message.RecipientType type, final Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public Address[] getReplyTo() throws MessagingException {
        this.checkExpunged();
        this.loadEnvelope();
        return this.aaclone(this.envelope.replyTo);
    }
    
    public void setReplyTo(final Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public String getSubject() throws MessagingException {
        this.checkExpunged();
        if (this.subject != null) {
            return this.subject;
        }
        this.loadEnvelope();
        if (this.envelope.subject == null) {
            return null;
        }
        try {
            this.subject = MimeUtility.decodeText(this.envelope.subject);
        }
        catch (UnsupportedEncodingException ex) {
            this.subject = this.envelope.subject;
        }
        return this.subject;
    }
    
    public void setSubject(final String subject, final String charset) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public Date getSentDate() throws MessagingException {
        this.checkExpunged();
        this.loadEnvelope();
        if (this.envelope.date == null) {
            return null;
        }
        return new Date(this.envelope.date.getTime());
    }
    
    public void setSentDate(final Date d) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public Date getReceivedDate() throws MessagingException {
        this.checkExpunged();
        this.loadEnvelope();
        if (this.receivedDate == null) {
            return null;
        }
        return new Date(this.receivedDate.getTime());
    }
    
    public int getSize() throws MessagingException {
        this.checkExpunged();
        if (this.size == -1) {
            this.loadEnvelope();
        }
        return this.size;
    }
    
    public int getLineCount() throws MessagingException {
        this.checkExpunged();
        this.loadBODYSTRUCTURE();
        return this.bs.lines;
    }
    
    public String[] getContentLangauge() throws MessagingException {
        this.checkExpunged();
        this.loadBODYSTRUCTURE();
        if (this.bs.language != null) {
            return this.bs.language.clone();
        }
        return null;
    }
    
    public void setContentLanguage(final String[] languages) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public String getInReplyTo() throws MessagingException {
        this.checkExpunged();
        this.loadEnvelope();
        return this.envelope.inReplyTo;
    }
    
    public String getContentType() throws MessagingException {
        this.checkExpunged();
        if (this.type == null) {
            this.loadBODYSTRUCTURE();
            final ContentType ct = new ContentType(this.bs.type, this.bs.subtype, this.bs.cParams);
            this.type = ct.toString();
        }
        return this.type;
    }
    
    public String getDisposition() throws MessagingException {
        this.checkExpunged();
        this.loadBODYSTRUCTURE();
        return this.bs.disposition;
    }
    
    public void setDisposition(final String disposition) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public String getEncoding() throws MessagingException {
        this.checkExpunged();
        this.loadBODYSTRUCTURE();
        return this.bs.encoding;
    }
    
    public String getContentID() throws MessagingException {
        this.checkExpunged();
        this.loadBODYSTRUCTURE();
        return this.bs.id;
    }
    
    public void setContentID(final String cid) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public String getContentMD5() throws MessagingException {
        this.checkExpunged();
        this.loadBODYSTRUCTURE();
        return this.bs.md5;
    }
    
    public void setContentMD5(final String md5) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public String getDescription() throws MessagingException {
        this.checkExpunged();
        if (this.description != null) {
            return this.description;
        }
        this.loadBODYSTRUCTURE();
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
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public String getMessageID() throws MessagingException {
        this.checkExpunged();
        this.loadEnvelope();
        return this.envelope.messageId;
    }
    
    public String getFileName() throws MessagingException {
        this.checkExpunged();
        String filename = null;
        this.loadBODYSTRUCTURE();
        if (this.bs.dParams != null) {
            filename = this.bs.dParams.get("filename");
        }
        if (filename == null && this.bs.cParams != null) {
            filename = this.bs.cParams.get("name");
        }
        return filename;
    }
    
    public void setFileName(final String filename) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    protected InputStream getContentStream() throws MessagingException {
        InputStream is = null;
        final boolean pk = this.getPeek();
        synchronized (this.getMessageCacheLock()) {
            final IMAPProtocol p = this.getProtocol();
            if (p.isREV1() && this.getFetchBlockSize() != -1) {
                return new IMAPInputStream(this, this.toSection("TEXT"), (this.bs != null) ? this.bs.size : -1, pk);
            }
            this.checkExpunged();
            try {
                if (p.isREV1()) {
                    BODY b;
                    if (pk) {
                        b = p.peekBody(this.getSequenceNumber(), this.toSection("TEXT"));
                    }
                    else {
                        b = p.fetchBody(this.getSequenceNumber(), this.toSection("TEXT"));
                    }
                    if (b != null) {
                        is = b.getByteArrayInputStream();
                    }
                }
                else {
                    final RFC822DATA rd = p.fetchRFC822(this.getSequenceNumber(), "TEXT");
                    if (rd != null) {
                        is = rd.getByteArrayInputStream();
                    }
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is == null) {
            throw new MessagingException("No content");
        }
        return is;
    }
    
    public synchronized DataHandler getDataHandler() throws MessagingException {
        this.checkExpunged();
        if (this.dh == null) {
            this.loadBODYSTRUCTURE();
            if (this.type == null) {
                final ContentType ct = new ContentType(this.bs.type, this.bs.subtype, this.bs.cParams);
                this.type = ct.toString();
            }
            if (this.bs.isMulti()) {
                this.dh = new DataHandler(new IMAPMultipartDataSource(this, this.bs.bodies, this.sectionId, this));
            }
            else if (this.bs.isNested() && this.getProtocol().isREV1()) {
                this.dh = new DataHandler(new IMAPNestedMessage(this, this.bs.bodies[0], this.bs.envelope, (this.sectionId == null) ? "1" : (this.sectionId + ".1")), this.type);
            }
        }
        return super.getDataHandler();
    }
    
    public void setDataHandler(final DataHandler content) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public void writeTo(final OutputStream os) throws IOException, MessagingException {
        InputStream is = null;
        final boolean pk = this.getPeek();
        synchronized (this.getMessageCacheLock()) {
            this.checkExpunged();
            final IMAPProtocol p = this.getProtocol();
            try {
                if (p.isREV1()) {
                    BODY b;
                    if (pk) {
                        b = p.peekBody(this.getSequenceNumber(), this.sectionId);
                    }
                    else {
                        b = p.fetchBody(this.getSequenceNumber(), this.sectionId);
                    }
                    if (b != null) {
                        is = b.getByteArrayInputStream();
                    }
                }
                else {
                    final RFC822DATA rd = p.fetchRFC822(this.getSequenceNumber(), null);
                    if (rd != null) {
                        is = rd.getByteArrayInputStream();
                    }
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is == null) {
            throw new MessagingException("No content");
        }
        final byte[] bytes = new byte[1024];
        int count;
        while ((count = is.read(bytes)) != -1) {
            os.write(bytes, 0, count);
        }
    }
    
    public String[] getHeader(final String name) throws MessagingException {
        this.checkExpunged();
        if (this.isHeaderLoaded(name)) {
            return this.headers.getHeader(name);
        }
        InputStream is = null;
        synchronized (this.getMessageCacheLock()) {
            this.checkExpunged();
            final IMAPProtocol p = this.getProtocol();
            try {
                if (p.isREV1()) {
                    final BODY b = p.peekBody(this.getSequenceNumber(), this.toSection("HEADER.FIELDS (" + name + ")"));
                    if (b != null) {
                        is = b.getByteArrayInputStream();
                    }
                }
                else {
                    final RFC822DATA rd = p.fetchRFC822(this.getSequenceNumber(), "HEADER.LINES (" + name + ")");
                    if (rd != null) {
                        is = rd.getByteArrayInputStream();
                    }
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is == null) {
            return null;
        }
        if (this.headers == null) {
            this.headers = new InternetHeaders();
        }
        this.headers.load(is);
        this.setHeaderLoaded(name);
        return this.headers.getHeader(name);
    }
    
    public String getHeader(final String name, final String delimiter) throws MessagingException {
        this.checkExpunged();
        if (this.getHeader(name) == null) {
            return null;
        }
        return this.headers.getHeader(name, delimiter);
    }
    
    public void setHeader(final String name, final String value) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public void addHeader(final String name, final String value) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public void removeHeader(final String name) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public Enumeration getAllHeaders() throws MessagingException {
        this.checkExpunged();
        this.loadHeaders();
        return super.getAllHeaders();
    }
    
    public Enumeration getMatchingHeaders(final String[] names) throws MessagingException {
        this.checkExpunged();
        this.loadHeaders();
        return super.getMatchingHeaders(names);
    }
    
    public Enumeration getNonMatchingHeaders(final String[] names) throws MessagingException {
        this.checkExpunged();
        this.loadHeaders();
        return super.getNonMatchingHeaders(names);
    }
    
    public void addHeaderLine(final String line) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }
    
    public Enumeration getAllHeaderLines() throws MessagingException {
        this.checkExpunged();
        this.loadHeaders();
        return super.getAllHeaderLines();
    }
    
    public Enumeration getMatchingHeaderLines(final String[] names) throws MessagingException {
        this.checkExpunged();
        this.loadHeaders();
        return super.getMatchingHeaderLines(names);
    }
    
    public Enumeration getNonMatchingHeaderLines(final String[] names) throws MessagingException {
        this.checkExpunged();
        this.loadHeaders();
        return super.getNonMatchingHeaderLines(names);
    }
    
    public synchronized Flags getFlags() throws MessagingException {
        this.checkExpunged();
        this.loadFlags();
        return super.getFlags();
    }
    
    public synchronized boolean isSet(final Flags.Flag flag) throws MessagingException {
        this.checkExpunged();
        this.loadFlags();
        return super.isSet(flag);
    }
    
    public synchronized void setFlags(final Flags flag, final boolean set) throws MessagingException {
        synchronized (this.getMessageCacheLock()) {
            this.checkExpunged();
            try {
                this.getProtocol().storeFlags(this.getSequenceNumber(), flag, set);
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
    }
    
    public synchronized void setPeek(final boolean peek) {
        this.peek = peek;
    }
    
    public synchronized boolean getPeek() {
        return this.peek;
    }
    
    public synchronized void invalidateHeaders() {
        this.headersLoaded = false;
        this.loadedHeaders = null;
        this.envelope = null;
        this.bs = null;
        this.receivedDate = null;
        this.size = -1;
        this.type = null;
        this.subject = null;
        this.description = null;
    }
    
    static void fetch(final IMAPFolder folder, final Message[] msgs, final FetchProfile fp) throws MessagingException {
        final StringBuffer command = new StringBuffer();
        boolean first = true;
        boolean allHeaders = false;
        if (fp.contains(FetchProfile.Item.ENVELOPE)) {
            command.append(IMAPMessage.EnvelopeCmd);
            first = false;
        }
        if (fp.contains(FetchProfile.Item.FLAGS)) {
            command.append(first ? "FLAGS" : " FLAGS");
            first = false;
        }
        if (fp.contains(FetchProfile.Item.CONTENT_INFO)) {
            command.append(first ? "BODYSTRUCTURE" : " BODYSTRUCTURE");
            first = false;
        }
        if (fp.contains(UIDFolder.FetchProfileItem.UID)) {
            command.append(first ? "UID" : " UID");
            first = false;
        }
        if (fp.contains(IMAPFolder.FetchProfileItem.HEADERS)) {
            allHeaders = true;
            if (folder.protocol.isREV1()) {
                command.append(first ? "BODY.PEEK[HEADER]" : " BODY.PEEK[HEADER]");
            }
            else {
                command.append(first ? "RFC822.HEADER" : " RFC822.HEADER");
            }
            first = false;
        }
        if (fp.contains(IMAPFolder.FetchProfileItem.SIZE)) {
            command.append(first ? "RFC822.SIZE" : " RFC822.SIZE");
            first = false;
        }
        String[] hdrs = null;
        if (!allHeaders) {
            hdrs = fp.getHeaderNames();
            if (hdrs.length > 0) {
                if (!first) {
                    command.append(" ");
                }
                command.append(craftHeaderCmd(folder.protocol, hdrs));
            }
        }
        final Utility.Condition condition = new FetchProfileCondition(fp);
        synchronized (folder.messageCacheLock) {
            final MessageSet[] msgsets = Utility.toMessageSet(msgs, condition);
            if (msgsets == null) {
                return;
            }
            Response[] r = null;
            final Vector v = new Vector();
            try {
                r = folder.protocol.fetch(msgsets, command.toString());
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(folder, cex.getMessage());
            }
            catch (CommandFailedException cfx) {}
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
            if (r == null) {
                return;
            }
            for (int i = 0; i < r.length; ++i) {
                if (r[i] != null) {
                    if (!(r[i] instanceof FetchResponse)) {
                        v.addElement(r[i]);
                    }
                    else {
                        final FetchResponse f = (FetchResponse)r[i];
                        final IMAPMessage msg = folder.getMessageBySeqNumber(f.getNumber());
                        final int count = f.getItemCount();
                        boolean unsolicitedFlags = false;
                        for (int j = 0; j < count; ++j) {
                            final Item item = f.getItem(j);
                            if (item instanceof Flags) {
                                if (!fp.contains(FetchProfile.Item.FLAGS) || msg == null) {
                                    unsolicitedFlags = true;
                                }
                                else {
                                    msg.flags = (Flags)item;
                                }
                            }
                            else if (item instanceof ENVELOPE) {
                                msg.envelope = (ENVELOPE)item;
                            }
                            else if (item instanceof INTERNALDATE) {
                                msg.receivedDate = ((INTERNALDATE)item).getDate();
                            }
                            else if (item instanceof RFC822SIZE) {
                                msg.size = ((RFC822SIZE)item).size;
                            }
                            else if (item instanceof BODYSTRUCTURE) {
                                msg.bs = (BODYSTRUCTURE)item;
                            }
                            else if (item instanceof UID) {
                                final UID u = (UID)item;
                                msg.uid = u.uid;
                                if (folder.uidTable == null) {
                                    folder.uidTable = new Hashtable();
                                }
                                folder.uidTable.put(new Long(u.uid), msg);
                            }
                            else if (item instanceof RFC822DATA || item instanceof BODY) {
                                InputStream headerStream;
                                if (item instanceof RFC822DATA) {
                                    headerStream = ((RFC822DATA)item).getByteArrayInputStream();
                                }
                                else {
                                    headerStream = ((BODY)item).getByteArrayInputStream();
                                }
                                final InternetHeaders h = new InternetHeaders();
                                h.load(headerStream);
                                if (msg.headers == null || allHeaders) {
                                    msg.headers = h;
                                }
                                else {
                                    final Enumeration e = h.getAllHeaders();
                                    while (e.hasMoreElements()) {
                                        final Header he = e.nextElement();
                                        if (!msg.isHeaderLoaded(he.getName())) {
                                            msg.headers.addHeader(he.getName(), he.getValue());
                                        }
                                    }
                                }
                                if (allHeaders) {
                                    msg.headersLoaded = true;
                                }
                                else {
                                    for (int k = 0; k < hdrs.length; ++k) {
                                        msg.setHeaderLoaded(hdrs[k]);
                                    }
                                }
                            }
                        }
                        if (unsolicitedFlags) {
                            v.addElement(f);
                        }
                    }
                }
            }
            final int size = v.size();
            if (size != 0) {
                final Response[] responses = new Response[size];
                v.copyInto(responses);
                folder.handleResponses(responses);
            }
        }
    }
    
    private synchronized void loadEnvelope() throws MessagingException {
        if (this.envelope != null) {
            return;
        }
        final IMAPProtocol p = this.getProtocol();
        Response[] r = null;
        synchronized (this.getMessageCacheLock()) {
            this.checkExpunged();
            final int seqnum = this.getSequenceNumber();
            try {
                r = p.fetch(seqnum, IMAPMessage.EnvelopeCmd);
                for (int i = 0; i < r.length; ++i) {
                    if (r[i] != null && r[i] instanceof FetchResponse) {
                        if (((FetchResponse)r[i]).getNumber() == seqnum) {
                            final FetchResponse f = (FetchResponse)r[i];
                            for (int count = f.getItemCount(), j = 0; j < count; ++j) {
                                final Item item = f.getItem(j);
                                if (item instanceof ENVELOPE) {
                                    this.envelope = (ENVELOPE)item;
                                }
                                else if (item instanceof INTERNALDATE) {
                                    this.receivedDate = ((INTERNALDATE)item).getDate();
                                }
                                else if (item instanceof RFC822SIZE) {
                                    this.size = ((RFC822SIZE)item).size;
                                }
                            }
                        }
                    }
                }
                p.notifyResponseHandlers(r);
                p.handleResult(r[r.length - 1]);
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (this.envelope == null) {
            throw new MessagingException("Failed to load IMAP envelope");
        }
    }
    
    private static String craftHeaderCmd(final IMAPProtocol p, final String[] hdrs) {
        StringBuffer sb;
        if (p.isREV1()) {
            sb = new StringBuffer("BODY.PEEK[HEADER.FIELDS (");
        }
        else {
            sb = new StringBuffer("RFC822.HEADER.LINES (");
        }
        for (int i = 0; i < hdrs.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(hdrs[i]);
        }
        if (p.isREV1()) {
            sb.append(")]");
        }
        else {
            sb.append(")");
        }
        return sb.toString();
    }
    
    private synchronized void loadBODYSTRUCTURE() throws MessagingException {
        if (this.bs != null) {
            return;
        }
        synchronized (this.getMessageCacheLock()) {
            this.checkExpunged();
            try {
                this.bs = this.getProtocol().fetchBodyStructure(this.getSequenceNumber());
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
            if (this.bs == null) {
                throw new MessagingException("Unable to load BODYSTRUCTURE");
            }
        }
    }
    
    private synchronized void loadHeaders() throws MessagingException {
        if (this.headersLoaded) {
            return;
        }
        InputStream is = null;
        synchronized (this.getMessageCacheLock()) {
            this.checkExpunged();
            final IMAPProtocol p = this.getProtocol();
            try {
                if (p.isREV1()) {
                    final BODY b = p.peekBody(this.getSequenceNumber(), this.toSection("HEADER"));
                    if (b != null) {
                        is = b.getByteArrayInputStream();
                    }
                }
                else {
                    final RFC822DATA rd = p.fetchRFC822(this.getSequenceNumber(), "HEADER");
                    if (rd != null) {
                        is = rd.getByteArrayInputStream();
                    }
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is == null) {
            throw new MessagingException("Cannot load header");
        }
        this.headers = new InternetHeaders(is);
        this.headersLoaded = true;
    }
    
    private synchronized void loadFlags() throws MessagingException {
        if (this.flags != null) {
            return;
        }
        synchronized (this.getMessageCacheLock()) {
            this.checkExpunged();
            try {
                this.flags = this.getProtocol().fetchFlags(this.getSequenceNumber());
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            }
            catch (ProtocolException pex) {
                this.forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
    }
    
    private boolean isHeaderLoaded(final String name) {
        return this.headersLoaded || (this.loadedHeaders != null && this.loadedHeaders.containsKey(name.toUpperCase()));
    }
    
    private void setHeaderLoaded(final String name) {
        if (this.loadedHeaders == null) {
            this.loadedHeaders = new Hashtable(1);
        }
        this.loadedHeaders.put(name.toUpperCase(), name);
    }
    
    private String toSection(final String what) {
        if (this.sectionId == null) {
            return what;
        }
        return this.sectionId + "." + what;
    }
    
    private InternetAddress[] aaclone(final InternetAddress[] aa) {
        if (aa == null) {
            return null;
        }
        return aa.clone();
    }
    
    private Flags _getFlags() {
        return this.flags;
    }
    
    private ENVELOPE _getEnvelope() {
        return this.envelope;
    }
    
    private BODYSTRUCTURE _getBodyStructure() {
        return this.bs;
    }
    
    void _setFlags(final Flags flags) {
        this.flags = flags;
    }
    
    Session _getSession() {
        return this.session;
    }
    
    static {
        IMAPMessage.EnvelopeCmd = "ENVELOPE INTERNALDATE RFC822.SIZE";
    }
    
    class FetchProfileCondition implements Utility.Condition
    {
        private boolean needEnvelope;
        private boolean needFlags;
        private boolean needBodyStructure;
        private boolean needUID;
        private boolean needHeaders;
        private boolean needSize;
        private String[] hdrs;
        
        public FetchProfileCondition(final FetchProfile fp) {
            this.needEnvelope = false;
            this.needFlags = false;
            this.needBodyStructure = false;
            this.needUID = false;
            this.needHeaders = false;
            this.needSize = false;
            this.hdrs = null;
            if (fp.contains(FetchProfile.Item.ENVELOPE)) {
                this.needEnvelope = true;
            }
            if (fp.contains(FetchProfile.Item.FLAGS)) {
                this.needFlags = true;
            }
            if (fp.contains(FetchProfile.Item.CONTENT_INFO)) {
                this.needBodyStructure = true;
            }
            if (fp.contains(UIDFolder.FetchProfileItem.UID)) {
                this.needUID = true;
            }
            if (fp.contains(IMAPFolder.FetchProfileItem.HEADERS)) {
                this.needHeaders = true;
            }
            if (fp.contains(IMAPFolder.FetchProfileItem.SIZE)) {
                this.needSize = true;
            }
            this.hdrs = fp.getHeaderNames();
        }
        
        public boolean test(final IMAPMessage m) {
            if (this.needEnvelope && m._getEnvelope() == null) {
                return true;
            }
            if (this.needFlags && m._getFlags() == null) {
                return true;
            }
            if (this.needBodyStructure && m._getBodyStructure() == null) {
                return true;
            }
            if (this.needUID && m.getUID() == -1L) {
                return true;
            }
            if (this.needHeaders && !m.headersLoaded) {
                return true;
            }
            if (this.needSize && m.size == -1) {
                return true;
            }
            for (int i = 0; i < this.hdrs.length; ++i) {
                if (!m.isHeaderLoaded(this.hdrs[i])) {
                    return true;
                }
            }
            return false;
        }
    }
}
