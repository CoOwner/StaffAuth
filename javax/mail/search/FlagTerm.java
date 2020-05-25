package javax.mail.search;

import javax.mail.*;

public final class FlagTerm extends SearchTerm
{
    protected boolean set;
    protected Flags flags;
    private static final long serialVersionUID = -142991500302030647L;
    
    public FlagTerm(final Flags flags, final boolean set) {
        this.flags = flags;
        this.set = set;
    }
    
    public Flags getFlags() {
        return (Flags)this.flags.clone();
    }
    
    public boolean getTestSet() {
        return this.set;
    }
    
    public boolean match(final Message msg) {
        try {
            final Flags f = msg.getFlags();
            if (this.set) {
                return f.contains(this.flags);
            }
            final Flags.Flag[] sf = this.flags.getSystemFlags();
            for (int i = 0; i < sf.length; ++i) {
                if (f.contains(sf[i])) {
                    return false;
                }
            }
            final String[] s = this.flags.getUserFlags();
            for (int j = 0; j < s.length; ++j) {
                if (f.contains(s[j])) {
                    return false;
                }
            }
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
    
    public boolean equals(final Object obj) {
        if (!(obj instanceof FlagTerm)) {
            return false;
        }
        final FlagTerm ft = (FlagTerm)obj;
        return ft.set == this.set && ft.flags.equals(this.flags);
    }
    
    public int hashCode() {
        return this.set ? this.flags.hashCode() : (~this.flags.hashCode());
    }
}
