package javax.mail.event;

import javax.mail.*;

public class StoreEvent extends MailEvent
{
    public static final int ALERT = 1;
    public static final int NOTICE = 2;
    protected int type;
    protected String message;
    private static final long serialVersionUID = 1938704919992515330L;
    
    public StoreEvent(final Store store, final int type, final String message) {
        super(store);
        this.type = type;
        this.message = message;
    }
    
    public int getMessageType() {
        return this.type;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public void dispatch(final Object listener) {
        ((StoreListener)listener).notification(this);
    }
}
