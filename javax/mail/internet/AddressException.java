package javax.mail.internet;

public class AddressException extends ParseException
{
    protected String ref;
    protected int pos;
    private static final long serialVersionUID = 9134583443539323120L;
    
    public AddressException() {
        this.ref = null;
        this.pos = -1;
    }
    
    public AddressException(final String s) {
        super(s);
        this.ref = null;
        this.pos = -1;
    }
    
    public AddressException(final String s, final String ref) {
        super(s);
        this.ref = null;
        this.pos = -1;
        this.ref = ref;
    }
    
    public AddressException(final String s, final String ref, final int pos) {
        super(s);
        this.ref = null;
        this.pos = -1;
        this.ref = ref;
        this.pos = pos;
    }
    
    public String getRef() {
        return this.ref;
    }
    
    public int getPos() {
        return this.pos;
    }
    
    public String toString() {
        String s = super.toString();
        if (this.ref == null) {
            return s;
        }
        s = s + " in string ``" + this.ref + "''";
        if (this.pos < 0) {
            return s;
        }
        return s + " at position " + this.pos;
    }
}
