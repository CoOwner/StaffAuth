package javax.mail.internet;

import javax.activation.*;
import javax.mail.*;
import com.sun.mail.util.*;
import java.io.*;

public class MimeMultipart extends Multipart
{
    private static boolean ignoreMissingEndBoundary;
    private static boolean ignoreMissingBoundaryParameter;
    private static boolean bmparse;
    protected DataSource ds;
    protected boolean parsed;
    private boolean complete;
    private String preamble;
    
    public MimeMultipart() {
        this("mixed");
    }
    
    public MimeMultipart(final String subtype) {
        this.ds = null;
        this.parsed = true;
        this.complete = true;
        this.preamble = null;
        final String boundary = UniqueValue.getUniqueBoundaryValue();
        final ContentType cType = new ContentType("multipart", subtype, null);
        cType.setParameter("boundary", boundary);
        this.contentType = cType.toString();
    }
    
    public MimeMultipart(final DataSource ds) throws MessagingException {
        this.ds = null;
        this.parsed = true;
        this.complete = true;
        this.preamble = null;
        if (ds instanceof MessageAware) {
            final MessageContext mc = ((MessageAware)ds).getMessageContext();
            this.setParent(mc.getPart());
        }
        if (ds instanceof MultipartDataSource) {
            this.setMultipartDataSource((MultipartDataSource)ds);
            return;
        }
        this.parsed = false;
        this.ds = ds;
        this.contentType = ds.getContentType();
    }
    
    public synchronized void setSubType(final String subtype) throws MessagingException {
        final ContentType cType = new ContentType(this.contentType);
        cType.setSubType(subtype);
        this.contentType = cType.toString();
    }
    
    public synchronized int getCount() throws MessagingException {
        this.parse();
        return super.getCount();
    }
    
    public synchronized BodyPart getBodyPart(final int index) throws MessagingException {
        this.parse();
        return super.getBodyPart(index);
    }
    
    public synchronized BodyPart getBodyPart(final String CID) throws MessagingException {
        this.parse();
        for (int count = this.getCount(), i = 0; i < count; ++i) {
            final MimeBodyPart part = (MimeBodyPart)this.getBodyPart(i);
            final String s = part.getContentID();
            if (s != null && s.equals(CID)) {
                return part;
            }
        }
        return null;
    }
    
    public boolean isComplete() throws MessagingException {
        this.parse();
        return this.complete;
    }
    
    public String getPreamble() throws MessagingException {
        this.parse();
        return this.preamble;
    }
    
    public void setPreamble(final String preamble) throws MessagingException {
        this.preamble = preamble;
    }
    
    protected void updateHeaders() throws MessagingException {
        for (int i = 0; i < this.parts.size(); ++i) {
            this.parts.elementAt(i).updateHeaders();
        }
    }
    
    public void writeTo(final OutputStream os) throws IOException, MessagingException {
        this.parse();
        final String boundary = "--" + new ContentType(this.contentType).getParameter("boundary");
        final LineOutputStream los = new LineOutputStream(os);
        if (this.preamble != null) {
            final byte[] pb = ASCIIUtility.getBytes(this.preamble);
            los.write(pb);
            if (pb.length > 0 && pb[pb.length - 1] != 13 && pb[pb.length - 1] != 10) {
                los.writeln();
            }
        }
        for (int i = 0; i < this.parts.size(); ++i) {
            los.writeln(boundary);
            this.parts.elementAt(i).writeTo(os);
            los.writeln();
        }
        los.writeln(boundary + "--");
    }
    
