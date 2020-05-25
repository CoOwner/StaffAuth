package com.mongodb.diagnostics.logging;

public interface Logger
{
    String getName();
    
    boolean isTraceEnabled();
    
    void trace(final String p0);
    
    void trace(final String p0, final Throwable p1);
    
    boolean isDebugEnabled();
    
    void debug(final String p0);
    
    void debug(final String p0, final Throwable p1);
    
    boolean isInfoEnabled();
    
    void info(final String p0);
    
    void info(final String p0, final Throwable p1);
    
    boolean isWarnEnabled();
    
    void warn(final String p0);
    
    void warn(final String p0, final Throwable p1);
    
    boolean isErrorEnabled();
    
    void error(final String p0);
    
    void error(final String p0, final Throwable p1);
}
