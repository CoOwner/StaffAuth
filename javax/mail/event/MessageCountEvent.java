package javax.mail.event;

import javax.mail.*;

public class MessageCountEvent extends MailEvent
{
    public static final int ADDED = 1;
    public static final int REMOVED = 2;
    protected int type;
    protected boolean removed;
    protected transient Message[] msgs;
    private static final long serialVersionUID = -7447022340837897369L;
    
    public MessageCountEvent(final Folder folder, final int type, final boolean removed, final Message[] msgs) {
        super(folder);
        this.type = type;
        this.removed = removed;
        this.msgs = msgs;
    }
    
    public int getType() {
        return this.type;
    }
    
    public boolean isRemoved() {
        return this.removed;
    }
    
    public Message[] getMessages() {
        return this.msgs;
    }
    
    public void dispatch(final Object listener) {
        if (this.type == 1) {
            ((MessageCountListener)listener).messagesAdded(this);
        }
        else {
            ((MessageCountListener)listener).messagesRemoved(this);
        }
    }
}