    protected synchronized void parse() throws MessagingException {
        if (this.parsed) {
            return;
        }
        if (MimeMultipart.bmparse) {
            this.parsebm();
            return;
        }
        InputStream in = null;
        SharedInputStream sin = null;
        long start = 0L;
        long end = 0L;
        try {
            in = this.ds.getInputStream();
            if (!(in instanceof ByteArrayInputStream) && !(in instanceof BufferedInputStream) && !(in instanceof SharedInputStream)) {
                in = new BufferedInputStream(in);
            }
        }
        catch (Exception ex) {
            throw new MessagingException("No inputstream from datasource");
        }
        if (in instanceof SharedInputStream) {
            sin = (SharedInputStream)in;
        }
        final ContentType cType = new ContentType(this.contentType);
        String boundary = null;
        final String bp = cType.getParameter("boundary");
        if (bp != null) {
            boundary = "--" + bp;
        }
        else if (!MimeMultipart.ignoreMissingBoundaryParameter) {
            throw new MessagingException("Missing boundary parameter");
        }
        try {
            final LineInputStream lin = new LineInputStream(in);
            String lineSeparator = null;
            String line;
            while ((line = lin.readLine()) != null) {
                int i;
                for (i = line.length() - 1; i >= 0; --i) {
                    final char c = line.charAt(i);
                    if (c != ' ' && c != '\t') {
                        break;
                    }
                }
                line = line.substring(0, i + 1);
                if (boundary != null) {
                    if (line.equals(boundary)) {
                        break;
                    }
                }
                else if (line.startsWith("--")) {
                    boundary = line;
                    break;
                }
                if (line.length() > 0) {
                    if (lineSeparator == null) {
                        try {
                            lineSeparator = System.getProperty("line.separator", "\n");
                        }
                        catch (SecurityException ex2) {
                            lineSeparator = "\n";
                        }
                    }
                    if (this.preamble == null) {
                        this.preamble = line + lineSeparator;
                    }
                    else {
                        this.preamble = this.preamble + line + lineSeparator;
                    }
                }
            }
            if (line == null) {
                throw new MessagingException("Missing start boundary");
            }
            final byte[] bndbytes = ASCIIUtility.getBytes(boundary);
            final int bl = bndbytes.length;
            boolean done = false;
            while (!done) {
                InternetHeaders headers = null;
                if (sin != null) {
                    start = sin.getPosition();
                    while ((line = lin.readLine()) != null && line.length() > 0) {}
                    if (line == null) {
                        if (!MimeMultipart.ignoreMissingEndBoundary) {
                            throw new MessagingException("missing multipart end boundary");
                        }
                        this.complete = false;
                        break;
                    }
                }
                else {
                    headers = this.createInternetHeaders(in);
                }
                if (!in.markSupported()) {
                    throw new MessagingException("Stream doesn't support mark");
                }
                ByteArrayOutputStream buf = null;
                if (sin == null) {
                    buf = new ByteArrayOutputStream();
                }
                else {
                    end = sin.getPosition();
                }
                boolean bol = true;
                int eol1 = -1;
                int eol2 = -1;
                while (true) {
                    if (bol) {
                        in.mark(bl + 4 + 1000);
                        int j;
                        for (j = 0; j < bl && in.read() == (bndbytes[j] & 0xFF); ++j) {}
                        if (j == bl) {
                            int b2 = in.read();
                            if (b2 == 45 && in.read() == 45) {
                                this.complete = true;
                                done = true;
                                break;
                            }
                            while (b2 == 32 || b2 == 9) {
                                b2 = in.read();
                            }
                            if (b2 == 10) {
                                break;
                            }
                            if (b2 == 13) {
                                in.mark(1);
                                if (in.read() != 10) {
                                    in.reset();
                                    break;
                                }
                                break;
                            }
                        }
                        in.reset();
                        if (buf != null && eol1 != -1) {
                            buf.write(eol1);
                            if (eol2 != -1) {
                                buf.write(eol2);
                            }
                            eol2 = (eol1 = -1);
                        }
                    }
                    int b3;
                    if ((b3 = in.read()) < 0) {
                        if (!MimeMultipart.ignoreMissingEndBoundary) {
                            throw new MessagingException("missing multipart end boundary");
                        }
                        this.complete = false;
                        done = true;
                        break;
                    }
                    else if (b3 == 13 || b3 == 10) {
                        bol = true;
                        if (sin != null) {
                            end = sin.getPosition() - 1L;
                        }
                        if ((eol1 = b3) != 13) {
                            continue;
                        }
                        in.mark(1);
                        if ((b3 = in.read()) == 10) {
                            eol2 = b3;
                        }
                        else {
                            in.reset();
                        }
                    }
                    else {
                        bol = false;
                        if (buf == null) {
                            continue;
                        }
                        buf.write(b3);
                    }
                }
                MimeBodyPart part;
                if (sin != null) {
                    part = this.createMimeBodyPart(sin.newStream(start, end));
                }
                else {
                    part = this.createMimeBodyPart(headers, buf.toByteArray());
                }
                this.addBodyPart(part);
            }
        }
        catch (IOException ioex) {
            throw new MessagingException("IO Error", ioex);
        }
        finally {
            try {
                in.close();
            }
            catch (IOException ex3) {}
        }
        this.parsed = true;
    }
    
