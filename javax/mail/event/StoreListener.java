package javax.mail.event;

import java.util.*;

public interface StoreListener extends EventListener
{
    void notification(final StoreEvent p0);
}
