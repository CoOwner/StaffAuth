package javax.mail;

import java.util.*;
import java.net.*;
import javax.mail.event.*;

public abstract class Service
{
    protected Session session;
    protected URLName url;
    protected boolean debug;
    private boolean connected;
    private Vector connectionListeners;
    private EventQueue q;
    private Object qLock;
    
    protected Service(final Session session, final URLName urlname) {
        this.url = null;
        this.debug = false;
        this.connected = false;
        this.connectionListeners = null;
        this.qLock = new Object();
        this.session = session;
        this.url = urlname;
        this.debug = session.getDebug();
    }
    
    public void connect() throws MessagingException {
        this.connect(null, null, null);
    }
    
    public void connect(final String host, final String user, final String password) throws MessagingException {
        this.connect(host, -1, user, password);
    }
    
    public void connect(final String user, final String password) throws MessagingException {
        this.connect(null, user, password);
    }
    
    public void connect(String host, int port, String user, String password) throws MessagingException {
        if (this.isConnected()) {
            throw new IllegalStateException("already connected");
        }
        boolean connected = false;
        boolean save = false;
        String protocol = null;
        String file = null;
        if (this.url != null) {
            protocol = this.url.getProtocol();
            if (host == null) {
                host = this.url.getHost();
            }
            if (port == -1) {
                port = this.url.getPort();
            }
            if (user == null) {
                user = this.url.getUsername();
                if (password == null) {
                    password = this.url.getPassword();
                }
            }
            else if (password == null && user.equals(this.url.getUsername())) {
                password = this.url.getPassword();
            }
            file = this.url.getFile();
        }
        if (protocol != null) {
            if (host == null) {
                host = this.session.getProperty("mail." + protocol + ".host");
            }
            if (user == null) {
                user = this.session.getProperty("mail." + protocol + ".user");
            }
        }
        if (host == null) {
            host = this.session.getProperty("mail.host");
        }
        if (user == null) {
            user = this.session.getProperty("mail.user");
        }
        if (user == null) {
            try {
                user = System.getProperty("user.name");
            }
            catch (SecurityException sex) {
                if (this.debug) {
                    sex.printStackTrace(this.session.getDebugOut());
                }
            }
        }
        if (password == null && this.url != null) {
            this.setURLName(new URLName(protocol, host, port, file, user, password));
            final PasswordAuthentication pw = this.session.getPasswordAuthentication(this.getURLName());
            if (pw != null) {
                if (user == null) {
                    user = pw.getUserName();
                    password = pw.getPassword();
                }
                else if (user.equals(pw.getUserName())) {
                    password = pw.getPassword();
                }
            }
            else {
                save = true;
            }
        }
        AuthenticationFailedException authEx = null;
        try {
            connected = this.protocolConnect(host, port, user, password);
        }
        catch (AuthenticationFailedException ex) {
            authEx = ex;
        }
        if (!connected) {
            InetAddress addr;
            try {
                addr = InetAddress.getByName(host);
            }
            catch (UnknownHostException e) {
                addr = null;
            }
            final PasswordAuthentication pw = this.session.requestPasswordAuthentication(addr, port, protocol, null, user);
            if (pw != null) {
                user = pw.getUserName();
                password = pw.getPassword();
                connected = this.protocolConnect(host, port, user, password);
            }
        }
        if (connected) {
            this.setURLName(new URLName(protocol, host, port, file, user, password));
            if (save) {
                this.session.setPasswordAuthentication(this.getURLName(), new PasswordAuthentication(user, password));
            }
            this.setConnected(true);
            this.notifyConnectionListeners(1);
            return;
        }
        if (authEx != null) {
            throw authEx;
        }
        throw new AuthenticationFailedException();
    }
    
    protected boolean protocolConnect(final String host, final int port, final String user, final String password) throws MessagingException {
        return false;
    }
    
    public boolean isConnected() {
        return this.connected;
    }
    
    protected void setConnected(final boolean connected) {
        this.connected = connected;
    }
    
    public synchronized void close() throws MessagingException {
        this.setConnected(false);
        this.notifyConnectionListeners(3);
    }
    
    public URLName getURLName() {
        if (this.url != null && (this.url.getPassword() != null || this.url.getFile() != null)) {
            return new URLName(this.url.getProtocol(), this.url.getHost(), this.url.getPort(), null, this.url.getUsername(), null);
        }
        return this.url;
    }
    
    protected void setURLName(final URLName url) {
        this.url = url;
    }
    
    public synchronized void addConnectionListener(final ConnectionListener l) {
        if (this.connectionListeners == null) {
            this.connectionListeners = new Vector();
        }
        this.connectionListeners.addElement(l);
    }
    
    public synchronized void removeConnectionListener(final ConnectionListener l) {
        if (this.connectionListeners != null) {
            this.connectionListeners.removeElement(l);
        }
    }
    
    protected synchronized void notifyConnectionListeners(final int type) {
        if (this.connectionListeners != null) {
            final ConnectionEvent e = new ConnectionEvent(this, type);
            this.queueEvent(e, this.connectionListeners);
        }
        if (type == 3) {
            this.terminateQueue();
        }
    }
    
    public String toString() {
        final URLName url = this.getURLName();
        if (url != null) {
            return url.toString();
        }
        return super.toString();
    }
    
    protected void queueEvent(final MailEvent event, final Vector vector) {
        synchronized (this.qLock) {
            if (this.q == null) {
                this.q = new EventQueue();
            }
        }
        final Vector v = (Vector)vector.clone();
        this.q.enqueue(event, v);
    }
    
    private void terminateQueue() {
        synchronized (this.qLock) {
            if (this.q != null) {
                final Vector dummyListeners = new Vector();
                dummyListeners.setSize(1);
                this.q.enqueue(new TerminatorEvent(), dummyListeners);
                this.q = null;
            }
        }
    }
    
    protected void finalize() throws Throwable {
        super.finalize();
        this.terminateQueue();
    }
    
    static class TerminatorEvent extends MailEvent
    {
        private static final long serialVersionUID = 5542172141759168416L;
        
        TerminatorEvent() {
            super(new Object());
        }
        
        public void dispatch(final Object listener) {
            Thread.currentThread().interrupt();
        }
    }
}
