package javax.activation;

import java.io.*;
import java.security.*;
import java.net.*;
import java.util.*;

class SecuritySupport
{
    private SecuritySupport() {
    }
    
    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>)new PrivilegedAction() {
            @Override
            public Object run() {
                Object contextClassLoader = null;
                try {
                    contextClassLoader = Thread.currentThread().getContextClassLoader();
                }
                catch (SecurityException ex) {}
                return contextClassLoader;
            }
        });
    }
    
    public static InputStream getResourceAsStream(final Class clazz, final String s) throws IOException {
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<InputStream>)new PrivilegedExceptionAction() {
                @Override
                public Object run() throws IOException {
                    return clazz.getResourceAsStream(s);
                }
            });
        }
        catch (PrivilegedActionException ex) {
            throw (IOException)ex.getException();
        }
    }
    
    public static URL[] getResources(final ClassLoader classLoader, final String s) {
        return AccessController.doPrivileged((PrivilegedAction<URL[]>)new PrivilegedAction() {
            @Override
            public Object run() {
                URL[] array = null;
                try {
                    final ArrayList<URL> list = new ArrayList<URL>();
                    final Enumeration<URL> resources = classLoader.getResources(s);
                    while (resources != null && resources.hasMoreElements()) {
                        final URL url = resources.nextElement();
                        if (url != null) {
                            list.add(url);
                        }
                    }
                    if (list.size() > 0) {
                        array = new URL[list.size()];
                        array = list.toArray(array);
                    }
                }
                catch (IOException ex) {}
                catch (SecurityException ex2) {}
                return array;
            }
        });
    }
    
    public static URL[] getSystemResources(final String s) {
        return AccessController.doPrivileged((PrivilegedAction<URL[]>)new PrivilegedAction() {
            @Override
            public Object run() {
                URL[] array = null;
                try {
                    final ArrayList<URL> list = new ArrayList<URL>();
                    final Enumeration<URL> systemResources = ClassLoader.getSystemResources(s);
                    while (systemResources != null && systemResources.hasMoreElements()) {
                        final URL url = systemResources.nextElement();
                        if (url != null) {
                            list.add(url);
                        }
                    }
                    if (list.size() > 0) {
                        array = new URL[list.size()];
                        array = list.toArray(array);
                    }
                }
                catch (IOException ex) {}
                catch (SecurityException ex2) {}
                return array;
            }
        });
    }
    
    public static InputStream openStream(final URL url) throws IOException {
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<InputStream>)new PrivilegedExceptionAction() {
                @Override
                public Object run() throws IOException {
                    return url.openStream();
                }
            });
        }
        catch (PrivilegedActionException ex) {
            throw (IOException)ex.getException();
        }
    }
}
