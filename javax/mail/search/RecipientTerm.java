package javax.mail.search;

import javax.mail.*;

public final class RecipientTerm extends AddressTerm
{
    protected Message.RecipientType type;
    private static final long serialVersionUID = 6548700653122680468L;
    
    public RecipientTerm(final Message.RecipientType type, final Address address) {
        super(address);
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
        if (!(obj instanceof RecipientTerm)) {
            return false;
        }
        final RecipientTerm rt = (RecipientTerm)obj;
        return rt.type.equals(this.type) && super.equals(obj);
    }
    
    public int hashCode() {
        return this.type.hashCode() + super.hashCode();
    }
}
