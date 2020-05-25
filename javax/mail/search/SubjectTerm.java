package javax.mail.search;

import javax.mail.*;

public final class SubjectTerm extends StringTerm
{
    private static final long serialVersionUID = 7481568618055573432L;
    
    public SubjectTerm(final String pattern) {
        super(pattern);
    }
    
    public boolean match(final Message msg) {
        String subj;
        try {
            subj = msg.getSubject();
        }
        catch (Exception e) {
            return false;
        }
        return subj != null && super.match(subj);
    }
    
    public boolean equals(final Object obj) {
        return obj instanceof SubjectTerm && super.equals(obj);
    }
}
