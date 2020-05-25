package javax.activation;

import java.io.*;

public interface DataSource
{
    InputStream getInputStream() throws IOException;
    
    OutputStream getOutputStream() throws IOException;
    
    String getContentType();
    
    String getName();
}
