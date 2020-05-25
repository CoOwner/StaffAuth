package org.bson.diagnostics;

import java.util.logging.*;

public final class Loggers
{
    public static final String PREFIX = "org.bson";
    
    public static Logger getLogger(final String suffix) {
        if (suffix == null) {
            throw new IllegalArgumentException("suffix can not be null");
        }
        if (suffix.startsWith(".") || suffix.endsWith(".")) {
            throw new IllegalArgumentException("The suffix can not start or end with a '.'");
        }
        return Logger.getLogger("org.bson." + suffix);
    }
    
    private Loggers() {
    }
}
