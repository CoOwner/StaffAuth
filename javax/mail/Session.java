package javax.mail;

import java.lang.reflect.*;
import com.sun.mail.util.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

public final class Session
{
    private final Properties props;
    private final Authenticator authenticator;
    private final Hashtable authTable;
    private boolean debug;
    private PrintStream out;
    private final Vector providers;
    private final Hashtable providersByProtocol;
    private final Hashtable providersByClassName;
    private final Properties addressMap;
    private static Session defaultSession;
    private static final String version = "1.4ea";
    
    private Session(final Properties props, final Authenticator authenticator) {
        this.authTable = new Hashtable();
        this.debug = false;
        this.providers = new Vector();
        this.providersByProtocol = new Hashtable();
        this.providersByClassName = new Hashtable();
        this.addressMap = new Properties();
        this.props = props;
        this.authenticator = authenticator;
        if (Boolean.valueOf(props.getProperty("mail.debug"))) {
            this.debug = true;
        }
        if (this.debug) {
            this.pr("DEBUG: JavaMail version 1.4ea");
        }
        Class cl;
        if (authenticator != null) {
            cl = authenticator.getClass();
        }
        else {
            cl = this.getClass();
        }
        this.loadProviders(cl);
        this.loadAddressMap(cl);
    }
    
    public static Session getInstance(final Properties props, final Authenticator authenticator) {
        return new Session(props, authenticator);
    }
    
    public static Session getInstance(final Properties props) {
        return new Session(props, null);
    }
    
    public static synchronized Session getDefaultInstance(final Properties props, final Authenticator authenticator) {
        if (Session.defaultSession == null) {
            Session.defaultSession = new Session(props, authenticator);
        }
        else if (Session.defaultSession.authenticator != authenticator) {
            if (Session.defaultSession.authenticator == null || authenticator == null || Session.defaultSession.authenticator.getClass().getClassLoader() != authenticator.getClass().getClassLoader()) {
                throw new SecurityException("Access to default session denied");
            }
        }
        return Session.defaultSession;
    }
    
    public static Session getDefaultInstance(final Properties props) {
        return getDefaultInstance(props, null);
    }
    
    public synchronized void setDebug(final boolean debug) {
        this.debug = debug;
        if (debug) {
            this.pr("DEBUG: setDebug: JavaMail version 1.4ea");
        }
    }
    
    public synchronized boolean getDebug() {
        return this.debug;
    }
    
    public synchronized void setDebugOut(final PrintStream out) {
        this.out = out;
    }
    
    public synchronized PrintStream getDebugOut() {
        if (this.out == null) {
            return System.out;
        }
        return this.out;
    }
    
    public synchronized Provider[] getProviders() {
        final Provider[] _providers = new Provider[this.providers.size()];
        this.providers.copyInto(_providers);
        return _providers;
    }
    
    public synchronized Provider getProvider(final String protocol) throws NoSuchProviderException {
        if (protocol == null || protocol.length() <= 0) {
            throw new NoSuchProviderException("Invalid protocol: null");
        }
        Provider _provider = null;
        final String _className = this.props.getProperty("mail." + protocol + ".class");
        if (_className != null) {
            if (this.debug) {
                this.pr("DEBUG: mail." + protocol + ".class property exists and points to " + _className);
            }
            _provider = this.providersByClassName.get(_className);
        }
        if (_provider != null) {
            return _provider;
        }
        _provider = this.providersByProtocol.get(protocol);
        if (_provider == null) {
            throw new NoSuchProviderException("No provider for " + protocol);
        }
        if (this.debug) {
            this.pr("DEBUG: getProvider() returning " + _provider.toString());
        }
        return _provider;
    }
    
    public synchronized void setProvider(final Provider provider) throws NoSuchProviderException {
        if (provider == null) {
            throw new NoSuchProviderException("Can't set null provider");
        }
        this.providersByProtocol.put(provider.getProtocol(), provider);
        ((Hashtable<String, String>)this.props).put("mail." + provider.getProtocol() + ".class", provider.getClassName());
    }
    
