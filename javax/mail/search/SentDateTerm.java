package javax.mail.search;

import java.util.*;
import javax.mail.*;

public final class SentDateTerm extends DateTerm
{
    private static final long serialVersionUID = 5647755030530907263L;
    
    public SentDateTerm(final int comparison, final Date date) {
        super(comparison, date);
    }
    
    public boolean match(final Message msg) {
        Date d;
        try {
            d = msg.getSentDate();
        }
        catch (Exception e) {
            return false;
        }
        return d != null && super.match(d);
    }
    
    public boolean equals(final Object obj) {
        return obj instanceof SentDateTerm && super.equals(obj);
    }
}
