package javax.mail.search;

import javax.mail.*;
import javax.mail.internet.*;

public abstract class AddressStringTerm extends StringTerm
{
    private static final long serialVersionUID = 3086821234204980368L;
    
    protected AddressStringTerm(final String pattern) {
        super(pattern, true);
    }
    
    protected boolean match(final Address a) {
        if (a instanceof InternetAddress) {
            final InternetAddress ia = (InternetAddress)a;
            return super.match(ia.toUnicodeString());
        }
        return super.match(a.toString());
    }
    
    public boolean equals(final Object obj) {
        return obj instanceof AddressStringTerm && super.equals(obj);
    }
}