    public Store getStore() throws NoSuchProviderException {
        return this.getStore(this.getProperty("mail.store.protocol"));
    }
    
    public Store getStore(final String protocol) throws NoSuchProviderException {
        return this.getStore(new URLName(protocol, null, -1, null, null, null));
    }
    
    public Store getStore(final URLName url) throws NoSuchProviderException {
        final String protocol = url.getProtocol();
        final Provider p = this.getProvider(protocol);
        return this.getStore(p, url);
    }
    
    public Store getStore(final Provider provider) throws NoSuchProviderException {
        return this.getStore(provider, null);
    }
    
    private Store getStore(final Provider provider, final URLName url) throws NoSuchProviderException {
        if (provider == null || provider.getType() != Provider.Type.STORE) {
            throw new NoSuchProviderException("invalid provider");
        }
        try {
            return (Store)this.getService(provider, url);
        }
        catch (ClassCastException cce) {
            throw new NoSuchProviderException("incorrect class");
        }
    }
    
    public Folder getFolder(final URLName url) throws MessagingException {
        final Store store = this.getStore(url);
        store.connect();
        return store.getFolder(url);
    }
    
    public Transport getTransport() throws NoSuchProviderException {
        return this.getTransport(this.getProperty("mail.transport.protocol"));
    }
    
    public Transport getTransport(final String protocol) throws NoSuchProviderException {
        return this.getTransport(new URLName(protocol, null, -1, null, null, null));
    }
    
    public Transport getTransport(final URLName url) throws NoSuchProviderException {
        final String protocol = url.getProtocol();
        final Provider p = this.getProvider(protocol);
        return this.getTransport(p, url);
    }
    
    public Transport getTransport(final Provider provider) throws NoSuchProviderException {
        return this.getTransport(provider, null);
    }
    
    public Transport getTransport(final Address address) throws NoSuchProviderException {
        final String transportProtocol = ((Hashtable<K, String>)this.addressMap).get(address.getType());
        if (transportProtocol == null) {
            throw new NoSuchProviderException("No provider for Address type: " + address.getType());
        }
        return this.getTransport(transportProtocol);
    }
    
    private Transport getTransport(final Provider provider, final URLName url) throws NoSuchProviderException {
        if (provider == null || provider.getType() != Provider.Type.TRANSPORT) {
            throw new NoSuchProviderException("invalid provider");
        }
        try {
            return (Transport)this.getService(provider, url);
        }
        catch (ClassCastException cce) {
            throw new NoSuchProviderException("incorrect class");
        }
    }
    
    private Object getService(final Provider provider, URLName url) throws NoSuchProviderException {
        if (provider == null) {
            throw new NoSuchProviderException("null");
        }
        if (url == null) {
            url = new URLName(provider.getProtocol(), null, -1, null, null, null);
        }
        Object service = null;
        ClassLoader cl;
        if (this.authenticator != null) {
            cl = this.authenticator.getClass().getClassLoader();
        }
        else {
            cl = this.getClass().getClassLoader();
        }
        Class serviceClass = null;
        try {
            final ClassLoader ccl = getContextClassLoader();
            if (ccl != null) {
                try {
                    serviceClass = ccl.loadClass(provider.getClassName());
                }
                catch (ClassNotFoundException ex4) {}
            }
            if (serviceClass == null) {
                serviceClass = cl.loadClass(provider.getClassName());
            }
        }
        catch (Exception ex3) {
            try {
                serviceClass = Class.forName(provider.getClassName());
            }
            catch (Exception ex) {
                if (this.debug) {
                    ex.printStackTrace(this.getDebugOut());
                }
                throw new NoSuchProviderException(provider.getProtocol());
            }
        }
        try {
            final Class[] c = { Session.class, URLName.class };
            final Constructor cons = serviceClass.getConstructor((Class[])c);
            final Object[] o = { this, url };
            service = cons.newInstance(o);
        }
        catch (Exception ex2) {
            if (this.debug) {
                ex2.printStackTrace(this.getDebugOut());
            }
            throw new NoSuchProviderException(provider.getProtocol());
        }
        return service;
    }
    
