package javax.mail.search;

import javax.mail.*;

public final class NotTerm extends SearchTerm
{
    protected SearchTerm term;
    private static final long serialVersionUID = 7152293214217310216L;
    
    public NotTerm(final SearchTerm t) {
        this.term = t;
    }
    
    public SearchTerm getTerm() {
        return this.term;
    }
    
    public boolean match(final Message msg) {
        return !this.term.match(msg);
    }
    
    public boolean equals(final Object obj) {
        if (!(obj instanceof NotTerm)) {
            return false;
        }
        final NotTerm nt = (NotTerm)obj;
        return nt.term.equals(this.term);
    }
    
    public int hashCode() {
        return this.term.hashCode() << 1;
    }
}
