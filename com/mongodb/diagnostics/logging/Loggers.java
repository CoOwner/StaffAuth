package com.mongodb.diagnostics.logging;

import com.mongodb.assertions.*;

public final class Loggers
{
    public static final String PREFIX = "org.mongodb.driver";
    private static final boolean USE_SLF4J;
    
    public static Logger getLogger(final String suffix) {
        Assertions.notNull("suffix", suffix);
        if (suffix.startsWith(".") || suffix.endsWith(".")) {
            throw new IllegalArgumentException("The suffix can not start or end with a '.'");
        }
        final String name = "org.mongodb.driver." + suffix;
        if (Loggers.USE_SLF4J) {
            return new SLF4JLogger(name);
        }
        return new JULLogger(name);
    }
    
    private Loggers() {
    }
    
    private static boolean shouldUseSLF4J() {
        try {
            Class.forName("org.slf4j.LoggerFactory");
            Class.forName("org.slf4j.impl.StaticLoggerBinder");
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    static {
        USE_SLF4J = shouldUseSLF4J();
    }
}
