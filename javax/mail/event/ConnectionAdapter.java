package javax.mail.event;

public abstract class ConnectionAdapter implements ConnectionListener
{
    public void opened(final ConnectionEvent e) {
    }
    
    public void disconnected(final ConnectionEvent e) {
    }
    
    public void closed(final ConnectionEvent e) {
    }
}
