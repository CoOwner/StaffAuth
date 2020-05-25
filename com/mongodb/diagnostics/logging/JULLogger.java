package com.mongodb.diagnostics.logging;

import java.util.logging.*;

class JULLogger implements Logger
{
    private final java.util.logging.Logger delegate;
    
    JULLogger(final String name) {
        this.delegate = java.util.logging.Logger.getLogger(name);
    }
    
    @Override
    public String getName() {
        return this.delegate.getName();
    }
    
    @Override
    public boolean isTraceEnabled() {
        return this.isEnabled(Level.FINER);
    }
    
    @Override
    public void trace(final String msg) {
        this.log(Level.FINER, msg);
    }
    
    @Override
    public void trace(final String msg, final Throwable t) {
        this.log(Level.FINER, msg, t);
    }
    
    @Override
    public boolean isDebugEnabled() {
        return this.isEnabled(Level.FINE);
    }
    
    @Override
    public void debug(final String msg) {
        this.log(Level.FINE, msg);
    }
    
    @Override
    public void debug(final String msg, final Throwable t) {
        this.log(Level.FINE, msg, t);
    }
    
    @Override
    public boolean isInfoEnabled() {
        return this.delegate.isLoggable(Level.INFO);
    }
    
    @Override
    public void info(final String msg) {
        this.log(Level.INFO, msg);
    }
    
    @Override
    public void info(final String msg, final Throwable t) {
        this.log(Level.INFO, msg, t);
    }
    
    @Override
    public boolean isWarnEnabled() {
        return this.delegate.isLoggable(Level.WARNING);
    }
    
    @Override
    public void warn(final String msg) {
        this.log(Level.WARNING, msg);
    }
    
    @Override
    public void warn(final String msg, final Throwable t) {
        this.log(Level.WARNING, msg, t);
    }
    
    @Override
    public boolean isErrorEnabled() {
        return this.delegate.isLoggable(Level.SEVERE);
    }
    
    @Override
    public void error(final String msg) {
        this.log(Level.SEVERE, msg);
    }
    
    @Override
    public void error(final String msg, final Throwable t) {
        this.log(Level.SEVERE, msg, t);
    }
    
    private boolean isEnabled(final Level level) {
        return this.delegate.isLoggable(level);
    }
    
    private void log(final Level level, final String msg) {
        this.delegate.log(level, msg);
    }
    
    public void log(final Level level, final String msg, final Throwable t) {
        this.delegate.log(level, msg, t);
    }
}
