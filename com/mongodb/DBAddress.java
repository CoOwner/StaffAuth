package com.mongodb;

import java.net.*;

@Deprecated
public class DBAddress extends ServerAddress
{
    private static final long serialVersionUID = -813211264765778133L;
    private final String _db;
    
    public DBAddress(final String urlFormat) {
        super(_getHostSection(urlFormat));
        _check(urlFormat, "urlFormat");
        this._db = _fixName(_getDBSection(urlFormat));
        _check(this.getHost(), "host");
        _check(this._db, "db");
    }
    
    static String _getHostSection(final String urlFormat) {
        if (urlFormat == null) {
            throw new NullPointerException("urlFormat can't be null");
        }
        final int idx = urlFormat.indexOf("/");
        if (idx >= 0) {
            return urlFormat.substring(0, idx);
        }
        return null;
    }
    
    static String _getDBSection(final String urlFormat) {
        if (urlFormat == null) {
            throw new NullPointerException("urlFormat can't be null");
        }
        final int idx = urlFormat.indexOf("/");
        if (idx >= 0) {
            return urlFormat.substring(idx + 1);
        }
        return urlFormat;
    }
    
    static String _fixName(final String name) {
        return name.replace('.', '-');
    }
    
    public DBAddress(final DBAddress other, final String databaseName) {
        this(other.getHost(), other.getPort(), databaseName);
    }
    
    public DBAddress(final String host, final String databaseName) {
        this(host, ServerAddress.defaultPort(), databaseName);
    }
    
    public DBAddress(final String host, final int port, final String databaseName) {
        super(host, port);
        this._db = databaseName.trim();
    }
    
    public DBAddress(final InetAddress inetAddress, final int port, final String databaseName) {
        super(inetAddress, port);
        _check(databaseName, "name");
        this._db = databaseName.trim();
    }
    
    static void _check(final String thing, final String name) {
        if (thing == null) {
            throw new NullPointerException(name + " can't be null ");
        }
        final String trimmedThing = thing.trim();
        if (trimmedThing.length() == 0) {
            throw new IllegalArgumentException(name + " can't be empty");
        }
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() + this._db.hashCode();
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other instanceof DBAddress) {
            final DBAddress a = (DBAddress)other;
            return a.getPort() == this.getPort() && a._db.equals(this._db) && a.getHost().equals(this.getHost());
        }
        return other instanceof ServerAddress && other.equals(this);
    }
    
    public DBAddress getSister(final String name) {
        return new DBAddress(this.getHost(), this.getPort(), name);
    }
    
    public String getDBName() {
        return this._db;
    }
    
    @Override
    public String toString() {
        return super.toString() + "/" + this._db;
    }
}
