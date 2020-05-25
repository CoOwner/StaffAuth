package com.mongodb.diagnostics.logging;

import org.slf4j.*;

class SLF4JLogger implements Logger
{
    private final org.slf4j.Logger delegate;
    
    SLF4JLogger(final String name) {
        this.delegate = LoggerFactory.getLogger(name);
    }
    
    @Override
    public String getName() {
        return this.delegate.getName();
    }
    
    @Override
    public boolean isTraceEnabled() {
        return this.delegate.isTraceEnabled();
    }
    
    @Override
    public void trace(final String msg) {
        this.delegate.trace(msg);
    }
    
    @Override
    public void trace(final String msg, final Throwable t) {
        this.delegate.trace(msg, t);
    }
    
    @Override
    public boolean isDebugEnabled() {
        return this.delegate.isDebugEnabled();
    }
    
    @Override
    public void debug(final String msg) {
        this.delegate.debug(msg);
    }
    
    @Override
    public void debug(final String msg, final Throwable t) {
        this.delegate.debug(msg, t);
    }
    
    @Override
    public boolean isInfoEnabled() {
        return this.delegate.isInfoEnabled();
    }
    
    @Override
    public void info(final String msg) {
        this.delegate.info(msg);
    }
    
    @Override
    public void info(final String msg, final Throwable t) {
        this.delegate.info(msg, t);
    }
    
    @Override
    public boolean isWarnEnabled() {
        return this.delegate.isWarnEnabled();
    }
    
    @Override
    public void warn(final String msg) {
        this.delegate.warn(msg);
    }
    
    @Override
    public void warn(final String msg, final Throwable t) {
        this.delegate.warn(msg, t);
    }
    
    @Override
    public boolean isErrorEnabled() {
        return this.delegate.isErrorEnabled();
    }
    
    @Override
    public void error(final String msg) {
        this.delegate.error(msg);
    }
    
    @Override
    public void error(final String msg, final Throwable t) {
        this.delegate.error(msg, t);
    }
}
