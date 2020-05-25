package org.bson;

import org.bson.io.*;

public interface BSONEncoder
{
    byte[] encode(final BSONObject p0);
    
    int putObject(final BSONObject p0);
    
    void done();
    
    void set(final OutputBuffer p0);
}
