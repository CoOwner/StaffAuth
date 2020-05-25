package javax.mail;

import java.util.*;
import javax.mail.search.*;
import javax.mail.event.*;

public abstract class Folder
{
    protected Store store;
    protected int mode;
    public static final int HOLDS_MESSAGES = 1;
    public static final int HOLDS_FOLDERS = 2;
    public static final int READ_ONLY = 1;
    public static final int READ_WRITE = 2;
    private volatile Vector connectionListeners;
    private volatile Vector folderListeners;
    private volatile Vector messageCountListeners;
    private volatile Vector messageChangedListeners;
    private EventQueue q;
    private Object qLock;
    
    protected Folder(final Store store) {
        this.mode = -1;
        this.connectionListeners = null;
        this.folderListeners = null;
        this.messageCountListeners = null;
        this.messageChangedListeners = null;
        this.qLock = new Object();
        this.store = store;
    }
    
    public abstract String getName();
    
    public abstract String getFullName();
    
    public URLName getURLName() throws MessagingException {
        final URLName storeURL = this.getStore().getURLName();
        final String fullname = this.getFullName();
        final StringBuffer encodedName = new StringBuffer();
        final char separator = this.getSeparator();
        if (fullname != null) {
            encodedName.append(fullname);
        }
        return new URLName(storeURL.getProtocol(), storeURL.getHost(), storeURL.getPort(), encodedName.toString(), storeURL.getUsername(), null);
    }
    
    public Store getStore() {
        return this.store;
    }
    
    public abstract Folder getParent() throws MessagingException;
    
    public abstract boolean exists() throws MessagingException;
    
    public abstract Folder[] list(final String p0) throws MessagingException;
    
    public Folder[] listSubscribed(final String pattern) throws MessagingException {
        return this.list(pattern);
    }
    
    public Folder[] list() throws MessagingException {
        return this.list("%");
    }
    
    public Folder[] listSubscribed() throws MessagingException {
        return this.listSubscribed("%");
    }
    
    public abstract char getSeparator() throws MessagingException;
    
    public abstract int getType() throws MessagingException;
    
    public abstract boolean create(final int p0) throws MessagingException;
    
    public boolean isSubscribed() {
        return true;
    }
    
    public void setSubscribed(final boolean subscribe) throws MessagingException {
        throw new MethodNotSupportedException();
    }
    
    public abstract boolean hasNewMessages() throws MessagingException;
    
    public abstract Folder getFolder(final String p0) throws MessagingException;
    
    public abstract boolean delete(final boolean p0) throws MessagingException;
    
    public abstract boolean renameTo(final Folder p0) throws MessagingException;
    
    public abstract void open(final int p0) throws MessagingException;
    
    public abstract void close(final boolean p0) throws MessagingException;
    
    public abstract boolean isOpen();
    
    public int getMode() {
        if (!this.isOpen()) {
            throw new IllegalStateException("Folder not open");
        }
        return this.mode;
    }
    
    public abstract Flags getPermanentFlags();
    
    public abstract int getMessageCount() throws MessagingException;
    
    public synchronized int getNewMessageCount() throws MessagingException {
        if (!this.isOpen()) {
            return -1;
        }
        int newmsgs = 0;
        for (int total = this.getMessageCount(), i = 1; i <= total; ++i) {
            try {
                if (this.getMessage(i).isSet(Flags.Flag.RECENT)) {
                    ++newmsgs;
                }
            }
            catch (MessageRemovedException me) {}
        }
        return newmsgs;
    }
    
    public synchronized int getUnreadMessageCount() throws MessagingException {
        if (!this.isOpen()) {
            return -1;
        }
        int unread = 0;
        for (int total = this.getMessageCount(), i = 1; i <= total; ++i) {
            try {
                if (!this.getMessage(i).isSet(Flags.Flag.SEEN)) {
                    ++unread;
                }
            }
            catch (MessageRemovedException me) {}
        }
        return unread;
    }
    
    public synchronized int getDeletedMessageCount() throws MessagingException {
        if (!this.isOpen()) {
            return -1;
        }
        int deleted = 0;
        for (int total = this.getMessageCount(), i = 1; i <= total; ++i) {
            try {
                if (this.getMessage(i).isSet(Flags.Flag.DELETED)) {
                    ++deleted;
                }
            }
            catch (MessageRemovedException me) {}
        }
        return deleted;
    }
    
    public abstract Message getMessage(final int p0) throws MessagingException;
    
    public synchronized Message[] getMessages(final int start, final int end) throws MessagingException {
        final Message[] msgs = new Message[end - start + 1];
        for (int i = start; i <= end; ++i) {
            msgs[i - start] = this.getMessage(i);
        }
        return msgs;
    }
    
    public synchronized Message[] getMessages(final int[] msgnums) throws MessagingException {
        final int len = msgnums.length;
        final Message[] msgs = new Message[len];
        for (int i = 0; i < len; ++i) {
            msgs[i] = this.getMessage(msgnums[i]);
        }
        return msgs;
    }
    
    public synchronized Message[] getMessages() throws MessagingException {
        if (!this.isOpen()) {
            throw new IllegalStateException("Folder not open");
        }
        final int total = this.getMessageCount();
        final Message[] msgs = new Message[total];
        for (int i = 1; i <= total; ++i) {
            msgs[i - 1] = this.getMessage(i);
        }
        return msgs;
    }
    
    public abstract void appendMessages(final Message[] p0) throws MessagingException;
    
