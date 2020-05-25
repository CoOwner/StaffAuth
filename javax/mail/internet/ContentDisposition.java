package javax.mail.internet;

public class ContentDisposition
{
    private String disposition;
    private ParameterList list;
    
    public ContentDisposition() {
    }
    
    public ContentDisposition(final String disposition, final ParameterList list) {
        this.disposition = disposition;
        this.list = list;
    }
    
    public ContentDisposition(final String s) throws ParseException {
        final HeaderTokenizer h = new HeaderTokenizer(s, "()<>@,;:\\\"\t []/?=");
        final HeaderTokenizer.Token tk = h.next();
        if (tk.getType() != -1) {
            throw new ParseException();
        }
        this.disposition = tk.getValue();
        final String rem = h.getRemainder();
        if (rem != null) {
            this.list = new ParameterList(rem);
        }
    }
    
    public String getDisposition() {
        return this.disposition;
    }
    
    public String getParameter(final String name) {
        if (this.list == null) {
            return null;
        }
        return this.list.get(name);
    }
    
    public ParameterList getParameterList() {
        return this.list;
    }
    
    public void setDisposition(final String disposition) {
        this.disposition = disposition;
    }
    
    public void setParameter(final String name, final String value) {
        if (this.list == null) {
            this.list = new ParameterList();
        }
        this.list.set(name, value);
    }
    
    public void setParameterList(final ParameterList list) {
        this.list = list;
    }
    
    public String toString() {
        if (this.disposition == null) {
            return null;
        }
        if (this.list == null) {
            return this.disposition;
        }
        final StringBuffer sb = new StringBuffer(this.disposition);
        sb.append(this.list.toString(sb.length() + 21));
        return sb.toString();
    }
}
