package javax.mail.util;

import javax.mail.internet.*;
import java.io.*;

public class SharedByteArrayInputStream extends ByteArrayInputStream implements SharedInputStream
{
    protected int start;
    
    public SharedByteArrayInputStream(final byte[] buf) {
        super(buf);
        this.start = 0;
    }
    
    public SharedByteArrayInputStream(final byte[] buf, final int offset, final int length) {
        super(buf, offset, length);
        this.start = 0;
        this.start = offset;
    }
    
    public long getPosition() {
        return this.pos - this.start;
    }
    
    public InputStream newStream(final long start, long end) {
        if (start < 0L) {
            throw new IllegalArgumentException("start < 0");
        }
        if (end == -1L) {
            end = this.count - this.start;
        }
        return new SharedByteArrayInputStream(this.buf, this.start + (int)start, (int)(end - start));
    }
}
