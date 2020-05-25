package javax.mail;

public class ReadOnlyFolderException extends MessagingException
{
    private transient Folder folder;
    private static final long serialVersionUID = 5711829372799039325L;
    
    public ReadOnlyFolderException(final Folder folder) {
        this(folder, null);
    }
    
    public ReadOnlyFolderException(final Folder folder, final String message) {
        super(message);
        this.folder = folder;
    }
    
    public Folder getFolder() {
        return this.folder;
    }
}
