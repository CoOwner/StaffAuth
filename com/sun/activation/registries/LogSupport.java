package com.sun.activation.registries;

import java.util.logging.*;

public class LogSupport
{
    private static boolean debug;
    private static Logger logger;
    private static final Level level;
    
    private LogSupport() {
    }
    
    public static void log(final String s) {
        if (LogSupport.debug) {
            System.out.println(s);
        }
        LogSupport.logger.log(LogSupport.level, s);
    }
    
    public static void log(final String s, final Throwable t) {
        if (LogSupport.debug) {
            System.out.println(s + "; Exception: " + t);
        }
        LogSupport.logger.log(LogSupport.level, s, t);
    }
    
    public static boolean isLoggable() {
        return LogSupport.debug || LogSupport.logger.isLoggable(LogSupport.level);
    }
    
    static {
        LogSupport.debug = false;
        level = Level.FINE;
        try {
            LogSupport.debug = Boolean.getBoolean("javax.activation.debug");
        }
        catch (Throwable t) {}
        LogSupport.logger = Logger.getLogger("javax.activation");
    }
}
