package javax.mail.internet;

import java.io.*;

public interface SharedInputStream
{
    long getPosition();
    
    InputStream newStream(final long p0, final long p1);
}
