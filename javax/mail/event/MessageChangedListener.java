package javax.mail.event;

import java.util.*;

public interface MessageChangedListener extends EventListener
{
    void messageChanged(final MessageChangedEvent p0);
}
