package com.sun.mail.imap;

import java.io.*;
import javax.mail.internet.*;
import javax.mail.search.*;
import java.util.*;
import javax.mail.*;
import com.sun.mail.iap.*;
import com.sun.mail.imap.protocol.*;

public class IMAPFolder extends Folder implements UIDFolder, ResponseHandler
{
    protected String fullName;
    protected String name;
    protected int type;
    protected char separator;
    protected Flags availableFlags;
    protected Flags permanentFlags;
    protected boolean exists;
    protected boolean isNamespace;
    protected String[] attributes;
    protected IMAPProtocol protocol;
    protected Vector messageCache;
    protected Object messageCacheLock;
    protected Hashtable uidTable;
    protected static final char UNKNOWN_SEPARATOR = '\uffff';
    private boolean opened;
    private boolean reallyClosed;
    private int total;
    private int recent;
    private int realTotal;
    private int uidvalidity;
    private int uidnext;
    private boolean doExpungeNotification;
    private Status cachedStatus;
    private long cachedStatusTime;
    private boolean debug;
    private PrintStream out;
    private boolean connectionPoolDebug;
    
    protected IMAPFolder(final String fullName, final char separator, final IMAPStore store) {
        this(fullName, separator, store, false);
    }
    
    protected IMAPFolder(final String fullName, final char separator, final IMAPStore store, final boolean isNamespace) {
        super(store);
        this.exists = false;
        this.isNamespace = false;
        this.opened = false;
        this.reallyClosed = true;
        this.total = -1;
        this.recent = -1;
        this.realTotal = -1;
        this.uidvalidity = -1;
        this.uidnext = -1;
        this.doExpungeNotification = true;
        this.cachedStatus = null;
        this.cachedStatusTime = 0L;
        this.debug = false;
        if (fullName == null) {
            throw new NullPointerException("Folder name is null");
        }
        this.fullName = fullName;
        this.separator = separator;
        this.isNamespace = isNamespace;
        this.messageCacheLock = new Object();
        this.debug = store.getSession().getDebug();
        this.connectionPoolDebug = store.getConnectionPoolDebug();
        this.out = store.getSession().getDebugOut();
        if (this.out == null) {
            this.out = System.out;
        }
    }
    
    protected IMAPFolder(final ListInfo li, final IMAPStore store) {
        this(li.name, li.separator, store);
        if (li.hasInferiors) {
            this.type |= 0x2;
        }
        if (li.canOpen) {
            this.type |= 0x1;
        }
        this.exists = true;
        this.attributes = li.attrs;
    }
    
    private void checkExists() throws MessagingException {
        if (!this.exists && !this.exists()) {
            throw new FolderNotFoundException(this, this.fullName + " not found");
        }
    }
    
    private void checkClosed() {
        if (this.opened) {
            throw new IllegalStateException("This operation is not allowed on an open folder");
        }
    }
    
    private void checkOpened() throws FolderClosedException {
        if (this.opened) {
            return;
        }
        if (this.reallyClosed) {
            throw new IllegalStateException("This operation is not allowed on a closed folder");
        }
        throw new FolderClosedException(this, "Lost folder connection to server");
    }
    
    private void checkRange(final int msgno) throws MessagingException {
        if (msgno < 1) {
            throw new IndexOutOfBoundsException();
        }
        if (msgno <= this.total) {
            return;
        }
        synchronized (this.messageCacheLock) {
            try {
                this.keepConnectionAlive(false);
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (msgno > this.total) {
            throw new IndexOutOfBoundsException();
        }
    }
    
    private void checkFlags(final Flags flags) throws MessagingException {
        if (this.mode != 2) {
            throw new IllegalStateException("Cannot change flags on READ_ONLY folder: " + this.fullName);
        }
        if (!this.availableFlags.contains(flags)) {
            throw new MessagingException("These flags are not supported by this implementation");
        }
    }
    
    public String getName() {
        if (this.name == null) {
            try {
                this.name = this.fullName.substring(this.fullName.lastIndexOf(this.getSeparator()) + 1);
            }
            catch (MessagingException ex) {}
        }
        return this.name;
    }
    
    public String getFullName() {
        return this.fullName;
    }
    
    public Folder getParent() throws MessagingException {
        final char c = this.getSeparator();
        final int index;
        if ((index = this.fullName.lastIndexOf(c)) != -1) {
            return new IMAPFolder(this.fullName.substring(0, index), c, (IMAPStore)this.store);
        }
        return new DefaultFolder((IMAPStore)this.store);
    }
    
    public boolean exists() throws MessagingException {
        ListInfo[] li = null;
        String lname;
        if (this.isNamespace && this.separator != '\0') {
            lname = this.fullName + this.separator;
        }
        else {
            lname = this.fullName;
        }
        li = (ListInfo[])this.doCommand(new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                return p.list("", lname);
            }
        });
        if (li != null) {
            final int i = this.findName(li, lname);
            this.fullName = li[i].name;
            this.separator = li[i].separator;
            final int len = this.fullName.length();
            if (this.separator != '\0' && len > 0 && this.fullName.charAt(len - 1) == this.separator) {
                this.fullName = this.fullName.substring(0, len - 1);
            }
            this.type = 0;
            if (li[i].hasInferiors) {
                this.type |= 0x2;
            }
            if (li[i].canOpen) {
                this.type |= 0x1;
            }
            this.exists = true;
            this.attributes = li[i].attrs;
        }
        else {
            this.exists = false;
        }
        return this.exists;
    }
    
