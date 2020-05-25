package javax.mail.event;

public abstract class TransportAdapter implements TransportListener
{
    public void messageDelivered(final TransportEvent e) {
    }
    
    public void messageNotDelivered(final TransportEvent e) {
    }
    
    public void messagePartiallyDelivered(final TransportEvent e) {
    }
}
