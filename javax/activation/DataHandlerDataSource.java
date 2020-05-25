package javax.activation;

import java.io.*;

class DataHandlerDataSource implements DataSource
{
    DataHandler dataHandler;
    
    public DataHandlerDataSource(final DataHandler dataHandler) {
        this.dataHandler = null;
        this.dataHandler = dataHandler;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return this.dataHandler.getInputStream();
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.dataHandler.getOutputStream();
    }
    
    @Override
    public String getContentType() {
        return this.dataHandler.getContentType();
    }
    
    @Override
    public String getName() {
        return this.dataHandler.getName();
    }
}
