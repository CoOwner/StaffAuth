package javax.mail.search;

import javax.mail.*;

public final class HeaderTerm extends StringTerm
{
    protected String headerName;
    private static final long serialVersionUID = 8342514650333389122L;
    
    public HeaderTerm(final String headerName, final String pattern) {
        super(pattern);
        this.headerName = headerName;
    }
    
    public String getHeaderName() {
        return this.headerName;
    }
    
    public boolean match(final Message msg) {
        String[] headers;
        try {
            headers = msg.getHeader(this.headerName);
        }
        catch (Exception e) {
            return false;
        }
        if (headers == null) {
            return false;
        }
        for (int i = 0; i < headers.length; ++i) {
            if (super.match(headers[i])) {
                return true;
            }
        }
        return false;
    }
    
    public boolean equals(final Object obj) {
        if (!(obj instanceof HeaderTerm)) {
            return false;
        }
        final HeaderTerm ht = (HeaderTerm)obj;
        return ht.headerName.equalsIgnoreCase(this.headerName) && super.equals(ht);
    }
    
    public int hashCode() {
        return this.headerName.toLowerCase().hashCode() + super.hashCode();
    }
}