    private synchronized void parsebm() throws MessagingException {
        if (this.parsed) {
            return;
        }
        InputStream in = null;
        SharedInputStream sin = null;
        long start = 0L;
        long end = 0L;
        try {
            in = this.ds.getInputStream();
            if (!(in instanceof ByteArrayInputStream) && !(in instanceof BufferedInputStream) && !(in instanceof SharedInputStream)) {
                in = new BufferedInputStream(in);
            }
        }
        catch (Exception ex) {
            throw new MessagingException("No inputstream from datasource");
        }
        if (in instanceof SharedInputStream) {
            sin = (SharedInputStream)in;
        }
        final ContentType cType = new ContentType(this.contentType);
        String boundary = null;
        final String bp = cType.getParameter("boundary");
        if (bp != null) {
            boundary = "--" + bp;
        }
        else if (!MimeMultipart.ignoreMissingBoundaryParameter) {
            throw new MessagingException("Missing boundary parameter");
        }
        try {
            final LineInputStream lin = new LineInputStream(in);
            String lineSeparator = null;
            String line;
            while ((line = lin.readLine()) != null) {
                int i;
                for (i = line.length() - 1; i >= 0; --i) {
                    final char c = line.charAt(i);
                    if (c != ' ' && c != '\t') {
                        break;
                    }
                }
                line = line.substring(0, i + 1);
                if (boundary != null) {
                    if (line.equals(boundary)) {
                        break;
                    }
                }
                else if (line.startsWith("--")) {
                    boundary = line;
                    break;
                }
                if (line.length() > 0) {
                    if (lineSeparator == null) {
                        try {
                            lineSeparator = System.getProperty("line.separator", "\n");
                        }
                        catch (SecurityException ex2) {
                            lineSeparator = "\n";
                        }
                    }
                    if (this.preamble == null) {
                        this.preamble = line + lineSeparator;
                    }
                    else {
                        this.preamble = this.preamble + line + lineSeparator;
                    }
                }
            }
            if (line == null) {
                throw new MessagingException("Missing start boundary");
            }
            final byte[] bndbytes = ASCIIUtility.getBytes(boundary);
            final int bl = bndbytes.length;
            final int[] bcs = new int[256];
            for (int j = 0; j < bl; ++j) {
                bcs[bndbytes[j]] = j + 1;
            }
            final int[] gss = new int[bl];
            int k = bl;
        Label_0443:
            while (k > 0) {
                while (true) {
                    int l;
                    for (l = bl - 1; l >= k; --l) {
                        if (bndbytes[l] != bndbytes[l - k]) {
                            --k;
                            continue Label_0443;
                        }
                        gss[l - 1] = k;
                    }
                    while (l > 0) {
                        gss[--l] = k;
                    }
                    continue;
                }
            }
            gss[bl - 1] = 1;
            boolean done = false;
            while (!done) {
                InternetHeaders headers = null;
                if (sin != null) {
                    start = sin.getPosition();
                    while ((line = lin.readLine()) != null && line.length() > 0) {}
                    if (line == null) {
                        if (!MimeMultipart.ignoreMissingEndBoundary) {
                            throw new MessagingException("missing multipart end boundary");
                        }
                        this.complete = false;
                        break;
                    }
                }
                else {
                    headers = this.createInternetHeaders(in);
                }
                if (!in.markSupported()) {
                    throw new MessagingException("Stream doesn't support mark");
                }
                ByteArrayOutputStream buf = null;
                if (sin == null) {
                    buf = new ByteArrayOutputStream();
                }
                else {
                    end = sin.getPosition();
                }
                byte[] inbuf = new byte[bl];
                byte[] previnbuf = new byte[bl];
                int inSize = 0;
                int prevSize = 0;
                boolean first = true;
                int eolLen;
                while (true) {
                    in.mark(bl + 4 + 1000);
                    eolLen = 0;
                    inSize = in.read(inbuf, 0, bl);
                    if (inSize < bl) {
                        if (!MimeMultipart.ignoreMissingEndBoundary) {
                            throw new MessagingException("missing multipart end boundary");
                        }
                        if (sin != null) {
                            end = sin.getPosition();
                        }
                        this.complete = false;
                        done = true;
                        break;
                    }
                    else {
                        int m;
                        for (m = bl - 1; m >= 0 && inbuf[m] == bndbytes[m]; --m) {}
                        if (m < 0) {
                            eolLen = 0;
                            if (!first) {
                                int b = previnbuf[prevSize - 1];
                                if (b == 13 || b == 10) {
                                    eolLen = 1;
                                    if (b == 10 && prevSize >= 2) {
                                        b = previnbuf[prevSize - 2];
                                        if (b == 13) {
                                            eolLen = 2;
                                        }
                                    }
                                }
                            }
                            if (first || eolLen > 0) {
                                if (sin != null) {
                                    end = sin.getPosition() - bl - eolLen;
                                }
                                int b2 = in.read();
                                if (b2 == 45 && in.read() == 45) {
                                    this.complete = true;
                                    done = true;
                                    break;
                                }
                                while (b2 == 32 || b2 == 9) {
                                    b2 = in.read();
                                }
                                if (b2 == 10) {
                                    break;
                                }
                                if (b2 == 13) {
                                    in.mark(1);
                                    if (in.read() != 10) {
                                        in.reset();
                                        break;
                                    }
                                    break;
                                }
                            }
                            m = 0;
                        }
                        final int skip = Math.max(m + 1 - bcs[inbuf[m] & 0x7F], gss[m]);
                        if (skip < 2) {
                            if (sin == null && prevSize > 1) {
                                buf.write(previnbuf, 0, prevSize - 1);
                            }
                            in.reset();
                            in.skip(1L);
                            if (prevSize >= 1) {
                                previnbuf[0] = previnbuf[prevSize - 1];
                                previnbuf[1] = inbuf[0];
                                prevSize = 2;
                            }
                            else {
                                previnbuf[0] = inbuf[0];
                                prevSize = 1;
                            }
                        }
                        else {
                            if (prevSize > 0 && sin == null) {
                                buf.write(previnbuf, 0, prevSize);
                            }
                            prevSize = skip;
                            in.reset();
                            in.skip(prevSize);
                            final byte[] tmp = inbuf;
                            inbuf = previnbuf;
                            previnbuf = tmp;
                        }
                        first = false;
                    }
                }
                MimeBodyPart part;
                if (sin != null) {
                    part = this.createMimeBodyPart(sin.newStream(start, end));
                }
                else {
                    if (prevSize - eolLen > 0) {
                        buf.write(previnbuf, 0, prevSize - eolLen);
                    }
                    if (!this.complete && inSize > 0) {
                        buf.write(inbuf, 0, inSize);
                    }
                    part = this.createMimeBodyPart(headers, buf.toByteArray());
                }
                this.addBodyPart(part);
            }
        }
        catch (IOException ioex) {
            throw new MessagingException("IO Error", ioex);
        }
        finally {
            try {
                in.close();
            }
            catch (IOException ex3) {}
        }
        this.parsed = true;
    }
    
    protected InternetHeaders createInternetHeaders(final InputStream is) throws MessagingException {
        return new InternetHeaders(is);
    }
    
    protected MimeBodyPart createMimeBodyPart(final InternetHeaders headers, final byte[] content) throws MessagingException {
        return new MimeBodyPart(headers, content);
    }
    
    protected MimeBodyPart createMimeBodyPart(final InputStream is) throws MessagingException {
        return new MimeBodyPart(is);
    }
    
    static {
        MimeMultipart.ignoreMissingEndBoundary = true;
        MimeMultipart.ignoreMissingBoundaryParameter = true;
        MimeMultipart.bmparse = true;
        try {
            String s = System.getProperty("mail.mime.multipart.ignoremissingendboundary");
            MimeMultipart.ignoreMissingEndBoundary = (s == null || !s.equalsIgnoreCase("false"));
            s = System.getProperty("mail.mime.multipart.ignoremissingboundaryparameter");
            MimeMultipart.ignoreMissingBoundaryParameter = (s == null || !s.equalsIgnoreCase("false"));
            s = System.getProperty("mail.mime.multipart.bmparse");
            MimeMultipart.bmparse = (s == null || !s.equalsIgnoreCase("false"));
        }
        catch (SecurityException ex) {}
    }
}
