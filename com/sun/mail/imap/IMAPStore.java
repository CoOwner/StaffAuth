package com.sun.mail.imap;

import java.util.*;
import com.sun.mail.imap.protocol.*;
import java.io.*;
import java.net.*;
import javax.mail.*;
import com.sun.mail.iap.*;

public class IMAPStore extends Store implements QuotaAwareStore, ResponseHandler
{
    private String name;
    private int defaultPort;
    private boolean isSSL;
    private int port;
    private int blksize;
    private int statusCacheTimeout;
    private int appendBufferSize;
    private String host;
    private String user;
    private String password;
    private String proxyAuthUser;
    private String authorizationID;
    private String saslRealm;
    private Namespaces namespaces;
    private boolean disableAuthLogin;
    private boolean disableAuthPlain;
    private boolean enableStartTLS;
    private boolean enableSASL;
    private String[] saslMechanisms;
    private boolean forcePasswordRefresh;
    private boolean debug;
    private PrintStream out;
    private ConnectionPool pool;
    
    public IMAPStore(final Session session, final URLName url) {
        this(session, url, "imap", 143, false);
    }
    
    protected IMAPStore(final Session session, final URLName url, String name, final int defaultPort, final boolean isSSL) {
        super(session, url);
        this.name = "imap";
        this.defaultPort = 143;
        this.isSSL = false;
        this.port = -1;
        this.blksize = 16384;
        this.statusCacheTimeout = 1000;
        this.appendBufferSize = -1;
        this.disableAuthLogin = false;
        this.disableAuthPlain = false;
        this.enableStartTLS = false;
        this.enableSASL = false;
        this.forcePasswordRefresh = false;
        this.pool = new ConnectionPool();
        if (url != null) {
            name = url.getProtocol();
        }
        this.name = name;
        this.defaultPort = defaultPort;
        this.isSSL = isSSL;
        this.pool.lastTimePruned = System.currentTimeMillis();
        this.debug = session.getDebug();
        this.out = session.getDebugOut();
        if (this.out == null) {
            this.out = System.out;
        }
        String s = session.getProperty("mail." + name + ".connectionpool.debug");
        if (s != null && s.equalsIgnoreCase("true")) {
            this.pool.debug = true;
        }
        s = session.getProperty("mail." + name + ".partialfetch");
        if (s != null && s.equalsIgnoreCase("false")) {
            this.blksize = -1;
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.partialfetch: false");
            }
        }
        else {
            if ((s = session.getProperty("mail." + name + ".fetchsize")) != null) {
                this.blksize = Integer.parseInt(s);
            }
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.fetchsize: " + this.blksize);
            }
        }
        s = session.getProperty("mail." + name + ".statuscachetimeout");
        if (s != null) {
            this.statusCacheTimeout = Integer.parseInt(s);
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.statuscachetimeout: " + this.statusCacheTimeout);
            }
        }
        s = session.getProperty("mail." + name + ".appendbuffersize");
        if (s != null) {
            this.appendBufferSize = Integer.parseInt(s);
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.appendbuffersize: " + this.appendBufferSize);
            }
        }
        s = session.getProperty("mail." + name + ".connectionpoolsize");
        if (s != null) {
            try {
                final int size = Integer.parseInt(s);
                if (size > 0) {
                    this.pool.poolSize = size;
                }
            }
            catch (NumberFormatException ex) {}
            if (this.pool.debug) {
                this.out.println("DEBUG: mail.imap.connectionpoolsize: " + this.pool.poolSize);
            }
        }
        s = session.getProperty("mail." + name + ".connectionpooltimeout");
        if (s != null) {
            try {
                final int connectionPoolTimeout = Integer.parseInt(s);
                if (connectionPoolTimeout > 0) {
                    this.pool.clientTimeoutInterval = connectionPoolTimeout;
                }
            }
            catch (NumberFormatException ex2) {}
            if (this.pool.debug) {
                this.out.println("DEBUG: mail.imap.connectionpooltimeout: " + this.pool.clientTimeoutInterval);
            }
        }
        s = session.getProperty("mail." + name + ".servertimeout");
        if (s != null) {
            try {
                final int serverTimeout = Integer.parseInt(s);
                if (serverTimeout > 0) {
                    this.pool.serverTimeoutInterval = serverTimeout;
                }
            }
            catch (NumberFormatException ex3) {}
            if (this.pool.debug) {
                this.out.println("DEBUG: mail.imap.servertimeout: " + this.pool.serverTimeoutInterval);
            }
        }
        s = session.getProperty("mail." + name + ".separatestoreconnection");
        if (s != null && s.equalsIgnoreCase("true")) {
            if (this.pool.debug) {
                this.out.println("DEBUG: dedicate a store connection");
            }
            this.pool.separateStoreConnection = true;
        }
        s = session.getProperty("mail." + name + ".proxyauth.user");
        if (s != null) {
            this.proxyAuthUser = s;
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.proxyauth.user: " + this.proxyAuthUser);
            }
        }
        s = session.getProperty("mail." + name + ".auth.login.disable");
        if (s != null && s.equalsIgnoreCase("true")) {
            if (this.debug) {
                this.out.println("DEBUG: disable AUTH=LOGIN");
            }
            this.disableAuthLogin = true;
        }
        s = session.getProperty("mail." + name + ".auth.plain.disable");
        if (s != null && s.equalsIgnoreCase("true")) {
            if (this.debug) {
                this.out.println("DEBUG: disable AUTH=PLAIN");
            }
            this.disableAuthPlain = true;
        }
        s = session.getProperty("mail." + name + ".starttls.enable");
        if (s != null && s.equalsIgnoreCase("true")) {
            if (this.debug) {
                this.out.println("DEBUG: enable STARTTLS");
            }
            this.enableStartTLS = true;
        }
        s = session.getProperty("mail." + name + ".sasl.enable");
        if (s != null && s.equalsIgnoreCase("true")) {
            if (this.debug) {
                this.out.println("DEBUG: enable SASL");
            }
            this.enableSASL = true;
        }
        if (this.enableSASL) {
            s = session.getProperty("mail." + name + ".sasl.mechanisms");
            if (s != null && s.length() > 0) {
                if (this.debug) {
                    this.out.println("DEBUG: SASL mechanisms allowed: " + s);
                }
                final Vector v = new Vector(5);
                final StringTokenizer st = new StringTokenizer(s, " ,");
                while (st.hasMoreTokens()) {
                    final String m = st.nextToken();
                    if (m.length() > 0) {
                        v.addElement(m);
                    }
                }
                v.copyInto(this.saslMechanisms = new String[v.size()]);
            }
        }
        s = session.getProperty("mail." + name + ".sasl.authorizationid");
        if (s != null) {
            this.authorizationID = s;
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.sasl.authorizationid: " + this.authorizationID);
            }
        }
        s = session.getProperty("mail." + name + ".sasl.realm");
        if (s != null) {
            this.saslRealm = s;
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.sasl.realm: " + this.saslRealm);
            }
        }
        s = session.getProperty("mail." + name + ".forcepasswordrefresh");
        if (s != null && s.equalsIgnoreCase("true")) {
            if (this.debug) {
                this.out.println("DEBUG: enable forcePasswordRefresh");
            }
            this.forcePasswordRefresh = true;
        }
    }
    
    protected synchronized boolean protocolConnect(final String host, final int pport, final String user, final String password) throws MessagingException {
        IMAPProtocol protocol = null;
        if (host == null || password == null || user == null) {
            if (this.debug) {
                this.out.println("DEBUG: protocolConnect returning false, host=" + host + ", user=" + user + ", password=" + ((password != null) ? "<non-null>" : "<null>"));
            }
            return false;
        }
        if (pport != -1) {
            this.port = pport;
        }
        else {
            final String portstring = this.session.getProperty("mail." + this.name + ".port");
            if (portstring != null) {
                this.port = Integer.parseInt(portstring);
            }
        }
        if (this.port == -1) {
            this.port = this.defaultPort;
        }
        try {
            final boolean poolEmpty;
            synchronized (this.pool) {
                poolEmpty = this.pool.authenticatedConnections.isEmpty();
            }
            if (poolEmpty) {
                protocol = new IMAPProtocol(this.name, host, this.port, this.session.getDebug(), this.session.getDebugOut(), this.session.getProperties(), this.isSSL);
                if (this.debug) {
                    this.out.println("DEBUG: protocolConnect login, host=" + host + ", user=" + user + ", password=" + ((password != null) ? "<non-null>" : "<null>"));
                }
                this.login(protocol, user, password);
                protocol.addResponseHandler(this);
                this.host = host;
                this.user = user;
                this.password = password;
                synchronized (this.pool) {
                    this.pool.authenticatedConnections.addElement(protocol);
                }
            }
        }
        catch (CommandFailedException cex) {
            protocol.disconnect();
            protocol = null;
            throw new AuthenticationFailedException(cex.getResponse().getRest());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        catch (IOException ioex) {
            throw new MessagingException(ioex.getMessage(), ioex);
        }
        return true;
    }
    
    private void login(final IMAPProtocol p, final String u, final String pw) throws ProtocolException {
        if (this.enableStartTLS && p.hasCapability("STARTTLS")) {
            p.startTLS();
            p.capability();
        }
        if (p.isAuthenticated()) {
            return;
        }
        String authzid;
        if (this.authorizationID != null) {
            authzid = this.authorizationID;
        }
        else if (this.proxyAuthUser != null) {
            authzid = this.proxyAuthUser;
        }
        else {
            authzid = u;
        }
        if (this.enableSASL) {
            p.sasllogin(this.saslMechanisms, this.saslRealm, authzid, u, pw);
            if (p.isAuthenticated()) {
                if (this.proxyAuthUser != null) {
                    p.proxyauth(this.proxyAuthUser);
                }
                return;
            }
        }
        if (p.hasCapability("AUTH=PLAIN") && !this.disableAuthPlain) {
            p.authplain(authzid, u, pw);
        }
        else if ((p.hasCapability("AUTH-LOGIN") || p.hasCapability("AUTH=LOGIN")) && !this.disableAuthLogin) {
            p.authlogin(u, pw);
        }
        else {
            if (p.hasCapability("LOGINDISABLED")) {
                throw new ProtocolException("No login methods supported!");
            }
            p.login(u, pw);
        }
        if (this.proxyAuthUser != null) {
            p.proxyauth(this.proxyAuthUser);
        }
    }
    
    public void setUsername(final String user) {
        this.user = user;
    }
    
    public void setPassword(final String password) {
        this.password = password;
    }
    
    IMAPProtocol getProtocol(final IMAPFolder folder) throws MessagingException {
        IMAPProtocol p = null;
        while (p == null) {
            synchronized (this.pool) {
                if (this.pool.authenticatedConnections.isEmpty() || (this.pool.separateStoreConnection && this.pool.authenticatedConnections.size() == 1) || (this.pool.borrowedStoreConnections > 0L && this.pool.authenticatedConnections.size() == 1)) {
                    if (this.debug) {
                        this.out.println("DEBUG: no connections in the pool, creating a new one");
                    }
                    try {
                        if (this.forcePasswordRefresh) {
                            InetAddress addr;
                            try {
                                addr = InetAddress.getByName(this.host);
                            }
                            catch (UnknownHostException e) {
                                addr = null;
                            }
                            final PasswordAuthentication pa = this.session.requestPasswordAuthentication(addr, this.port, this.name, null, this.user);
                            if (pa != null) {
                                this.user = pa.getUserName();
                                this.password = pa.getPassword();
                            }
                        }
                        p = new IMAPProtocol(this.name, this.host, this.port, this.session.getDebug(), this.session.getDebugOut(), this.session.getProperties(), this.isSSL);
                        this.login(p, this.user, this.password);
                    }
                    catch (Exception ex1) {
                        if (p != null) {
                            try {
                                p.disconnect();
                            }
                            catch (Exception ex2) {}
                        }
                        p = null;
                    }
                    if (p == null) {
                        throw new MessagingException("connection failure");
                    }
                }
                else {
                    if (this.debug) {
                        this.out.println("DEBUG: connection available -- size: " + this.pool.authenticatedConnections.size());
                    }
                    p = this.pool.authenticatedConnections.lastElement();
                    this.pool.authenticatedConnections.removeElement(p);
                    final long lastUsed = System.currentTimeMillis() - p.getTimestamp();
                    if (lastUsed > this.pool.serverTimeoutInterval) {
                        try {
                            p.noop();
                        }
                        catch (ProtocolException pex) {
                            try {
                                p.removeResponseHandler(this);
                                p.disconnect();
                            }
                            catch (Exception ex3) {}
                            p = null;
                            continue;
                        }
                    }
                    p.removeResponseHandler(this);
                }
                this.timeoutConnections();
                if (folder == null) {
                    continue;
                }
                if (this.pool.folders == null) {
                    this.pool.folders = new Vector();
                }
                this.pool.folders.addElement(folder);
            }
        }
        return p;
    }
    
    IMAPProtocol getStoreProtocol() throws ProtocolException {
        IMAPProtocol p = null;
        synchronized (this.pool) {
            if (this.pool.authenticatedConnections.isEmpty()) {
                if (this.pool.debug) {
                    this.out.println("DEBUG: getStoreProtocol() - no connections in the pool, creating a new one");
                }
                try {
                    p = new IMAPProtocol(this.name, this.host, this.port, this.session.getDebug(), this.session.getDebugOut(), this.session.getProperties(), this.isSSL);
                    this.login(p, this.user, this.password);
                }
                catch (Exception ex1) {
                    if (p != null) {
                        try {
                            p.logout();
                        }
                        catch (Exception ex2) {}
                    }
                    p = null;
                }
                if (p == null) {
                    throw new ConnectionException("failed to create new store connection");
                }
                p.addResponseHandler(this);
                this.pool.authenticatedConnections.addElement(p);
            }
            else {
                if (this.pool.debug) {
                    this.out.println("DEBUG: getStoreProtocol() - connection available -- size: " + this.pool.authenticatedConnections.size());
                }
                p = this.pool.authenticatedConnections.firstElement();
            }
            if (!this.pool.separateStoreConnection) {
                this.pool.borrowedStoreConnections++;
                if (this.pool.debug) {
                    this.out.println("DEBUG: getStoreProtocol() -- borrowedStoreConnections: " + this.pool.borrowedStoreConnections);
                }
            }
            this.timeoutConnections();
            return p;
        }
    }
    
    boolean allowReadOnlySelect() {
        final String s = this.session.getProperty("mail." + this.name + ".allowreadonlyselect");
        return s != null && s.equalsIgnoreCase("true");
    }
    
    boolean hasSeparateStoreConnection() {
        return this.pool.separateStoreConnection;
    }
    
    boolean getConnectionPoolDebug() {
        return this.pool.debug;
    }
    
    boolean isConnectionPoolFull() {
        synchronized (this.pool) {
            if (this.pool.debug) {
                this.out.println("DEBUG: current size: " + this.pool.authenticatedConnections.size() + "   pool size: " + this.pool.poolSize);
            }
            return this.pool.authenticatedConnections.size() >= this.pool.poolSize;
        }
    }
    
    void releaseProtocol(final IMAPFolder folder, final IMAPProtocol protocol) {
        synchronized (this.pool) {
            if (protocol != null) {
                if (!this.isConnectionPoolFull()) {
                    protocol.addResponseHandler(this);
                    this.pool.authenticatedConnections.addElement(protocol);
                    if (this.debug) {
                        this.out.println("DEBUG: added an Authenticated connection -- size: " + this.pool.authenticatedConnections.size());
                    }
                }
                else {
                    if (this.debug) {
                        this.out.println("DEBUG: pool is full, not adding an Authenticated connection");
                    }
                    try {
                        protocol.logout();
                    }
                    catch (ProtocolException ex) {}
                }
            }
            if (this.pool.folders != null) {
                this.pool.folders.removeElement(folder);
            }
            this.timeoutConnections();
        }
    }
    
    void releaseStoreProtocol(final IMAPProtocol protocol) {
        if (protocol == null) {
            return;
        }
        synchronized (this.pool) {
            if (!this.pool.separateStoreConnection) {
                this.pool.borrowedStoreConnections--;
                if (this.pool.debug) {
                    this.out.println("DEBUG: releaseStoreProtocol() -- borrowedStoreConnections: " + this.pool.borrowedStoreConnections);
                }
            }
            this.timeoutConnections();
        }
    }
    
    private void emptyConnectionPool(final boolean force) {
        synchronized (this.pool) {
            for (int index = this.pool.authenticatedConnections.size() - 1; index >= 0; --index) {
                try {
                    final IMAPProtocol p = this.pool.authenticatedConnections.elementAt(index);
                    p.removeResponseHandler(this);
                    if (force) {
                        p.disconnect();
                    }
                    else {
                        p.logout();
                    }
                }
                catch (ProtocolException ex) {}
            }
            this.pool.authenticatedConnections.removeAllElements();
        }
        if (this.pool.debug) {
            this.out.println("DEBUG: removed all authenticated connections");
        }
    }
    
    private void timeoutConnections() {
        synchronized (this.pool) {
            if (System.currentTimeMillis() - this.pool.lastTimePruned > this.pool.pruningInterval && this.pool.authenticatedConnections.size() > 1) {
                if (this.pool.debug) {
                    this.out.println("DEBUG: checking for connections to prune: " + (System.currentTimeMillis() - this.pool.lastTimePruned));
                    this.out.println("DEBUG: clientTimeoutInterval: " + this.pool.clientTimeoutInterval);
                }
                for (int index = this.pool.authenticatedConnections.size() - 1; index > 0; --index) {
                    final IMAPProtocol p = this.pool.authenticatedConnections.elementAt(index);
                    if (this.pool.debug) {
                        this.out.println("DEBUG: protocol last used: " + (System.currentTimeMillis() - p.getTimestamp()));
                    }
                    if (System.currentTimeMillis() - p.getTimestamp() > this.pool.clientTimeoutInterval) {
                        if (this.pool.debug) {
                            this.out.println("DEBUG: authenticated connection timed out");
                            this.out.println("DEBUG: logging out the connection");
                        }
                        p.removeResponseHandler(this);
                        this.pool.authenticatedConnections.removeElementAt(index);
                        try {
                            p.logout();
                        }
                        catch (ProtocolException ex) {}
                    }
                }
                this.pool.lastTimePruned = System.currentTimeMillis();
            }
        }
    }
    
    int getFetchBlockSize() {
        return this.blksize;
    }
    
    Session getSession() {
        return this.session;
    }
    
    int getStatusCacheTimeout() {
        return this.statusCacheTimeout;
    }
    
    int getAppendBufferSize() {
        return this.appendBufferSize;
    }
    
    public boolean hasCapability(final String capability) throws MessagingException {
        IMAPProtocol p = null;
        try {
            p = this.getStoreProtocol();
            return p.hasCapability(capability);
        }
        catch (ProtocolException pex) {
            if (p == null) {
                this.cleanup();
            }
            throw new MessagingException(pex.getMessage(), pex);
        }
        finally {
            this.releaseStoreProtocol(p);
        }
    }
    
    public synchronized boolean isConnected() {
        if (!super.isConnected()) {
            return false;
        }
        IMAPProtocol p = null;
        try {
            p = this.getStoreProtocol();
            p.noop();
        }
        catch (ProtocolException pex) {
            if (p == null) {
                this.cleanup();
            }
        }
        finally {
            this.releaseStoreProtocol(p);
        }
        return super.isConnected();
    }
    
    public void close() throws MessagingException {
        if (!super.isConnected()) {
            return;
        }
        IMAPProtocol protocol = null;
        try {
            final boolean isEmpty;
            synchronized (this.pool) {
                isEmpty = this.pool.authenticatedConnections.isEmpty();
            }
            if (isEmpty) {
                if (this.pool.debug) {
                    this.out.println("DEBUG: close() - no connections ");
                }
                this.cleanup();
                return;
            }
            protocol = this.getStoreProtocol();
            synchronized (this.pool) {
                this.pool.authenticatedConnections.removeElement(protocol);
            }
            protocol.logout();
        }
        catch (ProtocolException pex) {
            this.cleanup();
            throw new MessagingException(pex.getMessage(), pex);
        }
        finally {
            this.releaseStoreProtocol(protocol);
        }
    }
    
    protected void finalize() throws Throwable {
        super.finalize();
        this.close();
    }
    
    private synchronized void cleanup() {
        this.cleanup(false);
    }
    
    private synchronized void cleanup(final boolean force) {
        if (this.debug) {
            this.out.println("DEBUG: IMAPStore cleanup, force " + force);
        }
        Vector foldersCopy = null;
        boolean done = true;
        while (true) {
            synchronized (this.pool) {
                if (this.pool.folders != null) {
                    done = false;
                    foldersCopy = this.pool.folders;
                    this.pool.folders = null;
                }
                else {
                    done = true;
                }
            }
            if (done) {
                break;
            }
            for (int i = 0, fsize = foldersCopy.size(); i < fsize; ++i) {
                final IMAPFolder f = foldersCopy.elementAt(i);
                try {
                    if (force) {
                        if (this.debug) {
                            this.out.println("DEBUG: force folder to close");
                        }
                        f.forceClose();
                    }
                    else {
                        if (this.debug) {
                            this.out.println("DEBUG: close folder");
                        }
                        f.close(false);
                    }
                }
                catch (MessagingException mex) {}
                catch (IllegalStateException ex) {}
            }
        }
        synchronized (this.pool) {
            this.emptyConnectionPool(force);
        }
        try {
            super.close();
        }
        catch (MessagingException ex2) {}
        if (this.debug) {
            this.out.println("DEBUG: IMAPStore cleanup done");
        }
    }
    
    public Folder getDefaultFolder() throws MessagingException {
        this.checkConnected();
        return new DefaultFolder(this);
    }
    
    public Folder getFolder(final String name) throws MessagingException {
        this.checkConnected();
        return new IMAPFolder(name, '\uffff', this);
    }
    
    public Folder getFolder(final URLName url) throws MessagingException {
        this.checkConnected();
        return new IMAPFolder(url.getFile(), '\uffff', this);
    }
    
    public Folder[] getPersonalNamespaces() throws MessagingException {
        final Namespaces ns = this.getNamespaces();
        if (ns == null || ns.personal == null) {
            return super.getPersonalNamespaces();
        }
        return this.namespaceToFolders(ns.personal, null);
    }
    
    public Folder[] getUserNamespaces(final String user) throws MessagingException {
        final Namespaces ns = this.getNamespaces();
        if (ns == null || ns.otherUsers == null) {
            return super.getUserNamespaces(user);
        }
        return this.namespaceToFolders(ns.otherUsers, user);
    }
    
    public Folder[] getSharedNamespaces() throws MessagingException {
        final Namespaces ns = this.getNamespaces();
        if (ns == null || ns.shared == null) {
            return super.getSharedNamespaces();
        }
        return this.namespaceToFolders(ns.shared, null);
    }
    
    private synchronized Namespaces getNamespaces() throws MessagingException {
        this.checkConnected();
        IMAPProtocol p = null;
        if (this.namespaces == null) {
            try {
                p = this.getStoreProtocol();
                this.namespaces = p.namespace();
            }
            catch (BadCommandException bex) {}
            catch (ConnectionException cex) {
                throw new StoreClosedException(this, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
            finally {
                this.releaseStoreProtocol(p);
                if (p == null) {
                    this.cleanup();
                }
            }
        }
        return this.namespaces;
    }
    
    private Folder[] namespaceToFolders(final Namespaces.Namespace[] ns, final String user) {
        final Folder[] fa = new Folder[ns.length];
        for (int i = 0; i < fa.length; ++i) {
            String name = ns[i].prefix;
            if (user == null) {
                final int len = name.length();
                if (len > 0 && name.charAt(len - 1) == ns[i].delimiter) {
                    name = name.substring(0, len - 1);
                }
            }
            else {
                name += user;
            }
            fa[i] = new IMAPFolder(name, ns[i].delimiter, this, user == null);
        }
        return fa;
    }
    
    public Quota[] getQuota(final String root) throws MessagingException {
        Quota[] qa = null;
        IMAPProtocol p = null;
        try {
            p = this.getStoreProtocol();
            qa = p.getQuotaRoot(root);
        }
        catch (BadCommandException bex) {
            throw new MessagingException("QUOTA not supported", bex);
        }
        catch (ConnectionException cex) {
            throw new StoreClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        finally {
            this.releaseStoreProtocol(p);
            if (p == null) {
                this.cleanup();
            }
        }
        return qa;
    }
    
    public void setQuota(final Quota quota) throws MessagingException {
        IMAPProtocol p = null;
        try {
            p = this.getStoreProtocol();
            p.setQuota(quota);
        }
        catch (BadCommandException bex) {
            throw new MessagingException("QUOTA not supported", bex);
        }
        catch (ConnectionException cex) {
            throw new StoreClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        finally {
            this.releaseStoreProtocol(p);
            if (p == null) {
                this.cleanup();
            }
        }
    }
    
    private void checkConnected() {
        if (!super.isConnected()) {
            throw new IllegalStateException("Not connected");
        }
    }
    
    public void handleResponse(final Response r) {
        if (r.isOK() || r.isNO() || r.isBAD() || r.isBYE()) {
            this.handleResponseCode(r);
        }
        if (r.isBYE()) {
            if (this.debug) {
                this.out.println("DEBUG: IMAPStore connection dead");
            }
            if (super.isConnected()) {
                this.cleanup(r.isSynthetic());
            }
        }
    }
    
    void handleResponseCode(final Response r) {
        String s = r.getRest();
        boolean isAlert = false;
        if (s.startsWith("[")) {
            final int i = s.indexOf(93);
            if (i > 0 && s.substring(0, i + 1).equalsIgnoreCase("[ALERT]")) {
                isAlert = true;
            }
            s = s.substring(i + 1).trim();
        }
        if (isAlert) {
            this.notifyStoreListeners(1, s);
        }
        else if (r.isUnTagged() && s.length() > 0) {
            this.notifyStoreListeners(2, s);
        }
    }
    
    static class ConnectionPool
    {
        private Vector authenticatedConnections;
        private Vector folders;
        private boolean separateStoreConnection;
        private long borrowedStoreConnections;
        private long clientTimeoutInterval;
        private long serverTimeoutInterval;
        private long lastTimePruned;
        private int poolSize;
        private long pruningInterval;
        private boolean debug;
        
        ConnectionPool() {
            this.authenticatedConnections = new Vector();
            this.separateStoreConnection = false;
            this.borrowedStoreConnections = 0L;
            this.clientTimeoutInterval = 45000L;
            this.serverTimeoutInterval = 1800000L;
            this.poolSize = 1;
            this.pruningInterval = 60000L;
            this.debug = false;
        }
    }
}
