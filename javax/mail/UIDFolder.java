package javax.mail;

public interface UIDFolder
{
    public static final long LASTUID = -1L;
    
    long getUIDValidity() throws MessagingException;
    
    Message getMessageByUID(final long p0) throws MessagingException;
    
    Message[] getMessagesByUID(final long p0, final long p1) throws MessagingException;
    
    Message[] getMessagesByUID(final long[] p0) throws MessagingException;
    
    long getUID(final Message p0) throws MessagingException;
    
    public static class FetchProfileItem extends FetchProfile.Item
    {
        public static final FetchProfileItem UID;
        
        protected FetchProfileItem(final String name) {
            super(name);
        }
        
        static {
            UID = new FetchProfileItem("UID");
        }
    }
}
