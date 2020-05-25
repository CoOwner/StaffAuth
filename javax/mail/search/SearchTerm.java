package javax.mail.search;

import java.io.*;
import javax.mail.*;

public abstract class SearchTerm implements Serializable
{
    private static final long serialVersionUID = -6652358452205992789L;
    
    public abstract boolean match(final Message p0);
}
