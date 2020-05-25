package javax.mail.internet;

import javax.activation.*;

class CachedDataHandler extends DataHandler
{
    public CachedDataHandler(final Object o, final String mimeType) {
        super(o, mimeType);
    }
}
