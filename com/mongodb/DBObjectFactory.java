package com.mongodb;

import java.util.*;

interface DBObjectFactory
{
    DBObject getInstance();
    
    DBObject getInstance(final List<String> p0);
}