    public void setPasswordAuthentication(final URLName url, final PasswordAuthentication pw) {
        if (pw == null) {
            this.authTable.remove(url);
        }
        else {
            this.authTable.put(url, pw);
        }
    }
    
    public PasswordAuthentication getPasswordAuthentication(final URLName url) {
        return this.authTable.get(url);
    }
    
    public PasswordAuthentication requestPasswordAuthentication(final InetAddress addr, final int port, final String protocol, final String prompt, final String defaultUserName) {
        if (this.authenticator != null) {
            return this.authenticator.requestPasswordAuthentication(addr, port, protocol, prompt, defaultUserName);
        }
        return null;
    }
    
    public Properties getProperties() {
        return this.props;
    }
    
    public String getProperty(final String name) {
        return this.props.getProperty(name);
    }
    
    private void loadProviders(final Class cl) {
        final StreamLoader loader = new StreamLoader() {
            public void load(final InputStream is) throws IOException {
                Session.this.loadProvidersFromStream(is);
            }
        };
        try {
            final String res = System.getProperty("java.home") + File.separator + "lib" + File.separator + "javamail.providers";
            this.loadFile(res, loader);
        }
        catch (SecurityException sex) {
            if (this.debug) {
                this.pr("DEBUG: can't get java.home: " + sex);
            }
        }
        this.loadAllResources("META-INF/javamail.providers", cl, loader);
        this.loadResource("/META-INF/javamail.default.providers", cl, loader);
        if (this.providers.size() == 0) {
            if (this.debug) {
                this.pr("DEBUG: failed to load any providers, using defaults");
            }
            this.addProvider(new Provider(Provider.Type.STORE, "imap", "com.sun.mail.imap.IMAPStore", "Sun Microsystems, Inc.", "1.4ea"));
            this.addProvider(new Provider(Provider.Type.STORE, "imaps", "com.sun.mail.imap.IMAPSSLStore", "Sun Microsystems, Inc.", "1.4ea"));
            this.addProvider(new Provider(Provider.Type.STORE, "pop3", "com.sun.mail.pop3.POP3Store", "Sun Microsystems, Inc.", "1.4ea"));
            this.addProvider(new Provider(Provider.Type.STORE, "pop3s", "com.sun.mail.pop3.POP3SSLStore", "Sun Microsystems, Inc.", "1.4ea"));
            this.addProvider(new Provider(Provider.Type.TRANSPORT, "smtp", "com.sun.mail.smtp.SMTPTransport", "Sun Microsystems, Inc.", "1.4ea"));
            this.addProvider(new Provider(Provider.Type.TRANSPORT, "smtps", "com.sun.mail.smtp.SMTPSSLTransport", "Sun Microsystems, Inc.", "1.4ea"));
        }
        if (this.debug) {
            this.pr("DEBUG: Tables of loaded providers");
            this.pr("DEBUG: Providers Listed By Class Name: " + this.providersByClassName.toString());
            this.pr("DEBUG: Providers Listed By Protocol: " + this.providersByProtocol.toString());
        }
    }
    
