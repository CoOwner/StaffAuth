package com.sun.mail.pop3;

import java.lang.reflect.*;
import javax.mail.*;
import com.sun.mail.util.*;
import java.util.*;
import java.io.*;

public class POP3Folder extends Folder
{
    private String name;
    private Protocol port;
    private int total;
    private int size;
    private boolean exists;
    private boolean opened;
    private Vector message_cache;
    private boolean doneUidl;
    
    POP3Folder(final POP3Store store, final String name) {
        super(store);
        this.exists = false;
        this.opened = false;
        this.doneUidl = false;
        this.name = name;
        if (name.equalsIgnoreCase("INBOX")) {
            this.exists = true;
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getFullName() {
        return this.name;
    }
    
    public Folder getParent() {
        return new DefaultFolder((POP3Store)this.store);
    }
    
    public boolean exists() {
        return this.exists;
    }
    
    public Folder[] list(final String pattern) throws MessagingException {
        throw new MessagingException("not a directory");
    }
    
    public char getSeparator() {
        return '\0';
    }
    
    public int getType() {
        return 1;
    }
    
    public boolean create(final int type) throws MessagingException {
        return false;
    }
    
    public boolean hasNewMessages() throws MessagingException {
        return false;
    }
    
    public Folder getFolder(final String name) throws MessagingException {
        throw new MessagingException("not a directory");
    }
    
    public boolean delete(final boolean recurse) throws MessagingException {
        throw new MethodNotSupportedException("delete");
    }
    
    public boolean renameTo(final Folder f) throws MessagingException {
        throw new MethodNotSupportedException("renameTo");
    }
    
    public synchronized void open(final int mode) throws MessagingException {
        this.checkClosed();
        if (!this.exists) {
            throw new FolderNotFoundException(this, "folder is not INBOX");
        }
        try {
            this.port = ((POP3Store)this.store).getPort(this);
            final Status s = this.port.stat();
            this.total = s.total;
            this.size = s.size;
            this.mode = mode;
            this.opened = true;
        }
        catch (IOException ioex) {
            try {
                if (this.port != null) {
                    this.port.quit();
                }
            }
            catch (IOException ioex2) {}
            finally {
                this.port = null;
                ((POP3Store)this.store).closePort(this);
            }
            throw new MessagingException("Open failed", ioex);
        }
        (this.message_cache = new Vector(this.total)).setSize(this.total);
        this.doneUidl = false;
        this.notifyConnectionListeners(1);
    }
    
    public synchronized void close(final boolean expunge) throws MessagingException {
        this.checkOpen();
        try {
            if (((POP3Store)this.store).rsetBeforeQuit) {
                this.port.rset();
            }
            if (expunge && this.mode == 2) {
                for (int i = 0; i < this.message_cache.size(); ++i) {
                    final POP3Message m;
                    if ((m = this.message_cache.elementAt(i)) != null && m.isSet(Flags.Flag.DELETED)) {
                        try {
                            this.port.dele(i + 1);
                        }
                        catch (IOException ioex) {
                            throw new MessagingException("Exception deleting messages during close", ioex);
                        }
                    }
                }
            }
            this.port.quit();
        }
        catch (IOException ex) {}
        finally {
            this.port = null;
            ((POP3Store)this.store).closePort(this);
            this.message_cache = null;
            this.opened = false;
            this.notifyConnectionListeners(3);
        }
    }
    
    public boolean isOpen() {
        if (!this.opened) {
            return false;
        }
        if (this.store.isConnected()) {
            return true;
        }
        try {
            this.close(false);
        }
        catch (MessagingException ex) {}
        return false;
    }
    
    public Flags getPermanentFlags() {
        return new Flags();
    }
    
    public int getMessageCount() throws MessagingException {
        if (!this.opened) {
            return -1;
        }
        this.checkReadable();
        return this.total;
    }
    
    public synchronized Message getMessage(final int msgno) throws MessagingException {
        this.checkOpen();
        POP3Message m;
        if ((m = this.message_cache.elementAt(msgno - 1)) == null) {
            m = this.createMessage(this, msgno);
            this.message_cache.setElementAt(m, msgno - 1);
        }
        return m;
    }
    
    protected POP3Message createMessage(final Folder f, final int msgno) throws MessagingException {
        POP3Message m = null;
        final Constructor cons = ((POP3Store)this.store).messageConstructor;
        if (cons != null) {
            try {
                final Object[] o = { this, new Integer(msgno) };
                m = cons.newInstance(o);
            }
            catch (Exception ex) {}
        }
        if (m == null) {
            m = new POP3Message(this, msgno);
        }
        return m;
    }
    
    public void appendMessages(final Message[] msgs) throws MessagingException {
        throw new MethodNotSupportedException("Append not supported");
    }
    
    public Message[] expunge() throws MessagingException {
        throw new MethodNotSupportedException("Expunge not supported");
    }
    
    public synchronized void fetch(final Message[] msgs, final FetchProfile fp) throws MessagingException {
        this.checkReadable();
        if (!this.doneUidl && fp.contains(UIDFolder.FetchProfileItem.UID)) {
            final String[] uids = new String[this.message_cache.size()];
            try {
                if (!this.port.uidl(uids)) {
                    return;
                }
            }
            catch (EOFException eex) {
                this.close(false);
                throw new FolderClosedException(this, eex.toString());
            }
            catch (IOException ex) {
                throw new MessagingException("error getting UIDL", ex);
            }
            for (int i = 0; i < uids.length; ++i) {
                if (uids[i] != null) {
                    final POP3Message m = (POP3Message)this.getMessage(i + 1);
                    m.uid = uids[i];
                }
            }
            this.doneUidl = true;
        }
        if (fp.contains(FetchProfile.Item.ENVELOPE)) {
            for (int j = 0; j < msgs.length; ++j) {
                try {
                    final POP3Message msg = (POP3Message)msgs[j];
                    msg.getHeader("");
                    msg.getSize();
                }
                catch (MessageRemovedException ex2) {}
            }
        }
    }
    
    public synchronized String getUID(final Message msg) throws MessagingException {
        this.checkOpen();
        final POP3Message m = (POP3Message)msg;
        try {
            if (m.uid == "UNKNOWN") {
                m.uid = this.port.uidl(m.getMessageNumber());
            }
            return m.uid;
        }
        catch (EOFException eex) {
            this.close(false);
            throw new FolderClosedException(this, eex.toString());
        }
        catch (IOException ex) {
            throw new MessagingException("error getting UIDL", ex);
        }
    }
    
    public int getSize() throws MessagingException {
        this.checkOpen();
        return this.size;
    }
    
    public synchronized int[] getSizes() throws MessagingException {
        this.checkOpen();
        final int[] sizes = new int[this.total];
        InputStream is = null;
        LineInputStream lis = null;
        try {
            is = this.port.list();
            lis = new LineInputStream(is);
            String line;
            while ((line = lis.readLine()) != null) {
                try {
                    final StringTokenizer st = new StringTokenizer(line);
                    final int msgnum = Integer.parseInt(st.nextToken());
                    final int size = Integer.parseInt(st.nextToken());
                    if (msgnum <= 0 || msgnum > this.total) {
                        continue;
                    }
                    sizes[msgnum - 1] = size;
                }
                catch (Exception e) {}
            }
        }
        catch (IOException ex) {}
        finally {
            try {
                if (lis != null) {
                    lis.close();
                }
            }
            catch (IOException ex2) {}
            try {
                if (is != null) {
                    is.close();
                }
            }
            catch (IOException ex3) {}
        }
        return sizes;
    }
    
    public synchronized InputStream listCommand() throws MessagingException, IOException {
        this.checkOpen();
        return this.port.list();
    }
    
    protected void finalize() throws Throwable {
        super.finalize();
        this.close(false);
    }
    
    void checkOpen() throws IllegalStateException {
        if (!this.opened) {
            throw new IllegalStateException("Folder is not Open");
        }
    }
    
    void checkClosed() throws IllegalStateException {
        if (this.opened) {
            throw new IllegalStateException("Folder is Open");
        }
    }
    
    void checkReadable() throws IllegalStateException {
        if (!this.opened || (this.mode != 1 && this.mode != 2)) {
            throw new IllegalStateException("Folder is not Readable");
        }
    }
    
    void checkWritable() throws IllegalStateException {
        if (!this.opened || this.mode != 2) {
            throw new IllegalStateException("Folder is not Writable");
        }
    }
    
    Protocol getProtocol() throws MessagingException {
        this.checkOpen();
        return this.port;
    }
    
    protected void notifyMessageChangedListeners(final int type, final Message m) {
        super.notifyMessageChangedListeners(type, m);
    }
}
