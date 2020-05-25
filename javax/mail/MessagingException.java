package javax.mail;

public class MessagingException extends Exception
{
    private Exception next;
    private static final long serialVersionUID = -7569192289819959253L;
    
    public MessagingException() {
        this.initCause(null);
    }
    
    public MessagingException(final String s) {
        super(s);
        this.initCause(null);
    }
    
    public MessagingException(final String s, final Exception e) {
        super(s);
        this.next = e;
        this.initCause(null);
    }
    
    public synchronized Exception getNextException() {
        return this.next;
    }
    
    public synchronized Throwable getCause() {
        return this.next;
    }
    
    public synchronized boolean setNextException(final Exception ex) {
        Exception theEnd;
        for (theEnd = this; theEnd instanceof MessagingException && ((MessagingException)theEnd).next != null; theEnd = ((MessagingException)theEnd).next) {}
        if (theEnd instanceof MessagingException) {
            ((MessagingException)theEnd).next = ex;
            return true;
        }
        return false;
    }
    
    public synchronized String toString() {
        final String s = super.toString();
        Exception n = this.next;
        if (n == null) {
            return s;
        }
        final StringBuffer sb = new StringBuffer((s == null) ? "" : s);
        while (n != null) {
            sb.append(";\n  nested exception is:\n\t");
            sb.append(n.toString());
            if (n instanceof MessagingException) {
                final MessagingException mex = (MessagingException)n;
                n = mex.next;
            }
            else {
                n = null;
            }
        }
        return sb.toString();
    }
}
