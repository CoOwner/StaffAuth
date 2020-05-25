package javax.mail.util;

import javax.activation.*;
import javax.mail.internet.*;
import java.io.*;

public class ByteArrayDataSource implements DataSource
{
    private byte[] data;
    private String type;
    private String name;
    
    public ByteArrayDataSource(final InputStream is, final String type) throws IOException {
        this.name = "";
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final byte[] buf = new byte[8192];
        int len;
        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
        this.data = os.toByteArray();
        this.type = type;
    }
    
    public ByteArrayDataSource(final byte[] data, final String type) {
        this.name = "";
        this.data = data;
        this.type = type;
    }
    
    public ByteArrayDataSource(final String data, final String type) throws IOException {
        this.name = "";
        String charset = null;
        try {
            final ContentType ct = new ContentType(type);
            charset = ct.getParameter("charset");
        }
        catch (ParseException ex) {}
        if (charset == null) {
            charset = MimeUtility.getDefaultJavaCharset();
        }
        this.data = data.getBytes(charset);
        this.type = type;
    }
    
    public InputStream getInputStream() throws IOException {
        if (this.data == null) {
            throw new IOException("no data");
        }
        return new ByteArrayInputStream(this.data);
    }
    
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("cannot do this");
    }
    
    public String getContentType() {
        return this.type;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
}
