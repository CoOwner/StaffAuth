package javax.mail.internet;

import javax.mail.*;
import javax.activation.*;
import java.io.*;
import java.util.*;
import com.sun.mail.util.*;

public class MimeBodyPart extends BodyPart implements MimePart
{
    private static boolean setDefaultTextCharset;
    private static boolean setContentTypeFileName;
    private static boolean encodeFileName;
    private static boolean decodeFileName;
    static boolean cacheMultipart;
    protected DataHandler dh;
    protected byte[] content;
    protected InputStream contentStream;
    protected InternetHeaders headers;
    
    public MimeBodyPart() {
        this.headers = new InternetHeaders();
    }
    
    public MimeBodyPart(InputStream is) throws MessagingException {
        if (!(is instanceof ByteArrayInputStream) && !(is instanceof BufferedInputStream) && !(is instanceof SharedInputStream)) {
            is = new BufferedInputStream(is);
        }
        this.headers = new InternetHeaders(is);
        if (is instanceof SharedInputStream) {
            final SharedInputStream sis = (SharedInputStream)is;
            this.contentStream = sis.newStream(sis.getPosition(), -1L);
        }
        else {
            try {
                this.content = ASCIIUtility.getBytes(is);
            }
            catch (IOException ioex) {
                throw new MessagingException("Error reading input stream", ioex);
            }
        }
    }
    
    public MimeBodyPart(final InternetHeaders headers, final byte[] content) throws MessagingException {
        this.headers = headers;
        this.content = content;
    }
    
    public int getSize() throws MessagingException {
        if (this.content != null) {
            return this.content.length;
        }
        if (this.contentStream != null) {
            try {
                final int size = this.contentStream.available();
                if (size > 0) {
                    return size;
                }
            }
            catch (IOException ex) {}
        }
        return -1;
    }
    
    public int getLineCount() throws MessagingException {
        return -1;
    }
    
    public String getContentType() throws MessagingException {
        String s = this.getHeader("Content-Type", null);
        if (s == null) {
            s = "text/plain";
        }
        return s;
    }
    
    public boolean isMimeType(final String mimeType) throws MessagingException {
        return isMimeType(this, mimeType);
    }
    
    public String getDisposition() throws MessagingException {
        return getDisposition(this);
    }
    
    public void setDisposition(final String disposition) throws MessagingException {
        setDisposition(this, disposition);
    }
    
    public String getEncoding() throws MessagingException {
        return getEncoding(this);
    }
    
    public String getContentID() throws MessagingException {
        return this.getHeader("Content-Id", null);
    }
    
    public void setContentID(final String cid) throws MessagingException {
        if (cid == null) {
            this.removeHeader("Content-ID");
        }
        else {
            this.setHeader("Content-ID", cid);
        }
    }
    
    public String getContentMD5() throws MessagingException {
        return this.getHeader("Content-MD5", null);
    }
    
    public void setContentMD5(final String md5) throws MessagingException {
        this.setHeader("Content-MD5", md5);
    }
    
    public String[] getContentLanguage() throws MessagingException {
        return getContentLanguage(this);
    }
    
    public void setContentLanguage(final String[] languages) throws MessagingException {
        setContentLanguage(this, languages);
    }
    
    public String getDescription() throws MessagingException {
        return getDescription(this);
    }
    
    public void setDescription(final String description) throws MessagingException {
        this.setDescription(description, null);
    }
    
    public void setDescription(final String description, final String charset) throws MessagingException {
        setDescription(this, description, charset);
    }
    
    public String getFileName() throws MessagingException {
        return getFileName(this);
    }
    
    public void setFileName(final String filename) throws MessagingException {
        setFileName(this, filename);
    }
    
    public InputStream getInputStream() throws IOException, MessagingException {
        return this.getDataHandler().getInputStream();
    }
    
    protected InputStream getContentStream() throws MessagingException {
        if (this.contentStream != null) {
            return ((SharedInputStream)this.contentStream).newStream(0L, -1L);
        }
        if (this.content != null) {
            return new ByteArrayInputStream(this.content);
        }
        throw new MessagingException("No content");
    }
    
    public InputStream getRawInputStream() throws MessagingException {
        return this.getContentStream();
    }
    
    public DataHandler getDataHandler() throws MessagingException {
        if (this.dh == null) {
            this.dh = new DataHandler(new MimePartDataSource(this));
        }
        return this.dh;
    }
    
