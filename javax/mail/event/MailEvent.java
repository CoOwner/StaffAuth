package javax.mail.event;

import java.util.*;

public abstract class MailEvent extends EventObject
{
    private static final long serialVersionUID = 1846275636325456631L;
    
    public MailEvent(final Object source) {
        super(source);
    }
    
    public abstract void dispatch(final Object p0);
}
