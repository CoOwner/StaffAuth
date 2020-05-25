package javax.mail.search;

import javax.mail.*;

public final class AndTerm extends SearchTerm
{
    protected SearchTerm[] terms;
    private static final long serialVersionUID = -3583274505380989582L;
    
    public AndTerm(final SearchTerm t1, final SearchTerm t2) {
        (this.terms = new SearchTerm[2])[0] = t1;
        this.terms[1] = t2;
    }
    
    public AndTerm(final SearchTerm[] t) {
        this.terms = new SearchTerm[t.length];
        for (int i = 0; i < t.length; ++i) {
            this.terms[i] = t[i];
        }
    }
    
    public SearchTerm[] getTerms() {
        return this.terms.clone();
    }
    
    public boolean match(final Message msg) {
        for (int i = 0; i < this.terms.length; ++i) {
            if (!this.terms[i].match(msg)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean equals(final Object obj) {
        if (!(obj instanceof AndTerm)) {
            return false;
        }
        final AndTerm at = (AndTerm)obj;
        if (at.terms.length != this.terms.length) {
            return false;
        }
        for (int i = 0; i < this.terms.length; ++i) {
            if (!this.terms[i].equals(at.terms[i])) {
                return false;
            }
        }
        return true;
    }
    
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < this.terms.length; ++i) {
            hash += this.terms[i].hashCode();
        }
        return hash;
    }
}
