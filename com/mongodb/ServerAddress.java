package com.mongodb;

import java.io.*;
import com.mongodb.annotations.*;
import java.net.*;

@Immutable
public class ServerAddress implements Serializable
{
    private static final long serialVersionUID = 4027873363095395504L;
    private final String host;
    private final int port;
    
    public ServerAddress() {
        this(defaultHost(), defaultPort());
    }
    
    public ServerAddress(final String host) {
        this(host, defaultPort());
    }
    
    public ServerAddress(final InetAddress inetAddress) {
        this(inetAddress.getHostName(), defaultPort());
    }
    
    public ServerAddress(final InetAddress inetAddress, final int port) {
        this(inetAddress.getHostName(), port);
    }
    
    public ServerAddress(final InetSocketAddress inetSocketAddress) {
        this(inetSocketAddress.getAddress(), inetSocketAddress.getPort());
    }
    
    public ServerAddress(final String host, final int port) {
        String hostToUse = host;
        if (hostToUse == null) {
            hostToUse = defaultHost();
        }
        hostToUse = hostToUse.trim();
        if (hostToUse.length() == 0) {
            hostToUse = defaultHost();
        }
        int portToUse = port;
        if (hostToUse.startsWith("[")) {
            final int idx = host.indexOf("]");
            if (idx == -1) {
                throw new IllegalArgumentException("an IPV6 address must be encosed with '[' and ']' according to RFC 2732.");
            }
            final int portIdx = host.indexOf("]:");
            if (portIdx != -1) {
                if (port != defaultPort()) {
                    throw new IllegalArgumentException("can't specify port in construct and via host");
                }
                portToUse = Integer.parseInt(host.substring(portIdx + 2));
            }
            hostToUse = host.substring(1, idx);
        }
        else {
            final int idx = hostToUse.indexOf(":");
            final int lastIdx = hostToUse.lastIndexOf(":");
            if (idx == lastIdx && idx > 0) {
                if (port != defaultPort()) {
                    throw new IllegalArgumentException("can't specify port in construct and via host");
                }
                try {
                    portToUse = Integer.parseInt(hostToUse.substring(idx + 1));
                }
                catch (NumberFormatException e) {
                    throw new MongoException("host and port should be specified in host:port format");
                }
                hostToUse = hostToUse.substring(0, idx).trim();
            }
        }
        this.host = hostToUse.toLowerCase();
        this.port = portToUse;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ServerAddress that = (ServerAddress)o;
        return this.port == that.port && this.host.equals(that.host);
    }
    
    @Override
    public int hashCode() {
        int result = this.host.hashCode();
        result = 31 * result + this.port;
        return result;
    }
    
    public String getHost() {
        return this.host;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public InetSocketAddress getSocketAddress() {
        try {
            return new InetSocketAddress(InetAddress.getByName(this.host), this.port);
        }
        catch (UnknownHostException e) {
            throw new MongoSocketException(e.getMessage(), this, e);
        }
    }
    
    @Override
    public String toString() {
        return this.host + ":" + this.port;
    }
    
    public static String defaultHost() {
        return "127.0.0.1";
    }
    
    public static int defaultPort() {
        return 27017;
    }
    
    public boolean sameHost(final String hostName) {
        String hostToUse = hostName;
        final int idx = hostToUse.indexOf(":");
        int portToUse = defaultPort();
        if (idx > 0) {
            portToUse = Integer.parseInt(hostToUse.substring(idx + 1));
            hostToUse = hostToUse.substring(0, idx);
        }
        return this.getPort() == portToUse && this.getHost().equalsIgnoreCase(hostToUse);
    }
}
