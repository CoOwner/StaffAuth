package com.mongodb;

import org.bson.*;
import java.io.*;

public interface DBDecoder extends BSONDecoder
{
    DBCallback getDBCallback(final DBCollection p0);
    
    DBObject decode(final InputStream p0, final DBCollection p1) throws IOException;
    
    DBObject decode(final byte[] p0, final DBCollection p1);
}
