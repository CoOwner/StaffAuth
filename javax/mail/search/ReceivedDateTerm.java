package javax.mail.search;

import java.util.*;
import javax.mail.*;

public final class ReceivedDateTerm extends DateTerm
{
    private static final long serialVersionUID = -2756695246195503170L;
    
    public ReceivedDateTerm(final int comparison, final Date date) {
        super(comparison, date);
    }
    
    public boolean match(final Message msg) {
        Date d;
        try {
            d = msg.getReceivedDate();
        }
        catch (Exception e) {
            return false;
        }
        return d != null && super.match(d);
    }
    
    public boolean equals(final Object obj) {
        return obj instanceof ReceivedDateTerm && super.equals(obj);
    }
}
