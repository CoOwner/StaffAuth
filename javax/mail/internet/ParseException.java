package javax.mail.internet;

import javax.mail.*;

public class ParseException extends MessagingException
{
    private static final long serialVersionUID = 7649991205183658089L;
    
    public ParseException() {
    }
    
    public ParseException(final String s) {
        super(s);
    }
}
