package javax.mail;

public class NoSuchProviderException extends MessagingException
{
    private static final long serialVersionUID = 8058319293154708827L;
    
    public NoSuchProviderException() {
    }
    
    public NoSuchProviderException(final String message) {
        super(message);
    }
}
