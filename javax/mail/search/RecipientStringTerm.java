package javax.mail.search;

import javax.mail.*;

public final class RecipientStringTerm extends AddressStringTerm
{
    private Message.RecipientType type;
    private static final long serialVersionUID = -8293562089611618849L;
    
    public RecipientStringTerm(final Message.RecipientType type, final String pattern) {
        super(pattern);
        this.type = type;
    }
    
    public Message.RecipientType getRecipientType() {
        return this.type;
    }
    
    public boolean match(final Message msg) {
        Address[] recipients;
        try {
            recipients = msg.getRecipients(this.type);
        }
        catch (Exception e) {
            return false;
        }
        if (recipients == null) {
            return false;
        }
        for (int i = 0; i < recipients.length; ++i) {
            if (super.match(recipients[i])) {
                return true;
            }
        }
        return false;
    }
    
    public boolean equals(final Object obj) {
        if (!(obj instanceof RecipientStringTerm)) {
            return false;
        }
        final RecipientStringTerm rst = (RecipientStringTerm)obj;
        return rst.type.equals(this.type) && super.equals(obj);
    }
    
    public int hashCode() {
        return this.type.hashCode() + super.hashCode();
    }
}
