package com.mongodb.client.jndi;

import javax.naming.spi.*;
import javax.naming.*;
import com.mongodb.*;
import java.util.*;
import com.mongodb.diagnostics.logging.*;

public class MongoClientFactory implements ObjectFactory
{
    private static final Logger LOGGER;
    private static final String CONNECTION_STRING = "connectionString";
    
    @Override
    public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment) throws Exception {
        String connectionString = null;
        if (environment.get("connectionString") instanceof String) {
            connectionString = (String)environment.get("connectionString");
        }
        if (connectionString == null || connectionString.isEmpty()) {
            MongoClientFactory.LOGGER.debug(String.format("No '%s' property in environment.  Casting 'obj' to java.naming.Reference to look for a javax.naming.RefAddr with type equal to '%s'", "connectionString", "connectionString"));
            if (obj instanceof Reference) {
                final Enumeration<RefAddr> props = ((Reference)obj).getAll();
                while (props.hasMoreElements()) {
                    final RefAddr addr = props.nextElement();
                    if (addr != null && "connectionString".equals(addr.getType()) && addr.getContent() instanceof String) {
                        connectionString = (String)addr.getContent();
                        break;
                    }
                }
            }
        }
        if (connectionString == null || connectionString.isEmpty()) {
            throw new MongoException(String.format("Could not locate '%s' in either environment or obj", "connectionString"));
        }
        final MongoClientURI uri = new MongoClientURI(connectionString);
        return new MongoClient(uri);
    }
    
    static {
        LOGGER = Loggers.getLogger("client.jndi");
    }
}
