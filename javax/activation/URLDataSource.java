package javax.activation;

import java.net.*;
import java.io.*;

public class URLDataSource implements DataSource
{
    private URL url;
    private URLConnection url_conn;
    
    public URLDataSource(final URL url) {
        this.url = null;
        this.url_conn = null;
        this.url = url;
    }
    
    @Override
    public String getContentType() {
        String contentType = null;
        try {
            if (this.url_conn == null) {
                this.url_conn = this.url.openConnection();
            }
        }
        catch (IOException ex) {}
        if (this.url_conn != null) {
            contentType = this.url_conn.getContentType();
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return contentType;
    }
    
    @Override
    public String getName() {
        return this.url.getFile();
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return this.url.openStream();
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        this.url_conn = this.url.openConnection();
        if (this.url_conn != null) {
            this.url_conn.setDoOutput(true);
            return this.url_conn.getOutputStream();
        }
        return null;
    }
    
    public URL getURL() {
        return this.url;
    }
}