    public void fetch(final Message[] msgs, final FetchProfile fp) throws MessagingException {
    }
    
    public synchronized void setFlags(final Message[] msgs, final Flags flag, final boolean value) throws MessagingException {
        for (int i = 0; i < msgs.length; ++i) {
            try {
                msgs[i].setFlags(flag, value);
            }
            catch (MessageRemovedException ex) {}
        }
    }
    
    public synchronized void setFlags(final int start, final int end, final Flags flag, final boolean value) throws MessagingException {
        for (int i = start; i <= end; ++i) {
            try {
                final Message msg = this.getMessage(i);
                msg.setFlags(flag, value);
            }
            catch (MessageRemovedException ex) {}
        }
    }
    
    public synchronized void setFlags(final int[] msgnums, final Flags flag, final boolean value) throws MessagingException {
        for (int i = 0; i < msgnums.length; ++i) {
            try {
                final Message msg = this.getMessage(msgnums[i]);
                msg.setFlags(flag, value);
            }
            catch (MessageRemovedException ex) {}
        }
    }
    
    public void copyMessages(final Message[] msgs, final Folder folder) throws MessagingException {
        if (!folder.exists()) {
            throw new FolderNotFoundException(folder.getFullName() + " does not exist", folder);
        }
        folder.appendMessages(msgs);
    }
    
    public abstract Message[] expunge() throws MessagingException;
    
    public Message[] search(final SearchTerm term) throws MessagingException {
        return this.search(term, this.getMessages());
    }
    
    public Message[] search(final SearchTerm term, final Message[] msgs) throws MessagingException {
        final Vector matchedMsgs = new Vector();
        for (int i = 0; i < msgs.length; ++i) {
            try {
                if (msgs[i].match(term)) {
                    matchedMsgs.addElement(msgs[i]);
                }
            }
            catch (MessageRemovedException ex) {}
        }
        final Message[] m = new Message[matchedMsgs.size()];
        matchedMsgs.copyInto(m);
        return m;
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
    
    protected void notifyConnectionListeners(final int type) {
        if (this.connectionListeners != null) {
            final ConnectionEvent e = new ConnectionEvent(this, type);
            this.queueEvent(e, this.connectionListeners);
        }
        if (type == 3) {
            this.terminateQueue();
        }
    }
    
    public synchronized void addFolderListener(final FolderListener l) {
        if (this.folderListeners == null) {
            this.folderListeners = new Vector();
        }
        this.folderListeners.addElement(l);
    }
    
    public synchronized void removeFolderListener(final FolderListener l) {
        if (this.folderListeners != null) {
            this.folderListeners.removeElement(l);
        }
    }
    
    protected void notifyFolderListeners(final int type) {
        if (this.folderListeners != null) {
            final FolderEvent e = new FolderEvent(this, this, type);
            this.queueEvent(e, this.folderListeners);
        }
        this.store.notifyFolderListeners(type, this);
    }
    
    protected void notifyFolderRenamedListeners(final Folder folder) {
        if (this.folderListeners != null) {
            final FolderEvent e = new FolderEvent(this, this, folder, 3);
            this.queueEvent(e, this.folderListeners);
        }
        this.store.notifyFolderRenamedListeners(this, folder);
    }
    
    public synchronized void addMessageCountListener(final MessageCountListener l) {
        if (this.messageCountListeners == null) {
            this.messageCountListeners = new Vector();
        }
        this.messageCountListeners.addElement(l);
    }
    
    public synchronized void removeMessageCountListener(final MessageCountListener l) {
        if (this.messageCountListeners != null) {
            this.messageCountListeners.removeElement(l);
        }
    }
    
    protected void notifyMessageAddedListeners(final Message[] msgs) {
        if (this.messageCountListeners == null) {
            return;
        }
        final MessageCountEvent e = new MessageCountEvent(this, 1, false, msgs);
        this.queueEvent(e, this.messageCountListeners);
    }
    
    protected void notifyMessageRemovedListeners(final boolean removed, final Message[] msgs) {
        if (this.messageCountListeners == null) {
            return;
        }
        final MessageCountEvent e = new MessageCountEvent(this, 2, removed, msgs);
        this.queueEvent(e, this.messageCountListeners);
    }
    
    public synchronized void addMessageChangedListener(final MessageChangedListener l) {
        if (this.messageChangedListeners == null) {
            this.messageChangedListeners = new Vector();
        }
        this.messageChangedListeners.addElement(l);
    }
    
    public synchronized void removeMessageChangedListener(final MessageChangedListener l) {
        if (this.messageChangedListeners != null) {
            this.messageChangedListeners.removeElement(l);
        }
    }
    
    protected void notifyMessageChangedListeners(final int type, final Message msg) {
        if (this.messageChangedListeners == null) {
            return;
        }
        final MessageChangedEvent e = new MessageChangedEvent(this, type, msg);
        this.queueEvent(e, this.messageChangedListeners);
    }
    
    private void queueEvent(final MailEvent event, final Vector vector) {
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
    
    public String toString() {
        final String s = this.getFullName();
        if (s != null) {
            return s;
        }
        return super.toString();
    }
    
    static class TerminatorEvent extends MailEvent
    {
        private static final long serialVersionUID = 3765761925441296565L;
        
        TerminatorEvent() {
            super(new Object());
        }
        
        public void dispatch(final Object listener) {
            Thread.currentThread().interrupt();
        }
    }
}
