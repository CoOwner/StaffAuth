package javax.mail.search;

import javax.mail.*;

public final class FromTerm extends AddressTerm
{
    private static final long serialVersionUID = 5214730291502658665L;
    
    public FromTerm(final Address address) {
        super(address);
    }
    
    public boolean match(final Message msg) {
        Address[] from;
        try {
            from = msg.getFrom();
        }
        catch (Exception e) {
            return false;
        }
        if (from == null) {
            return false;
        }
        for (int i = 0; i < from.length; ++i) {
            if (super.match(from[i])) {
                return true;
            }
        }
        return false;
    }
    
    public boolean equals(final Object obj) {
        return obj instanceof FromTerm && super.equals(obj);
    }
}
