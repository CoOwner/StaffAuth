package javax.mail.search;

import javax.mail.*;

public final class FromStringTerm extends AddressStringTerm
{
    private static final long serialVersionUID = 5801127523826772788L;
    
    public FromStringTerm(final String pattern) {
        super(pattern);
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
        return obj instanceof FromStringTerm && super.equals(obj);
    }
}
