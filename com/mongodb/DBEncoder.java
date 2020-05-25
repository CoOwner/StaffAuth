package com.mongodb;

import org.bson.io.*;
import org.bson.*;

public interface DBEncoder
{
    int writeObject(final OutputBuffer p0, final BSONObject p1);
}