    public Object getContent() throws IOException, MessagingException {
        final Object c = this.getDataHandler().getContent();
        if (MimeBodyPart.cacheMultipart && (c instanceof Multipart || c instanceof Message) && !(this.dh instanceof CachedDataHandler) && (this.content != null || this.contentStream != null)) {
            this.dh = createCachedDataHandler(c, this.getContentType());
        }
        return c;
    }
    
    public void setDataHandler(final DataHandler dh) throws MessagingException {
        this.dh = dh;
        invalidateContentHeaders(this);
    }
    
    public void setContent(final Object o, final String type) throws MessagingException {
        if (o instanceof Multipart) {
            this.setContent((Multipart)o);
        }
        else {
            this.setDataHandler(new DataHandler(o, type));
        }
    }
    
    public void setText(final String text) throws MessagingException {
        this.setText(text, null);
    }
    
    public void setText(final String text, final String charset) throws MessagingException {
        setText(this, text, charset, "plain");
    }
    
    public void setText(final String text, final String charset, final String subtype) throws MessagingException {
        setText(this, text, charset, subtype);
    }
    
    public void setContent(final Multipart mp) throws MessagingException {
        this.setDataHandler(new DataHandler(mp, mp.getContentType()));
        mp.setParent(this);
    }
    
    public void attachFile(final File file) throws IOException, MessagingException {
        final FileDataSource fds = new FileDataSource(file);
        this.setDataHandler(new DataHandler(fds));
        this.setFileName(fds.getName());
    }
    
    public void attachFile(final String file) throws IOException, MessagingException {
        final File f = new File(file);
        this.attachFile(f);
    }
    
