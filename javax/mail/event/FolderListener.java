package javax.mail.event;

import java.util.*;

public interface FolderListener extends EventListener
{
    void folderCreated(final FolderEvent p0);
    
    void folderDeleted(final FolderEvent p0);
    
    void folderRenamed(final FolderEvent p0);
}
