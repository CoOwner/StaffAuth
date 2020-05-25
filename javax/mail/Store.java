package javax.mail;

import java.util.*;
import javax.mail.event.*;

public abstract class Store extends Service
{
    private volatile Vector storeListeners;
    private volatile Vector folderListeners;
    
    protected Store(final Session session, final URLName urlname) {
        super(session, urlname);
        this.storeListeners = null;
        this.folderListeners = null;
    }
    
    public abstract Folder getDefaultFolder() throws MessagingException;
    
    public abstract Folder getFolder(final String p0) throws MessagingException;
    
    public abstract Folder getFolder(final URLName p0) throws MessagingException;
    
    public Folder[] getPersonalNamespaces() throws MessagingException {
        return new Folder[] { this.getDefaultFolder() };
    }
    
    public Folder[] getUserNamespaces(final String user) throws MessagingException {
        return new Folder[0];
    }
    
    public Folder[] getSharedNamespaces() throws MessagingException {
        return new Folder[0];
    }
    
    public synchronized void addStoreListener(final StoreListener l) {
        if (this.storeListeners == null) {
            this.storeListeners = new Vector();
        }
        this.storeListeners.addElement(l);
    }
    
    public synchronized void removeStoreListener(final StoreListener l) {
        if (this.storeListeners != null) {
            this.storeListeners.removeElement(l);
        }
    }
    
    protected void notifyStoreListeners(final int type, final String message) {
        if (this.storeListeners == null) {
            return;
        }
        final StoreEvent e = new StoreEvent(this, type, message);
        this.queueEvent(e, this.storeListeners);
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
    
    protected void notifyFolderListeners(final int type, final Folder folder) {
        if (this.folderListeners == null) {
            return;
        }
        final FolderEvent e = new FolderEvent(this, folder, type);
        this.queueEvent(e, this.folderListeners);
    }
    
    protected void notifyFolderRenamedListeners(final Folder oldF, final Folder newF) {
        if (this.folderListeners == null) {
            return;
        }
        final FolderEvent e = new FolderEvent(this, oldF, newF, 3);
        this.queueEvent(e, this.folderListeners);
    }
}
