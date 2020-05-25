package com.mongodb;

public interface DBCallbackFactory
{
    DBCallback create(final DBCollection p0);
}
