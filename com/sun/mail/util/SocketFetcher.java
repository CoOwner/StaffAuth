package com.sun.mail.util;

import java.util.*;
import javax.net.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;

public class SocketFetcher
{
    private SocketFetcher() {
    }
    
    public static Socket getSocket(final String host, final int port, Properties props, String prefix, final boolean useSSL) throws IOException {
        if (prefix == null) {
            prefix = "socket";
        }
        if (props == null) {
            props = new Properties();
        }
        final String s = props.getProperty(prefix + ".connectiontimeout", null);
        int cto = -1;
        if (s != null) {
            try {
                cto = Integer.parseInt(s);
            }
            catch (NumberFormatException ex2) {}
        }
        Socket socket = null;
        final String sfClass = props.getProperty(prefix + ".socketFactory.class", null);
        final String timeout = props.getProperty(prefix + ".timeout", null);
        final String localaddrstr = props.getProperty(prefix + ".localaddress", null);
        InetAddress localaddr = null;
        if (localaddrstr != null) {
            localaddr = InetAddress.getByName(localaddrstr);
        }
        final String localportstr = props.getProperty(prefix + ".localport", null);
        int localport = 0;
        if (localportstr != null) {
            try {
                localport = Integer.parseInt(localportstr);
            }
            catch (NumberFormatException ex3) {}
        }
        if (sfClass != null && sfClass.length() > 0) {
            int sfPort = -1;
            boolean fb = false;
            final String fallback = props.getProperty(prefix + ".socketFactory.fallback", null);
            fb = (fallback == null || !fallback.equalsIgnoreCase("false"));
            final String sfPortStr = props.getProperty(prefix + ".socketFactory.port", null);
            if (sfPortStr != null) {
                try {
                    sfPort = Integer.parseInt(sfPortStr);
                }
                catch (NumberFormatException ex4) {}
            }
            try {
                final ClassLoader cl = getContextClassLoader();
                Class clsSockFact = null;
                if (cl != null) {
                    try {
                        clsSockFact = cl.loadClass(sfClass);
                    }
                    catch (ClassNotFoundException ex5) {}
                }
                if (clsSockFact == null) {
                    clsSockFact = Class.forName(sfClass);
                }
                final Method mthGetDefault = clsSockFact.getMethod("getDefault", (Class[])new Class[0]);
                final SocketFactory sf = (SocketFactory)mthGetDefault.invoke(new Object(), new Object[0]);
                if (sfPort == -1) {
                    sfPort = port;
                }
                socket = createSocket(localaddr, localport, host, sfPort, cto, sf, useSSL);
            }
            catch (SocketTimeoutException sex) {
                throw sex;
            }
            catch (Exception ex) {
                if (!fb) {
                    if (ex instanceof InvocationTargetException) {
                        final Throwable t = ((InvocationTargetException)ex).getTargetException();
                        if (t instanceof Exception) {
                            ex = (Exception)t;
                        }
                    }
                    if (ex instanceof IOException) {
                        throw (IOException)ex;
                    }
                    final IOException ioex = new IOException("Couldn't connect using \"" + sfClass + "\" socket factory to host, port: " + host + ", " + sfPort + "; Exception: " + ex);
                    ioex.initCause(ex);
                    throw ioex;
                }
            }
        }
        if (socket == null) {
            socket = createSocket(localaddr, localport, host, port, cto, null, useSSL);
        }
        int to = -1;
        if (timeout != null) {
            try {
                to = Integer.parseInt(timeout);
            }
            catch (NumberFormatException ex6) {}
        }
        if (to >= 0) {
            socket.setSoTimeout(to);
        }
        return socket;
    }
    
    public static Socket getSocket(final String host, final int port, final Properties props, final String prefix) throws IOException {
        return getSocket(host, port, props, prefix, false);
    }
    
    private static Socket createSocket(final InetAddress localaddr, final int localport, final String host, final int port, final int cto, final SocketFactory sf, final boolean useSSL) throws IOException {
        Socket socket;
        if (sf != null) {
            socket = sf.createSocket();
        }
        else if (useSSL) {
            socket = SSLSocketFactory.getDefault().createSocket();
        }
        else {
            socket = new Socket();
        }
        if (localaddr != null) {
            socket.bind(new InetSocketAddress(localaddr, localport));
        }
        if (cto >= 0) {
            socket.connect(new InetSocketAddress(host, port), cto);
        }
        else {
            socket.connect(new InetSocketAddress(host, port));
        }
        return socket;
    }
    
    public static Socket startTLS(Socket socket) throws IOException {
        final InetAddress a = socket.getInetAddress();
        final String host = a.getHostName();
        final int port = socket.getPort();
        try {
            final SSLSocketFactory ssf = (SSLSocketFactory)SSLSocketFactory.getDefault();
            socket = ssf.createSocket(socket, host, port, true);
            ((SSLSocket)socket).setEnabledProtocols(new String[] { "TLSv1" });
        }
        catch (Exception ex) {
            if (ex instanceof InvocationTargetException) {
                final Throwable t = ((InvocationTargetException)ex).getTargetException();
                if (t instanceof Exception) {
                    ex = (Exception)t;
                }
            }
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            final IOException ioex = new IOException("Exception in startTLS: host " + host + ", port " + port + "; Exception: " + ex);
            ioex.initCause(ex);
            throw ioex;
        }
        return socket;
    }
    
    private static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>)new PrivilegedAction() {
            public Object run() {
                ClassLoader cl = null;
                try {
                    cl = Thread.currentThread().getContextClassLoader();
                }
                catch (SecurityException ex) {}
                return cl;
            }
        });
    }
}