    public void saveFile(final File file) throws IOException, MessagingException {
        final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        final InputStream in = this.getInputStream();
        try {
            final byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException ex) {}
            try {
                if (out != null) {
                    out.close();
                }
            }
            catch (IOException ex2) {}
        }
    }
    
    public void saveFile(final String file) throws IOException, MessagingException {
        final File f = new File(file);
        this.saveFile(f);
    }
    
    public void writeTo(final OutputStream os) throws IOException, MessagingException {
        writeTo(this, os, null);
    }
    
    public String[] getHeader(final String name) throws MessagingException {
        return this.headers.getHeader(name);
    }
    
    public String getHeader(final String name, final String delimiter) throws MessagingException {
        return this.headers.getHeader(name, delimiter);
    }
    
    public void setHeader(final String name, final String value) throws MessagingException {
        this.headers.setHeader(name, value);
    }
    
    public void addHeader(final String name, final String value) throws MessagingException {
        this.headers.addHeader(name, value);
    }
    
    public void removeHeader(final String name) throws MessagingException {
        this.headers.removeHeader(name);
    }
    
    public Enumeration getAllHeaders() throws MessagingException {
        return this.headers.getAllHeaders();
    }
    
    public Enumeration getMatchingHeaders(final String[] names) throws MessagingException {
        return this.headers.getMatchingHeaders(names);
    }
    
    public Enumeration getNonMatchingHeaders(final String[] names) throws MessagingException {
        return this.headers.getNonMatchingHeaders(names);
    }
    
    public void addHeaderLine(final String line) throws MessagingException {
        this.headers.addHeaderLine(line);
    }
    
    public Enumeration getAllHeaderLines() throws MessagingException {
        return this.headers.getAllHeaderLines();
    }
    
    public Enumeration getMatchingHeaderLines(final String[] names) throws MessagingException {
        return this.headers.getMatchingHeaderLines(names);
    }
    
    public Enumeration getNonMatchingHeaderLines(final String[] names) throws MessagingException {
        return this.headers.getNonMatchingHeaderLines(names);
    }
    
    protected void updateHeaders() throws MessagingException {
        updateHeaders(this);
    }
    
    static boolean isMimeType(final MimePart part, final String mimeType) throws MessagingException {
        try {
            final ContentType ct = new ContentType(part.getContentType());
            return ct.match(mimeType);
        }
        catch (ParseException ex) {
            return part.getContentType().equalsIgnoreCase(mimeType);
        }
    }
    
    static void setText(final MimePart part, final String text, String charset, final String subtype) throws MessagingException {
        if (charset == null) {
            if (MimeUtility.checkAscii(text) != 1) {
                charset = MimeUtility.getDefaultMIMECharset();
            }
            else {
                charset = "us-ascii";
            }
        }
        part.setContent(text, "text/" + subtype + "; charset=" + MimeUtility.quote(charset, "()<>@,;:\\\"\t []/?="));
    }
    
    static String getDisposition(final MimePart part) throws MessagingException {
        final String s = part.getHeader("Content-Disposition", null);
        if (s == null) {
            return null;
        }
        final ContentDisposition cd = new ContentDisposition(s);
        return cd.getDisposition();
    }
    
    static void setDisposition(final MimePart part, String disposition) throws MessagingException {
        if (disposition == null) {
            part.removeHeader("Content-Disposition");
        }
        else {
            final String s = part.getHeader("Content-Disposition", null);
            if (s != null) {
                final ContentDisposition cd = new ContentDisposition(s);
                cd.setDisposition(disposition);
                disposition = cd.toString();
            }
            part.setHeader("Content-Disposition", disposition);
        }
    }
    
    static String getDescription(final MimePart part) throws MessagingException {
        final String rawvalue = part.getHeader("Content-Description", null);
        if (rawvalue == null) {
            return null;
        }
        try {
            return MimeUtility.decodeText(MimeUtility.unfold(rawvalue));
        }
        catch (UnsupportedEncodingException ex) {
            return rawvalue;
        }
    }
    
    static void setDescription(final MimePart part, final String description, final String charset) throws MessagingException {
        if (description == null) {
            part.removeHeader("Content-Description");
            return;
        }
        try {
            part.setHeader("Content-Description", MimeUtility.fold(21, MimeUtility.encodeText(description, charset, null)));
        }
        catch (UnsupportedEncodingException uex) {
            throw new MessagingException("Encoding error", uex);
        }
    }
    
    static String getFileName(final MimePart part) throws MessagingException {
        String filename = null;
        String s = part.getHeader("Content-Disposition", null);
        if (s != null) {
            final ContentDisposition cd = new ContentDisposition(s);
            filename = cd.getParameter("filename");
        }
        if (filename == null) {
            s = part.getHeader("Content-Type", null);
            if (s != null) {
                try {
                    final ContentType ct = new ContentType(s);
                    filename = ct.getParameter("name");
                }
                catch (ParseException ex2) {}
            }
        }
        if (MimeBodyPart.decodeFileName && filename != null) {
            try {
                filename = MimeUtility.decodeText(filename);
            }
            catch (UnsupportedEncodingException ex) {
                throw new MessagingException("Can't decode filename", ex);
            }
        }
        return filename;
    }
    
    static void setFileName(final MimePart part, String name) throws MessagingException {
        if (MimeBodyPart.encodeFileName && name != null) {
            try {
                name = MimeUtility.encodeText(name);
            }
            catch (UnsupportedEncodingException ex) {
                throw new MessagingException("Can't encode filename", ex);
            }
        }
        String s = part.getHeader("Content-Disposition", null);
        final ContentDisposition cd = new ContentDisposition((s == null) ? "attachment" : s);
        cd.setParameter("filename", name);
        part.setHeader("Content-Disposition", cd.toString());
        if (MimeBodyPart.setContentTypeFileName) {
            s = part.getHeader("Content-Type", null);
            if (s != null) {
                try {
                    final ContentType cType = new ContentType(s);
                    cType.setParameter("name", name);
                    part.setHeader("Content-Type", cType.toString());
                }
                catch (ParseException ex2) {}
            }
        }
    }
    
    static String[] getContentLanguage(final MimePart part) throws MessagingException {
        final String s = part.getHeader("Content-Language", null);
        if (s == null) {
            return null;
        }
        final HeaderTokenizer h = new HeaderTokenizer(s, "()<>@,;:\\\"\t []/?=");
        final Vector v = new Vector();
        while (true) {
            final HeaderTokenizer.Token tk = h.next();
            final int tkType = tk.getType();
            if (tkType == -4) {
                break;
            }
            if (tkType != -1) {
                continue;
            }
            v.addElement(tk.getValue());
        }
        if (v.size() == 0) {
            return null;
        }
        final String[] language = new String[v.size()];
        v.copyInto(language);
        return language;
    }
    
    static void setContentLanguage(final MimePart part, final String[] languages) throws MessagingException {
        final StringBuffer sb = new StringBuffer(languages[0]);
        for (int i = 1; i < languages.length; ++i) {
            sb.append(',').append(languages[i]);
        }
        part.setHeader("Content-Language", sb.toString());
    }
    
    static String getEncoding(final MimePart part) throws MessagingException {
        String s = part.getHeader("Content-Transfer-Encoding", null);
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.equalsIgnoreCase("7bit") || s.equalsIgnoreCase("8bit") || s.equalsIgnoreCase("quoted-printable") || s.equalsIgnoreCase("binary") || s.equalsIgnoreCase("base64")) {
            return s;
        }
        final HeaderTokenizer h = new HeaderTokenizer(s, "()<>@,;:\\\"\t []/?=");
        int tkType;
        HeaderTokenizer.Token tk;
        do {
            tk = h.next();
            tkType = tk.getType();
            if (tkType == -4) {
                return s;
            }
        } while (tkType != -1);
        return tk.getValue();
    }
    
    static void setEncoding(final MimePart part, final String encoding) throws MessagingException {
        part.setHeader("Content-Transfer-Encoding", encoding);
    }
    
    static DataHandler createCachedDataHandler(final Object o, final String mimeType) {
        return new CachedDataHandler(o, mimeType);
    }
    
    static void updateHeaders(final MimePart part) throws MessagingException {
        final DataHandler dh = part.getDataHandler();
        if (dh == null) {
            return;
        }
        try {
            String type = dh.getContentType();
            boolean composite = false;
            final boolean needCTHeader = part.getHeader("Content-Type") == null;
            final ContentType cType = new ContentType(type);
            if (cType.match("multipart/*")) {
                composite = true;
                final Object o = dh.getContent();
                if (!(o instanceof MimeMultipart)) {
                    throw new MessagingException("MIME part of type \"" + type + "\" contains object of type " + o.getClass().getName() + " instead of MimeMultipart");
                }
                ((MimeMultipart)o).updateHeaders();
            }
            else if (cType.match("message/rfc822")) {
                composite = true;
            }
            if (!composite) {
                if (part.getHeader("Content-Transfer-Encoding") == null) {
                    setEncoding(part, MimeUtility.getEncoding(dh));
                }
                if (needCTHeader && MimeBodyPart.setDefaultTextCharset && cType.match("text/*") && cType.getParameter("charset") == null) {
                    final String enc = part.getEncoding();
                    String charset;
                    if (enc != null && enc.equalsIgnoreCase("7bit")) {
                        charset = "us-ascii";
                    }
                    else {
                        charset = MimeUtility.getDefaultMIMECharset();
                    }
                    cType.setParameter("charset", charset);
                    type = cType.toString();
                }
            }
            if (needCTHeader) {
                final String s = part.getHeader("Content-Disposition", null);
                if (s != null) {
                    final ContentDisposition cd = new ContentDisposition(s);
                    final String filename = cd.getParameter("filename");
                    if (filename != null) {
                        cType.setParameter("name", filename);
                        type = cType.toString();
                    }
                }
                part.setHeader("Content-Type", type);
            }
        }
        catch (IOException ex) {
            throw new MessagingException("IOException updating headers", ex);
        }
    }
    
    static void invalidateContentHeaders(final MimePart part) throws MessagingException {
        part.removeHeader("Content-Type");
        part.removeHeader("Content-Transfer-Encoding");
    }
    
    static void writeTo(final MimePart part, OutputStream os, final String[] ignoreList) throws IOException, MessagingException {
        LineOutputStream los = null;
        if (os instanceof LineOutputStream) {
            los = (LineOutputStream)os;
        }
        else {
            los = new LineOutputStream(os);
        }
        final Enumeration hdrLines = part.getNonMatchingHeaderLines(ignoreList);
        while (hdrLines.hasMoreElements()) {
            los.writeln(hdrLines.nextElement());
        }
        los.writeln();
        os = MimeUtility.encode(os, part.getEncoding());
        part.getDataHandler().writeTo(os);
        os.flush();
    }
    
    static {
        MimeBodyPart.setDefaultTextCharset = true;
        MimeBodyPart.setContentTypeFileName = true;
        MimeBodyPart.encodeFileName = false;
        MimeBodyPart.decodeFileName = false;
        MimeBodyPart.cacheMultipart = true;
        try {
            String s = System.getProperty("mail.mime.setdefaulttextcharset");
            MimeBodyPart.setDefaultTextCharset = (s == null || !s.equalsIgnoreCase("false"));
            s = System.getProperty("mail.mime.setcontenttypefilename");
            MimeBodyPart.setContentTypeFileName = (s == null || !s.equalsIgnoreCase("false"));
            s = System.getProperty("mail.mime.encodefilename");
            MimeBodyPart.encodeFileName = (s != null && !s.equalsIgnoreCase("false"));
            s = System.getProperty("mail.mime.decodefilename");
            MimeBodyPart.decodeFileName = (s != null && !s.equalsIgnoreCase("false"));
            s = System.getProperty("mail.mime.cachemultipart");
            MimeBodyPart.cacheMultipart = (s == null || !s.equalsIgnoreCase("false"));
        }
        catch (SecurityException ex) {}
    }
}
