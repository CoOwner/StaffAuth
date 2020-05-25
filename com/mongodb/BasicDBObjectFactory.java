package com.mongodb;

import java.util.*;

class BasicDBObjectFactory implements DBObjectFactory
{
    @Override
    public DBObject getInstance() {
        return new BasicDBObject();
    }
    
    @Override
    public DBObject getInstance(final List<String> path) {
        return new BasicDBObject();
    }
}
