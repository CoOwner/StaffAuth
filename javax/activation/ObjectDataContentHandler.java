package javax.activation;

import java.awt.datatransfer.*;
import java.io.*;

class ObjectDataContentHandler implements DataContentHandler
{
    private DataFlavor[] transferFlavors;
    private Object obj;
    private String mimeType;
    private DataContentHandler dch;
    
    public ObjectDataContentHandler(final DataContentHandler dch, final Object obj, final String mimeType) {
        this.transferFlavors = null;
        this.dch = null;
        this.obj = obj;
        this.mimeType = mimeType;
        this.dch = dch;
    }
    
    public DataContentHandler getDCH() {
        return this.dch;
    }
    
    @Override
    public synchronized DataFlavor[] getTransferDataFlavors() {
        if (this.transferFlavors == null) {
            if (this.dch != null) {
                this.transferFlavors = this.dch.getTransferDataFlavors();
            }
            else {
                (this.transferFlavors = new DataFlavor[1])[0] = new ActivationDataFlavor(this.obj.getClass(), this.mimeType, this.mimeType);
            }
        }
        return this.transferFlavors;
    }
    
    @Override
    public Object getTransferData(final DataFlavor dataFlavor, final DataSource dataSource) throws UnsupportedFlavorException, IOException {
        if (this.dch != null) {
            return this.dch.getTransferData(dataFlavor, dataSource);
        }
        if (dataFlavor.equals(this.getTransferDataFlavors()[0])) {
            return this.obj;
        }
        throw new UnsupportedFlavorException(dataFlavor);
    }
    
    @Override
    public Object getContent(final DataSource dataSource) {
        return this.obj;
    }
    
    @Override
    public void writeTo(final Object o, final String s, final OutputStream outputStream) throws IOException {
        if (this.dch != null) {
            this.dch.writeTo(o, s, outputStream);
        }
        else if (o instanceof byte[]) {
            outputStream.write((byte[])o);
        }
        else {
            if (!(o instanceof String)) {
                throw new UnsupportedDataTypeException("no object DCH for MIME type " + this.mimeType);
            }
            final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write((String)o);
            outputStreamWriter.flush();
        }
    }
}
