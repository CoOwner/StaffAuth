package org.bson;

import java.io.*;

public interface BSONDecoder
{
    BSONObject readObject(final byte[] p0);
    
    BSONObject readObject(final InputStream p0) throws IOException;
    
    int decode(final byte[] p0, final BSONCallback p1);
    
    int decode(final InputStream p0, final BSONCallback p1) throws IOException;
}