    private void loadProvidersFromStream(final InputStream is) throws IOException {
        if (is != null) {
            final LineInputStream lis = new LineInputStream(is);
            String currLine;
            while ((currLine = lis.readLine()) != null) {
                if (currLine.startsWith("#")) {
                    continue;
                }
                Provider.Type type = null;
                String protocol = null;
                String className = null;
                String vendor = null;
                String version = null;
                final StringTokenizer tuples = new StringTokenizer(currLine, ";");
                while (tuples.hasMoreTokens()) {
                    final String currTuple = tuples.nextToken().trim();
                    final int sep = currTuple.indexOf("=");
                    if (currTuple.startsWith("protocol=")) {
                        protocol = currTuple.substring(sep + 1);
                    }
                    else if (currTuple.startsWith("type=")) {
                        final String strType = currTuple.substring(sep + 1);
                        if (strType.equalsIgnoreCase("store")) {
                            type = Provider.Type.STORE;
                        }
                        else {
                            if (!strType.equalsIgnoreCase("transport")) {
                                continue;
                            }
                            type = Provider.Type.TRANSPORT;
                        }
                    }
                    else if (currTuple.startsWith("class=")) {
                        className = currTuple.substring(sep + 1);
                    }
                    else if (currTuple.startsWith("vendor=")) {
                        vendor = currTuple.substring(sep + 1);
                    }
                    else {
                        if (!currTuple.startsWith("version=")) {
                            continue;
                        }
                        version = currTuple.substring(sep + 1);
                    }
                }
                if (type == null || protocol == null || className == null || protocol.length() <= 0 || className.length() <= 0) {
                    if (!this.debug) {
                        continue;
                    }
                    this.pr("DEBUG: Bad provider entry: " + currLine);
                }
                else {
                    final Provider provider = new Provider(type, protocol, className, vendor, version);
                    this.addProvider(provider);
                }
            }
        }
    }
    
    public synchronized void addProvider(final Provider provider) {
        this.providers.addElement(provider);
        this.providersByClassName.put(provider.getClassName(), provider);
        if (!this.providersByProtocol.containsKey(provider.getProtocol())) {
            this.providersByProtocol.put(provider.getProtocol(), provider);
        }
    }
    
    private void loadAddressMap(final Class cl) {
        final StreamLoader loader = new StreamLoader() {
            public void load(final InputStream is) throws IOException {
                Session.this.addressMap.load(is);
            }
        };
        this.loadResource("/META-INF/javamail.default.address.map", cl, loader);
        this.loadAllResources("META-INF/javamail.address.map", cl, loader);
        try {
            final String res = System.getProperty("java.home") + File.separator + "lib" + File.separator + "javamail.address.map";
            this.loadFile(res, loader);
        }
        catch (SecurityException sex) {
            if (this.debug) {
                this.pr("DEBUG: can't get java.home: " + sex);
            }
        }
        if (this.addressMap.isEmpty()) {
            if (this.debug) {
                this.pr("DEBUG: failed to load address map, using defaults");
            }
            ((Hashtable<String, String>)this.addressMap).put("rfc822", "smtp");
        }
    }
    
    public synchronized void setProtocolForAddress(final String addresstype, final String protocol) {
        if (protocol == null) {
            this.addressMap.remove(addresstype);
        }
        else {
            ((Hashtable<String, String>)this.addressMap).put(addresstype, protocol);
        }
    }
    
    private void loadFile(final String name, final StreamLoader loader) {
        InputStream clis = null;
        try {
            clis = new BufferedInputStream(new FileInputStream(name));
            if (clis != null) {
                loader.load(clis);
                if (this.debug) {
                    this.pr("DEBUG: successfully loaded file: " + name);
                }
            }
            else if (this.debug) {
                this.pr("DEBUG: not loading file: " + name);
            }
        }
        catch (IOException e) {
            if (this.debug) {
                this.pr("DEBUG: " + e);
            }
        }
        catch (SecurityException sex) {
            if (this.debug) {
                this.pr("DEBUG: " + sex);
            }
        }
        finally {
            try {
                if (clis != null) {
                    clis.close();
                }
            }
            catch (IOException ex) {}
        }
    }
    
    private void loadResource(final String name, final Class cl, final StreamLoader loader) {
        InputStream clis = null;
        try {
            clis = getResourceAsStream(cl, name);
            if (clis != null) {
                loader.load(clis);
                if (this.debug) {
                    this.pr("DEBUG: successfully loaded resource: " + name);
                }
            }
            else if (this.debug) {
                this.pr("DEBUG: not loading resource: " + name);
            }
        }
        catch (IOException e) {
            if (this.debug) {
                this.pr("DEBUG: " + e);
            }
        }
        catch (SecurityException sex) {
            if (this.debug) {
                this.pr("DEBUG: " + sex);
            }
        }
        finally {
            try {
                if (clis != null) {
                    clis.close();
                }
            }
            catch (IOException ex) {}
        }
    }
    
