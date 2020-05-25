package javax.activation;

import java.awt.datatransfer.*;
import java.io.*;

public interface DataContentHandler
{
    DataFlavor[] getTransferDataFlavors();
    
    Object getTransferData(final DataFlavor p0, final DataSource p1) throws UnsupportedFlavorException, IOException;
    
    Object getContent(final DataSource p0) throws IOException;
    
    void writeTo(final Object p0, final String p1, final OutputStream p2) throws IOException;
}
