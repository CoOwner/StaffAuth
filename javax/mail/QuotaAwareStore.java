package javax.mail;

public interface QuotaAwareStore
{
    Quota[] getQuota(final String p0) throws MessagingException;
    
    void setQuota(final Quota p0) throws MessagingException;
}