    private int findName(final ListInfo[] li, final String lname) {
        int i;
        for (i = 0; i < li.length && !li[i].name.equals(lname); ++i) {}
        if (i >= li.length) {
            i = 0;
        }
        return i;
    }
    
    public Folder[] list(final String pattern) throws MessagingException {
        return this.doList(pattern, false);
    }
    
    public Folder[] listSubscribed(final String pattern) throws MessagingException {
        return this.doList(pattern, true);
    }
    
    private Folder[] doList(final String pattern, final boolean subscribed) throws MessagingException {
        this.checkExists();
        if (!this.isDirectory()) {
            return new Folder[0];
        }
        final char c = this.getSeparator();
        final ListInfo[] li = (ListInfo[])this.doCommandIgnoreFailure(new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                if (subscribed) {
                    return p.lsub("", IMAPFolder.this.fullName + c + pattern);
                }
                return p.list("", IMAPFolder.this.fullName + c + pattern);
            }
        });
        if (li == null) {
            return new Folder[0];
        }
        int start = 0;
        if (li.length > 0 && li[0].name.equals(this.fullName + c)) {
            start = 1;
        }
        final IMAPFolder[] folders = new IMAPFolder[li.length - start];
        for (int i = start; i < li.length; ++i) {
            folders[i - start] = new IMAPFolder(li[i], (IMAPStore)this.store);
        }
        return folders;
    }
    
    public synchronized char getSeparator() throws MessagingException {
        if (this.separator == '\uffff') {
            ListInfo[] li = null;
            li = (ListInfo[])this.doCommand(new ProtocolCommand() {
                public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                    if (p.isREV1()) {
                        return p.list(IMAPFolder.this.fullName, "");
                    }
                    return p.list("", IMAPFolder.this.fullName);
                }
            });
            if (li != null) {
                this.separator = li[0].separator;
            }
            else {
                this.separator = '/';
            }
        }
        return this.separator;
    }
    
    public int getType() throws MessagingException {
        this.checkExists();
        return this.type;
    }
    
    public boolean isSubscribed() {
        ListInfo[] li = null;
        String lname;
        if (this.isNamespace && this.separator != '\0') {
            lname = this.fullName + this.separator;
        }
        else {
            lname = this.fullName;
        }
        try {
            li = (ListInfo[])this.doProtocolCommand(new ProtocolCommand() {
                public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                    return p.lsub("", lname);
                }
            });
        }
        catch (ProtocolException ex) {}
        if (li != null) {
            final int i = this.findName(li, lname);
            return li[i].canOpen;
        }
        return false;
    }
    
    public void setSubscribed(final boolean subscribe) throws MessagingException {
        this.doCommandIgnoreFailure(new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                if (subscribe) {
                    p.subscribe(IMAPFolder.this.fullName);
                }
                else {
                    p.unsubscribe(IMAPFolder.this.fullName);
                }
                return null;
            }
        });
    }
    
    public synchronized boolean create(final int type) throws MessagingException {
        char c = '\0';
        if ((type & 0x1) == 0x0) {
            c = this.getSeparator();
        }
        final char sep = c;
        final Object ret = this.doCommandIgnoreFailure(new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                if ((type & 0x1) == 0x0) {
                    p.create(IMAPFolder.this.fullName + sep);
                }
                else {
                    p.create(IMAPFolder.this.fullName);
                    if ((type & 0x2) != 0x0) {
                        final ListInfo[] li = p.list("", IMAPFolder.this.fullName);
                        if (li != null && !li[0].hasInferiors) {
                            p.delete(IMAPFolder.this.fullName);
                            throw new ProtocolException("Unsupported type");
                        }
                    }
                }
                return Boolean.TRUE;
            }
        });
        if (ret == null) {
            return false;
        }
        final boolean retb = this.exists();
        this.notifyFolderListeners(1);
        return retb;
    }
    
    public synchronized boolean hasNewMessages() throws MessagingException {
        this.checkExists();
        if (this.opened) {
            synchronized (this.messageCacheLock) {
                try {
                    this.keepConnectionAlive(true);
                }
                catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                }
                catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
            return this.recent > 0;
        }
        final Boolean b = (Boolean)this.doCommandIgnoreFailure(new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final ListInfo[] li = p.list("", IMAPFolder.this.fullName);
                if (li != null) {
                    if (li[0].changeState == 1) {
                        return Boolean.TRUE;
                    }
                    if (li[0].changeState == 2) {
                        return Boolean.FALSE;
                    }
                }
                final Status status = IMAPFolder.this.getStatus();
                if (status.recent > 0) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
        return b != null && b;
    }
    
    public Folder getFolder(final String name) throws MessagingException {
        if (this.exists && !this.isDirectory()) {
            throw new MessagingException("Cannot contain subfolders");
        }
        final char c = this.getSeparator();
        return new IMAPFolder(this.fullName + c + name, c, (IMAPStore)this.store);
    }
    
    public synchronized boolean delete(final boolean recurse) throws MessagingException {
        this.checkClosed();
        if (recurse) {
            final Folder[] f = this.list();
            for (int i = 0; i < f.length; ++i) {
                f[i].delete(recurse);
            }
        }
        final Object ret = this.doCommandIgnoreFailure(new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                p.delete(IMAPFolder.this.fullName);
                return Boolean.TRUE;
            }
        });
        if (ret == null) {
            return false;
        }
        this.exists = false;
        this.notifyFolderListeners(2);
        return true;
    }
    
    public synchronized boolean renameTo(final Folder f) throws MessagingException {
        this.checkClosed();
        this.checkExists();
        if (f.getStore() != this.store) {
            throw new MessagingException("Can't rename across Stores");
        }
        final Object ret = this.doCommandIgnoreFailure(new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                p.rename(IMAPFolder.this.fullName, f.getFullName());
                return Boolean.TRUE;
            }
        });
        if (ret == null) {
            return false;
        }
        this.exists = false;
        this.notifyFolderRenamedListeners(f);
        return true;
    }
    
    public synchronized void open(final int mode) throws MessagingException {
        this.checkClosed();
        MailboxInfo mi = null;
        this.protocol = ((IMAPStore)this.store).getProtocol(this);
        CommandFailedException exc = null;
        Label_0460: {
            synchronized (this.messageCacheLock) {
                this.protocol.addResponseHandler(this);
                try {
                    if (mode == 1) {
                        mi = this.protocol.examine(this.fullName);
                    }
                    else {
                        mi = this.protocol.select(this.fullName);
                    }
                }
                catch (CommandFailedException cex) {
                    this.releaseProtocol(true);
                    this.protocol = null;
                    exc = cex;
                    break Label_0460;
                }
                catch (ProtocolException pex) {
                    try {
                        this.protocol.logout();
                    }
                    catch (ProtocolException pex2) {}
                    finally {
                        this.releaseProtocol(false);
                        this.protocol = null;
                        throw new MessagingException(pex.getMessage(), pex);
                    }
                }
                if (mi.mode != mode) {
                    if (mode != 2 || mi.mode != 1 || !((IMAPStore)this.store).allowReadOnlySelect()) {
                        try {
                            this.protocol.close();
                            this.releaseProtocol(true);
                        }
                        catch (ProtocolException pex) {
                            try {
                                this.protocol.logout();
                            }
                            catch (ProtocolException pex2) {}
                            finally {
                                this.releaseProtocol(false);
                            }
                        }
                        finally {
                            this.protocol = null;
                            throw new ReadOnlyFolderException(this, "Cannot open in desired mode");
                        }
                    }
                }
                this.opened = true;
                this.reallyClosed = false;
                this.mode = mi.mode;
                this.availableFlags = mi.availableFlags;
                this.permanentFlags = mi.permanentFlags;
                final int total = mi.total;
                this.realTotal = total;
                this.total = total;
                this.recent = mi.recent;
                this.uidvalidity = mi.uidvalidity;
                this.uidnext = mi.uidnext;
                this.messageCache = new Vector(this.total);
                for (int i = 0; i < this.total; ++i) {
                    this.messageCache.addElement(new IMAPMessage(this, i + 1, i + 1));
                }
            }
        }
        if (exc == null) {
            this.notifyConnectionListeners(1);
            return;
        }
        this.checkExists();
        if ((this.type & 0x1) == 0x0) {
            throw new MessagingException("folder cannot contain messages");
        }
        throw new MessagingException(exc.getMessage(), exc);
    }
    
    public synchronized void fetch(final Message[] msgs, final FetchProfile fp) throws MessagingException {
        this.checkOpened();
        IMAPMessage.fetch(this, msgs, fp);
    }
    
    public synchronized void setFlags(final Message[] msgs, final Flags flag, final boolean value) throws MessagingException {
        this.checkOpened();
        this.checkFlags(flag);
        if (msgs.length == 0) {
            return;
        }
        synchronized (this.messageCacheLock) {
            try {
                final MessageSet[] ms = Utility.toMessageSet(msgs, null);
                if (ms == null) {
                    throw new MessageRemovedException("Messages have been removed");
                }
                this.protocol.storeFlags(ms, flag, value);
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
    }
    
    public synchronized void close(final boolean expunge) throws MessagingException {
        this.close(expunge, false);
    }
    
    public synchronized void forceClose() throws MessagingException {
        this.close(false, true);
    }
    
    private void close(final boolean expunge, final boolean force) throws MessagingException {
        synchronized (this.messageCacheLock) {
            if (!this.opened && this.reallyClosed) {
                throw new IllegalStateException("This operation is not allowed on a closed folder");
            }
            this.reallyClosed = true;
            if (!this.opened) {
                return;
            }
            try {
                if (force) {
                    if (this.debug) {
                        this.out.println("DEBUG: forcing folder " + this.fullName + " to close");
                    }
                    if (this.protocol != null) {
                        this.protocol.disconnect();
                    }
                }
                else if (((IMAPStore)this.store).isConnectionPoolFull()) {
                    if (this.debug) {
                        this.out.println("DEBUG: pool is full, not adding an Authenticated connection");
                    }
                    if (expunge) {
                        this.protocol.close();
                    }
                    if (this.protocol != null) {
                        this.protocol.logout();
                    }
                }
                else {
                    if (!expunge && this.mode == 2) {
                        try {
                            final MailboxInfo mi = this.protocol.examine(this.fullName);
                        }
                        catch (ProtocolException pex2) {
                            if (this.protocol != null) {
                                this.protocol.disconnect();
                            }
                        }
                    }
                    if (this.protocol != null) {
                        this.protocol.close();
                    }
                }
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
            finally {
                if (this.opened) {
                    this.cleanup(true);
                }
            }
        }
    }
    
    private void cleanup(final boolean returnToPool) {
        this.releaseProtocol(returnToPool);
        this.protocol = null;
        this.messageCache = null;
        this.uidTable = null;
        this.exists = false;
        this.opened = false;
        this.notifyConnectionListeners(3);
    }
    
    public synchronized boolean isOpen() {
        synchronized (this.messageCacheLock) {
            if (this.opened) {
                try {
                    this.keepConnectionAlive(false);
                }
                catch (ProtocolException ex) {}
            }
        }
        return this.opened;
    }
    
    public Flags getPermanentFlags() {
        return this.permanentFlags;
    }
    
    public synchronized int getMessageCount() throws MessagingException {
        this.checkExists();
        if (!this.opened) {
            try {
                final Status status = this.getStatus();
                return status.total;
            }
            catch (BadCommandException bex) {
                IMAPProtocol p = null;
                try {
                    p = this.getStoreProtocol();
                    final MailboxInfo minfo = p.examine(this.fullName);
                    p.close();
                    return minfo.total;
                }
                catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
                finally {
                    this.releaseStoreProtocol(p);
                }
            }
            catch (ConnectionException cex) {
                throw new StoreClosedException(this.store, cex.getMessage());
            }
            catch (ProtocolException pex2) {
                throw new MessagingException(pex2.getMessage(), pex2);
            }
        }
        synchronized (this.messageCacheLock) {
            try {
                this.keepConnectionAlive(true);
                return this.total;
            }
            catch (ConnectionException cex2) {
                throw new FolderClosedException(this, cex2.getMessage());
            }
            catch (ProtocolException pex3) {
                throw new MessagingException(pex3.getMessage(), pex3);
            }
        }
    }
    
    public synchronized int getNewMessageCount() throws MessagingException {
        this.checkExists();
        if (!this.opened) {
            try {
                final Status status = this.getStatus();
                return status.recent;
            }
            catch (BadCommandException bex) {
                IMAPProtocol p = null;
                try {
                    p = this.getStoreProtocol();
                    final MailboxInfo minfo = p.examine(this.fullName);
                    p.close();
                    return minfo.recent;
                }
                catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
                finally {
                    this.releaseStoreProtocol(p);
                }
            }
            catch (ConnectionException cex) {
                throw new StoreClosedException(this.store, cex.getMessage());
            }
            catch (ProtocolException pex2) {
                throw new MessagingException(pex2.getMessage(), pex2);
            }
        }
        synchronized (this.messageCacheLock) {
            try {
                this.keepConnectionAlive(true);
                return this.recent;
            }
            catch (ConnectionException cex2) {
                throw new FolderClosedException(this, cex2.getMessage());
            }
            catch (ProtocolException pex3) {
                throw new MessagingException(pex3.getMessage(), pex3);
            }
        }
    }
    
    public synchronized int getUnreadMessageCount() throws MessagingException {
        this.checkExists();
        if (!this.opened) {
            try {
                final Status status = this.getStatus();
                return status.unseen;
            }
            catch (BadCommandException bex) {
                return -1;
            }
            catch (ConnectionException cex) {
                throw new StoreClosedException(this.store, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        final Flags f = new Flags();
        f.add(Flags.Flag.SEEN);
        try {
            synchronized (this.messageCacheLock) {
                final int[] matches = this.protocol.search(new FlagTerm(f, false));
                return matches.length;
            }
        }
        catch (ConnectionException cex2) {
            throw new FolderClosedException(this, cex2.getMessage());
        }
        catch (ProtocolException pex2) {
            throw new MessagingException(pex2.getMessage(), pex2);
        }
    }
    
    public synchronized int getDeletedMessageCount() throws MessagingException {
        this.checkExists();
        if (!this.opened) {
            return -1;
        }
        final Flags f = new Flags();
        f.add(Flags.Flag.DELETED);
        try {
            synchronized (this.messageCacheLock) {
                final int[] matches = this.protocol.search(new FlagTerm(f, true));
                return matches.length;
            }
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }
    
    private Status getStatus() throws ProtocolException {
        final int statusCacheTimeout = ((IMAPStore)this.store).getStatusCacheTimeout();
        if (statusCacheTimeout > 0 && this.cachedStatus != null && System.currentTimeMillis() - this.cachedStatusTime < statusCacheTimeout) {
            return this.cachedStatus;
        }
        IMAPProtocol p = null;
        try {
            p = this.getStoreProtocol();
            final Status s = p.status(this.fullName, null);
            if (statusCacheTimeout > 0) {
                this.cachedStatus = s;
                this.cachedStatusTime = System.currentTimeMillis();
            }
            return s;
        }
        finally {
            this.releaseStoreProtocol(p);
        }
    }
    
    public synchronized Message getMessage(final int msgnum) throws MessagingException {
        this.checkOpened();
        this.checkRange(msgnum);
        return this.messageCache.elementAt(msgnum - 1);
    }
    
    public void appendMessages(final Message[] msgs) throws MessagingException {
        this.checkExists();
        final int maxsize = ((IMAPStore)this.store).getAppendBufferSize();
        for (int i = 0; i < msgs.length; ++i) {
            final Message m = msgs[i];
            MessageLiteral mos;
            try {
                mos = new MessageLiteral(m, (m.getSize() > maxsize) ? 0 : maxsize);
            }
            catch (IOException ex) {
                throw new MessagingException("IOException while appending messages", ex);
            }
            catch (MessageRemovedException mrex) {
                continue;
            }
            Date d = m.getReceivedDate();
            if (d == null) {
                d = m.getSentDate();
            }
            final Date dd = d;
            final Flags f = m.getFlags();
            this.doCommand(new ProtocolCommand() {
                public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                    p.append(IMAPFolder.this.fullName, f, dd, mos);
                    return null;
                }
            });
        }
    }
    
    public AppendUID[] appendUIDMessages(final Message[] msgs) throws MessagingException {
        this.checkExists();
        final int maxsize = ((IMAPStore)this.store).getAppendBufferSize();
        final AppendUID[] uids = new AppendUID[msgs.length];
        for (int i = 0; i < msgs.length; ++i) {
            final Message m = msgs[i];
            MessageLiteral mos;
            try {
                mos = new MessageLiteral(m, (m.getSize() > maxsize) ? 0 : maxsize);
            }
            catch (IOException ex) {
                throw new MessagingException("IOException while appending messages", ex);
            }
            catch (MessageRemovedException mrex) {
                continue;
            }
            Date d = m.getReceivedDate();
            if (d == null) {
                d = m.getSentDate();
            }
            final Date dd = d;
            final Flags f = m.getFlags();
            final AppendUID auid = (AppendUID)this.doCommand(new ProtocolCommand() {
                public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                    return p.appenduid(IMAPFolder.this.fullName, f, dd, mos);
                }
            });
            uids[i] = auid;
        }
        return uids;
    }
    
    public Message[] addMessages(final Message[] msgs) throws MessagingException {
        this.checkOpened();
        final Message[] rmsgs = new MimeMessage[msgs.length];
        final AppendUID[] uids = this.appendUIDMessages(msgs);
        for (int i = 0; i < uids.length; ++i) {
            final AppendUID auid = uids[i];
            if (auid != null && auid.uidvalidity == this.uidvalidity) {
                try {
                    rmsgs[i] = this.getMessageByUID(auid.uid);
                }
                catch (MessagingException ex) {}
            }
        }
        return rmsgs;
    }
    
    public synchronized void copyMessages(final Message[] msgs, final Folder folder) throws MessagingException {
        this.checkOpened();
        if (msgs.length == 0) {
            return;
        }
        if (folder.getStore() == this.store) {
            synchronized (this.messageCacheLock) {
                try {
                    final MessageSet[] ms = Utility.toMessageSet(msgs, null);
                    if (ms == null) {
                        throw new MessageRemovedException("Messages have been removed");
                    }
                    this.protocol.copy(ms, folder.getFullName());
                }
                catch (CommandFailedException cfx) {
                    if (cfx.getMessage().indexOf("TRYCREATE") != -1) {
                        throw new FolderNotFoundException(folder, folder.getFullName() + " does not exist");
                    }
                    throw new MessagingException(cfx.getMessage(), cfx);
                }
                catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                }
                catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
        }
        else {
            super.copyMessages(msgs, folder);
        }
    }
    
    public synchronized Message[] expunge() throws MessagingException {
        return this.expunge(null);
    }
    
    public synchronized Message[] expunge(final Message[] msgs) throws MessagingException {
        this.checkOpened();
        final Vector v = new Vector();
        if (msgs != null) {
            final FetchProfile fp = new FetchProfile();
            fp.add(UIDFolder.FetchProfileItem.UID);
            this.fetch(msgs, fp);
        }
        synchronized (this.messageCacheLock) {
            this.doExpungeNotification = false;
            try {
                if (msgs != null) {
                    this.protocol.uidexpunge(Utility.toUIDSet(msgs));
                }
                else {
                    this.protocol.expunge();
                }
            }
            catch (CommandFailedException cfx) {
                if (this.mode != 2) {
                    throw new IllegalStateException("Cannot expunge READ_ONLY folder: " + this.fullName);
                }
                throw new MessagingException(cfx.getMessage(), cfx);
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
            finally {
                this.doExpungeNotification = true;
            }
            int i = 0;
            while (i < this.messageCache.size()) {
                final IMAPMessage m = this.messageCache.elementAt(i);
                if (m.isExpunged()) {
                    v.addElement(m);
                    this.messageCache.removeElementAt(i);
                    if (this.uidTable == null) {
                        continue;
                    }
                    final long uid = m.getUID();
                    if (uid == -1L) {
                        continue;
                    }
                    this.uidTable.remove(new Long(uid));
                }
                else {
                    m.setMessageNumber(m.getSequenceNumber());
                    ++i;
                }
            }
        }
        this.total = this.messageCache.size();
        final Message[] rmsgs = new Message[v.size()];
        v.copyInto(rmsgs);
        if (rmsgs.length > 0) {
            this.notifyMessageRemovedListeners(true, rmsgs);
        }
        return rmsgs;
    }
    
    public synchronized Message[] search(final SearchTerm term) throws MessagingException {
        this.checkOpened();
        try {
            Message[] matchMsgs = null;
            synchronized (this.messageCacheLock) {
                final int[] matches = this.protocol.search(term);
                if (matches != null) {
                    matchMsgs = new IMAPMessage[matches.length];
                    for (int i = 0; i < matches.length; ++i) {
                        matchMsgs[i] = this.getMessageBySeqNumber(matches[i]);
                    }
                }
            }
            return matchMsgs;
        }
        catch (CommandFailedException cfx) {
            return super.search(term);
        }
        catch (SearchException sex) {
            return super.search(term);
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }
    
    public synchronized Message[] search(final SearchTerm term, final Message[] msgs) throws MessagingException {
        this.checkOpened();
        if (msgs.length == 0) {
            return msgs;
        }
        try {
            Message[] matchMsgs = null;
            synchronized (this.messageCacheLock) {
                final MessageSet[] ms = Utility.toMessageSet(msgs, null);
                if (ms == null) {
                    throw new MessageRemovedException("Messages have been removed");
                }
                final int[] matches = this.protocol.search(ms, term);
                if (matches != null) {
                    matchMsgs = new IMAPMessage[matches.length];
                    for (int i = 0; i < matches.length; ++i) {
                        matchMsgs[i] = this.getMessageBySeqNumber(matches[i]);
                    }
                }
            }
            return matchMsgs;
        }
        catch (CommandFailedException cfx) {
            return super.search(term, msgs);
        }
        catch (SearchException sex) {
            return super.search(term, msgs);
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
    }
    
    public synchronized long getUIDValidity() throws MessagingException {
        if (this.opened) {
            return this.uidvalidity;
        }
        IMAPProtocol p = null;
        Status status = null;
        try {
            p = this.getStoreProtocol();
            final String[] item = { "UIDVALIDITY" };
            status = p.status(this.fullName, item);
        }
        catch (BadCommandException bex) {
            throw new MessagingException("Cannot obtain UIDValidity", bex);
        }
        catch (ConnectionException cex) {
            this.throwClosedException(cex);
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        finally {
            this.releaseStoreProtocol(p);
        }
        return status.uidvalidity;
    }
    
    public synchronized long getUIDNext() throws MessagingException {
        if (this.opened) {
            return this.uidnext;
        }
        IMAPProtocol p = null;
        Status status = null;
        try {
            p = this.getStoreProtocol();
            final String[] item = { "UIDNEXT" };
            status = p.status(this.fullName, item);
        }
        catch (BadCommandException bex) {
            throw new MessagingException("Cannot obtain UIDNext", bex);
        }
        catch (ConnectionException cex) {
            this.throwClosedException(cex);
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        finally {
            this.releaseStoreProtocol(p);
        }
        return status.uidnext;
    }
    
    public synchronized Message getMessageByUID(final long uid) throws MessagingException {
        this.checkOpened();
        final Long l = new Long(uid);
        IMAPMessage m = null;
        if (this.uidTable != null) {
            m = this.uidTable.get(l);
            if (m != null) {
                return m;
            }
        }
        else {
            this.uidTable = new Hashtable();
        }
        try {
            synchronized (this.messageCacheLock) {
                final UID u = this.protocol.fetchSequenceNumber(uid);
                if (u != null && u.msgno <= this.total) {
                    m = this.messageCache.elementAt(u.msgno - 1);
                    m.setUID(u.uid);
                    this.uidTable.put(l, m);
                }
            }
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return m;
    }
    
    public synchronized Message[] getMessagesByUID(final long start, final long end) throws MessagingException {
        this.checkOpened();
        if (this.uidTable == null) {
            this.uidTable = new Hashtable();
        }
        Message[] msgs;
        try {
            synchronized (this.messageCacheLock) {
                final UID[] ua = this.protocol.fetchSequenceNumbers(start, end);
                msgs = new Message[ua.length];
                for (int i = 0; i < ua.length; ++i) {
                    final IMAPMessage m = this.messageCache.elementAt(ua[i].msgno - 1);
                    m.setUID(ua[i].uid);
                    msgs[i] = m;
                    this.uidTable.put(new Long(ua[i].uid), m);
                }
            }
        }
        catch (ConnectionException cex) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return msgs;
    }
    
    public synchronized Message[] getMessagesByUID(final long[] uids) throws MessagingException {
        this.checkOpened();
        long[] unavailUids = uids;
        if (this.uidTable != null) {
            final Vector v = new Vector();
            for (int i = 0; i < uids.length; ++i) {
                final Long l;
                if (!this.uidTable.containsKey(l = new Long(uids[i]))) {
                    v.addElement(l);
                }
            }
            final int vsize = v.size();
            unavailUids = new long[vsize];
            for (int j = 0; j < vsize; ++j) {
                unavailUids[j] = v.elementAt(j);
            }
        }
        else {
            this.uidTable = new Hashtable();
        }
        if (unavailUids.length > 0) {
            try {
                synchronized (this.messageCacheLock) {
                    final UID[] ua = this.protocol.fetchSequenceNumbers(unavailUids);
                    for (int j = 0; j < ua.length; ++j) {
                        final IMAPMessage m = this.messageCache.elementAt(ua[j].msgno - 1);
                        m.setUID(ua[j].uid);
                        this.uidTable.put(new Long(ua[j].uid), m);
                    }
                }
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        final Message[] msgs = new Message[uids.length];
        for (int k = 0; k < uids.length; ++k) {
            msgs[k] = this.uidTable.get(new Long(uids[k]));
        }
        return msgs;
    }
    
    public synchronized long getUID(final Message message) throws MessagingException {
        if (message.getFolder() != this) {
            throw new NoSuchElementException("Message does not belong to this folder");
        }
        this.checkOpened();
        final IMAPMessage m = (IMAPMessage)message;
        long uid;
        if ((uid = m.getUID()) != -1L) {
            return uid;
        }
        UID u = null;
        synchronized (this.messageCacheLock) {
            m.checkExpunged();
            try {
                u = this.protocol.fetchUID(m.getSequenceNumber());
            }
            catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            }
            catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (u != null) {
            uid = u.uid;
            m.setUID(uid);
            if (this.uidTable == null) {
                this.uidTable = new Hashtable();
            }
            this.uidTable.put(new Long(uid), m);
        }
        return uid;
    }
    
    public Quota[] getQuota() throws MessagingException {
        return (Quota[])this.doOptionalCommand("QUOTA not supported", new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                return p.getQuotaRoot(IMAPFolder.this.fullName);
            }
        });
    }
    
    public void setQuota(final Quota quota) throws MessagingException {
        this.doOptionalCommand("QUOTA not supported", new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                p.setQuota(quota);
                return null;
            }
        });
    }
    
    public ACL[] getACL() throws MessagingException {
        return (ACL[])this.doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                return p.getACL(IMAPFolder.this.fullName);
            }
        });
    }
    
    public void addACL(final ACL acl) throws MessagingException {
        this.setACL(acl, '\0');
    }
    
    public void removeACL(final String name) throws MessagingException {
        this.doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                p.deleteACL(IMAPFolder.this.fullName, name);
                return null;
            }
        });
    }
    
    public void addRights(final ACL acl) throws MessagingException {
        this.setACL(acl, '+');
    }
    
    public void removeRights(final ACL acl) throws MessagingException {
        this.setACL(acl, '-');
    }
    
    public Rights[] listRights(final String name) throws MessagingException {
        return (Rights[])this.doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                return p.listRights(IMAPFolder.this.fullName, name);
            }
        });
    }
    
    public Rights myRights() throws MessagingException {
        return (Rights)this.doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                return p.myRights(IMAPFolder.this.fullName);
            }
        });
    }
    
    private void setACL(final ACL acl, final char mod) throws MessagingException {
        this.doOptionalCommand("ACL not supported", new ProtocolCommand() {
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                p.setACL(IMAPFolder.this.fullName, mod, acl);
                return null;
            }
        });
    }
    
    public String[] getAttributes() throws MessagingException {
        if (this.attributes == null) {
            this.exists();
        }
        return this.attributes.clone();
    }
    
    public void handleResponse(final Response r) {
        if (r.isOK() || r.isNO() || r.isBAD() || r.isBYE()) {
            ((IMAPStore)this.store).handleResponseCode(r);
        }
        if (r.isBYE()) {
            if (this.opened) {
                this.cleanup(false);
            }
            return;
        }
        if (r.isOK()) {
            return;
        }
        if (!r.isUnTagged()) {
            return;
        }
        if (!(r instanceof IMAPResponse)) {
            this.out.println("UNEXPECTED RESPONSE : " + r.toString());
            this.out.println("CONTACT javamail@sun.com");
            return;
        }
        final IMAPResponse ir = (IMAPResponse)r;
        if (ir.keyEquals("EXISTS")) {
            final int exists = ir.getNumber();
            if (exists <= this.realTotal) {
                return;
            }
            final int count = exists - this.realTotal;
            final Message[] msgs = new Message[count];
            for (int i = 0; i < count; ++i) {
                final IMAPMessage msg = new IMAPMessage(this, ++this.total, ++this.realTotal);
                msgs[i] = msg;
                this.messageCache.addElement(msg);
            }
            this.notifyMessageAddedListeners(msgs);
        }
        else if (ir.keyEquals("EXPUNGE")) {
            final IMAPMessage msg2 = this.getMessageBySeqNumber(ir.getNumber());
            msg2.setExpunged(true);
            for (int j = msg2.getMessageNumber(); j < this.total; ++j) {
                final IMAPMessage m = this.messageCache.elementAt(j);
                if (!m.isExpunged()) {
                    m.setSequenceNumber(m.getSequenceNumber() - 1);
                }
            }
            --this.realTotal;
            if (this.doExpungeNotification) {
                final Message[] msgs2 = { msg2 };
                this.notifyMessageRemovedListeners(false, msgs2);
            }
        }
        else if (ir.keyEquals("FETCH")) {
            final FetchResponse f = (FetchResponse)ir;
            final Flags flags = (Flags)f.getItem(Flags.class);
            if (flags != null) {
                final IMAPMessage msg3 = this.getMessageBySeqNumber(f.getNumber());
                if (msg3 != null) {
                    msg3._setFlags(flags);
                    this.notifyMessageChangedListeners(1, msg3);
                }
            }
        }
        else if (ir.keyEquals("RECENT")) {
            this.recent = ir.getNumber();
        }
    }
    
    void handleResponses(final Response[] r) {
        for (int i = 0; i < r.length; ++i) {
            if (r[i] != null) {
                this.handleResponse(r[i]);
            }
        }
    }
    
    protected synchronized IMAPProtocol getStoreProtocol() throws ProtocolException {
        if (this.connectionPoolDebug) {
            this.out.println("DEBUG: getStoreProtocol() - borrowing a connection");
        }
        return ((IMAPStore)this.store).getStoreProtocol();
    }
    
    private synchronized void throwClosedException(final ConnectionException cex) throws FolderClosedException, StoreClosedException {
        if ((this.protocol != null && cex.getProtocol() == this.protocol) || (this.protocol == null && !this.reallyClosed)) {
            throw new FolderClosedException(this, cex.getMessage());
        }
        throw new StoreClosedException(this.store, cex.getMessage());
    }
    
    public IMAPProtocol getProtocol() {
        return this.protocol;
    }
    
    public Object doCommand(final ProtocolCommand cmd) throws MessagingException {
        try {
            return this.doProtocolCommand(cmd);
        }
        catch (ConnectionException cex) {
            this.throwClosedException(cex);
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return null;
    }
    
    public Object doOptionalCommand(final String err, final ProtocolCommand cmd) throws MessagingException {
        try {
            return this.doProtocolCommand(cmd);
        }
        catch (BadCommandException bex) {
            throw new MessagingException(err, bex);
        }
        catch (ConnectionException cex) {
            this.throwClosedException(cex);
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return null;
    }
    
    public Object doCommandIgnoreFailure(final ProtocolCommand cmd) throws MessagingException {
        try {
            return this.doProtocolCommand(cmd);
        }
        catch (CommandFailedException cfx) {
            return null;
        }
        catch (ConnectionException cex) {
            this.throwClosedException(cex);
        }
        catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        }
        return null;
    }
    
    protected Object doProtocolCommand(final ProtocolCommand cmd) throws ProtocolException {
        synchronized (this) {
            if (this.opened && !((IMAPStore)this.store).hasSeparateStoreConnection()) {
                synchronized (this.messageCacheLock) {
                    return cmd.doCommand(this.getProtocol());
                }
            }
        }
        IMAPProtocol p = null;
        try {
            p = this.getStoreProtocol();
            return cmd.doCommand(p);
        }
        finally {
            this.releaseStoreProtocol(p);
        }
    }
    
    protected synchronized void releaseStoreProtocol(final IMAPProtocol p) {
        if (p != this.protocol) {
            ((IMAPStore)this.store).releaseStoreProtocol(p);
        }
    }
    
    private void releaseProtocol(final boolean returnToPool) {
        if (this.protocol != null) {
            this.protocol.removeResponseHandler(this);
            if (returnToPool) {
                ((IMAPStore)this.store).releaseProtocol(this, this.protocol);
            }
            else {
                ((IMAPStore)this.store).releaseProtocol(this, null);
            }
        }
    }
    
    private void keepConnectionAlive(final boolean keepStoreAlive) throws ProtocolException {
        if (System.currentTimeMillis() - this.protocol.getTimestamp() > 1000L) {
            this.protocol.noop();
        }
        if (keepStoreAlive && ((IMAPStore)this.store).hasSeparateStoreConnection()) {
            IMAPProtocol p = null;
            try {
                p = ((IMAPStore)this.store).getStoreProtocol();
                if (System.currentTimeMillis() - p.getTimestamp() > 1000L) {
                    p.noop();
                }
            }
            finally {
                ((IMAPStore)this.store).releaseStoreProtocol(p);
            }
        }
    }
    
    IMAPMessage getMessageBySeqNumber(final int seqnum) {
        for (int i = seqnum - 1; i < this.total; ++i) {
            final IMAPMessage msg = this.messageCache.elementAt(i);
            if (msg.getSequenceNumber() == seqnum) {
                return msg;
            }
        }
        return null;
    }
    
    private boolean isDirectory() {
        return (this.type & 0x2) != 0x0;
    }
    
    public static class FetchProfileItem extends FetchProfile.Item
    {
        public static final FetchProfileItem HEADERS;
        public static final FetchProfileItem SIZE;
        
        protected FetchProfileItem(final String name) {
            super(name);
        }
        
        static {
            HEADERS = new FetchProfileItem("HEADERS");
            SIZE = new FetchProfileItem("SIZE");
        }
    }
    
    public interface ProtocolCommand
    {
        Object doCommand(final IMAPProtocol p0) throws ProtocolException;
    }
}
