package javax.mail.event;

import javax.mail.*;

public class MessageChangedEvent extends MailEvent
{
    public static final int FLAGS_CHANGED = 1;
    public static final int ENVELOPE_CHANGED = 2;
    protected int type;
    protected transient Message msg;
    private static final long serialVersionUID = -4974972972105535108L;
    
    public MessageChangedEvent(final Object source, final int type, final Message msg) {
        super(source);
        this.msg = msg;
        this.type = type;
    }
    
    public int getMessageChangeType() {
        return this.type;
    }
    
    public Message getMessage() {
        return this.msg;
    }
    
    public void dispatch(final Object listener) {
        ((MessageChangedListener)listener).messageChanged(this);
    }
}
