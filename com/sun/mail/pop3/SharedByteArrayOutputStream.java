package com.sun.mail.pop3;

import java.io.*;
import javax.mail.util.*;

class SharedByteArrayOutputStream extends ByteArrayOutputStream
{
    public SharedByteArrayOutputStream(final int size) {
        super(size);
    }
    
    public InputStream toStream() {
        return new SharedByteArrayInputStream(this.buf, 0, this.count);
    }
}
