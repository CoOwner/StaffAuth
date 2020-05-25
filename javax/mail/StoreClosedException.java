package javax.mail;

public class StoreClosedException extends MessagingException
{
    private transient Store store;
    private static final long serialVersionUID = -3145392336120082655L;
    
    public StoreClosedException(final Store store) {
        this(store, null);
    }
    
    public StoreClosedException(final Store store, final String message) {
        super(message);
        this.store = store;
    }
    
    public Store getStore() {
        return this.store;
    }
}
