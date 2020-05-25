package javax.mail.search;

import javax.mail.*;

public abstract class AddressTerm extends SearchTerm
{
    protected Address address;
    private static final long serialVersionUID = 2005405551929769980L;
    
    protected AddressTerm(final Address address) {
        this.address = address;
    }
    
    public Address getAddress() {
        return this.address;
    }
    
    protected boolean match(final Address a) {
        return a.equals(this.address);
    }
    
    public boolean equals(final Object obj) {
        if (!(obj instanceof AddressTerm)) {
            return false;
        }
        final AddressTerm at = (AddressTerm)obj;
        return at.address.equals(this.address);
    }
    
    public int hashCode() {
        return this.address.hashCode();
    }
}