    private void loadAllResources(final String name, final Class cl, final StreamLoader loader) {
        boolean anyLoaded = false;
        try {
            ClassLoader cld = null;
            cld = getContextClassLoader();
            if (cld == null) {
                cld = cl.getClassLoader();
            }
            URL[] urls;
            if (cld != null) {
                urls = getResources(cld, name);
            }
            else {
                urls = getSystemResources(name);
            }
            if (urls != null) {
                for (int i = 0; i < urls.length; ++i) {
                    final URL url = urls[i];
                    InputStream clis = null;
                    Label_0101: {
                        if (!this.debug) {
                            break Label_0101;
                        }
                        this.pr("DEBUG: URL " + url);
                        try {
                            clis = openStream(url);
                            if (clis != null) {
                                loader.load(clis);
                                anyLoaded = true;
                                if (this.debug) {
                                    this.pr("DEBUG: successfully loaded resource: " + url);
                                }
                            }
                            else if (this.debug) {
                                this.pr("DEBUG: not loading resource: " + url);
                            }
                        }
                        catch (IOException ioex) {
                            if (this.debug) {
                                this.pr("DEBUG: " + ioex);
                            }
                        }
                        catch (SecurityException sex) {
                            if (this.debug) {
                                this.pr("DEBUG: " + sex);
                            }
                        }
                        finally {
                            try {
                                if (clis != null) {
                                    clis.close();
                                }
                            }
                            catch (IOException ex2) {}
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            if (this.debug) {
                this.pr("DEBUG: " + ex);
            }
        }
        if (!anyLoaded) {
            if (this.debug) {
                this.pr("DEBUG: !anyLoaded");
            }
            this.loadResource("/" + name, cl, loader);
        }
    }
    
    private void pr(final String str) {
        this.getDebugOut().println(str);
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
    
    private static InputStream getResourceAsStream(final Class c, final String name) throws IOException {
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<InputStream>)new PrivilegedExceptionAction() {
                public Object run() throws IOException {
                    return c.getResourceAsStream(name);
                }
            });
        }
        catch (PrivilegedActionException e) {
            throw (IOException)e.getException();
        }
    }
    
    private static URL[] getResources(final ClassLoader cl, final String name) {
        return AccessController.doPrivileged((PrivilegedAction<URL[]>)new PrivilegedAction() {
            public Object run() {
                URL[] ret = null;
                try {
                    final Vector v = new Vector();
                    final Enumeration e = cl.getResources(name);
                    while (e != null && e.hasMoreElements()) {
                        final URL url = e.nextElement();
                        if (url != null) {
                            v.addElement(url);
                        }
                    }
                    if (v.size() > 0) {
                        ret = new URL[v.size()];
                        v.copyInto(ret);
                    }
                }
                catch (IOException ioex) {}
                catch (SecurityException ex) {}
                return ret;
            }
        });
    }
    
    private static URL[] getSystemResources(final String name) {
        return AccessController.doPrivileged((PrivilegedAction<URL[]>)new PrivilegedAction() {
            public Object run() {
                URL[] ret = null;
                try {
                    final Vector v = new Vector();
                    final Enumeration e = ClassLoader.getSystemResources(name);
                    while (e != null && e.hasMoreElements()) {
                        final URL url = e.nextElement();
                        if (url != null) {
                            v.addElement(url);
                        }
                    }
                    if (v.size() > 0) {
                        ret = new URL[v.size()];
                        v.copyInto(ret);
                    }
                }
                catch (IOException ioex) {}
                catch (SecurityException ex) {}
                return ret;
            }
        });
    }
    
    private static InputStream openStream(final URL url) throws IOException {
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<InputStream>)new PrivilegedExceptionAction() {
                public Object run() throws IOException {
                    return url.openStream();
                }
            });
        }
        catch (PrivilegedActionException e) {
            throw (IOException)e.getException();
        }
    }
    
    static {
        Session.defaultSession = null;
    }
}
