package javax.activation;

import java.net.*;
import java.io.*;
import java.awt.datatransfer.*;

public class DataHandler implements Transferable
{
    private DataSource dataSource;
    private DataSource objDataSource;
    private Object object;
    private String objectMimeType;
    private CommandMap currentCommandMap;
    private static final DataFlavor[] emptyFlavors;
    private DataFlavor[] transferFlavors;
    private DataContentHandler dataContentHandler;
    private DataContentHandler factoryDCH;
    private static DataContentHandlerFactory factory;
    private DataContentHandlerFactory oldFactory;
    private String shortType;
    
    public DataHandler(final DataSource dataSource) {
        this.dataSource = null;
        this.objDataSource = null;
        this.object = null;
        this.objectMimeType = null;
        this.currentCommandMap = null;
        this.transferFlavors = DataHandler.emptyFlavors;
        this.dataContentHandler = null;
        this.factoryDCH = null;
        this.oldFactory = null;
        this.shortType = null;
        this.dataSource = dataSource;
        this.oldFactory = DataHandler.factory;
    }
    
    public DataHandler(final Object object, final String objectMimeType) {
        this.dataSource = null;
        this.objDataSource = null;
        this.object = null;
        this.objectMimeType = null;
        this.currentCommandMap = null;
        this.transferFlavors = DataHandler.emptyFlavors;
        this.dataContentHandler = null;
        this.factoryDCH = null;
        this.oldFactory = null;
        this.shortType = null;
        this.object = object;
        this.objectMimeType = objectMimeType;
        this.oldFactory = DataHandler.factory;
    }
    
    public DataHandler(final URL url) {
        this.dataSource = null;
        this.objDataSource = null;
        this.object = null;
        this.objectMimeType = null;
        this.currentCommandMap = null;
        this.transferFlavors = DataHandler.emptyFlavors;
        this.dataContentHandler = null;
        this.factoryDCH = null;
        this.oldFactory = null;
        this.shortType = null;
        this.dataSource = new URLDataSource(url);
        this.oldFactory = DataHandler.factory;
    }
    
    private synchronized CommandMap getCommandMap() {
        if (this.currentCommandMap != null) {
            return this.currentCommandMap;
        }
        return CommandMap.getDefaultCommandMap();
    }
    
    public DataSource getDataSource() {
        if (this.dataSource == null) {
            if (this.objDataSource == null) {
                this.objDataSource = new DataHandlerDataSource(this);
            }
            return this.objDataSource;
        }
        return this.dataSource;
    }
    
    public String getName() {
        if (this.dataSource != null) {
            return this.dataSource.getName();
        }
        return null;
    }
    
    public String getContentType() {
        if (this.dataSource != null) {
            return this.dataSource.getContentType();
        }
        return this.objectMimeType;
    }
    
    public InputStream getInputStream() throws IOException {
        InputStream inputStream;
        if (this.dataSource != null) {
            inputStream = this.dataSource.getInputStream();
        }
        else {
            final DataContentHandler dataContentHandler = this.getDataContentHandler();
            if (dataContentHandler == null) {
                throw new UnsupportedDataTypeException("no DCH for MIME type " + this.getBaseType());
            }
            if (dataContentHandler instanceof ObjectDataContentHandler && ((ObjectDataContentHandler)dataContentHandler).getDCH() == null) {
                throw new UnsupportedDataTypeException("no object DCH for MIME type " + this.getBaseType());
            }
            final ObjectDataContentHandler objectDataContentHandler = (ObjectDataContentHandler)dataContentHandler;
            final PipedOutputStream pipedOutputStream = new PipedOutputStream();
            final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        objectDataContentHandler.writeTo(DataHandler.this.object, DataHandler.this.objectMimeType, pipedOutputStream);
                    }
                    catch (IOException ex) {}
                    finally {
                        try {
                            pipedOutputStream.close();
                        }
                        catch (IOException ex2) {}
                    }
                }
            }, "DataHandler.getInputStream").start();
            inputStream = pipedInputStream;
        }
        return inputStream;
    }
    
    public void writeTo(final OutputStream outputStream) throws IOException {
        if (this.dataSource != null) {
            final byte[] array = new byte[8192];
            final InputStream inputStream = this.dataSource.getInputStream();
            try {
                int read;
                while ((read = inputStream.read(array)) > 0) {
                    outputStream.write(array, 0, read);
                }
            }
            finally {
                inputStream.close();
            }
        }
        else {
            this.getDataContentHandler().writeTo(this.object, this.objectMimeType, outputStream);
        }
    }
    
    public OutputStream getOutputStream() throws IOException {
        if (this.dataSource != null) {
            return this.dataSource.getOutputStream();
        }
        return null;
    }
    
    @Override
    public synchronized DataFlavor[] getTransferDataFlavors() {
        if (DataHandler.factory != this.oldFactory) {
            this.transferFlavors = DataHandler.emptyFlavors;
        }
        if (this.transferFlavors == DataHandler.emptyFlavors) {
            this.transferFlavors = this.getDataContentHandler().getTransferDataFlavors();
        }
        if (this.transferFlavors == DataHandler.emptyFlavors) {
            return this.transferFlavors;
        }
        return this.transferFlavors.clone();
    }
    
    @Override
    public boolean isDataFlavorSupported(final DataFlavor dataFlavor) {
        final DataFlavor[] transferDataFlavors = this.getTransferDataFlavors();
        for (int i = 0; i < transferDataFlavors.length; ++i) {
            if (transferDataFlavors[i].equals(dataFlavor)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Object getTransferData(final DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
        return this.getDataContentHandler().getTransferData(dataFlavor, this.dataSource);
    }
    
    public synchronized void setCommandMap(final CommandMap currentCommandMap) {
        if (currentCommandMap != this.currentCommandMap || currentCommandMap == null) {
            this.transferFlavors = DataHandler.emptyFlavors;
            this.dataContentHandler = null;
            this.currentCommandMap = currentCommandMap;
        }
    }
    
    public CommandInfo[] getPreferredCommands() {
        if (this.dataSource != null) {
            return this.getCommandMap().getPreferredCommands(this.getBaseType(), this.dataSource);
        }
        return this.getCommandMap().getPreferredCommands(this.getBaseType());
    }
    
    public CommandInfo[] getAllCommands() {
        if (this.dataSource != null) {
            return this.getCommandMap().getAllCommands(this.getBaseType(), this.dataSource);
        }
        return this.getCommandMap().getAllCommands(this.getBaseType());
    }
    
    public CommandInfo getCommand(final String s) {
        if (this.dataSource != null) {
            return this.getCommandMap().getCommand(this.getBaseType(), s, this.dataSource);
        }
        return this.getCommandMap().getCommand(this.getBaseType(), s);
    }
    
    public Object getContent() throws IOException {
        if (this.object != null) {
            return this.object;
        }
        return this.getDataContentHandler().getContent(this.getDataSource());
    }
    
    public Object getBean(final CommandInfo commandInfo) {
        Object commandObject = null;
        try {
            ClassLoader classLoader = SecuritySupport.getContextClassLoader();
            if (classLoader == null) {
                classLoader = this.getClass().getClassLoader();
            }
            commandObject = commandInfo.getCommandObject(this, classLoader);
        }
        catch (IOException ex) {}
        catch (ClassNotFoundException ex2) {}
        return commandObject;
    }
    
    private synchronized DataContentHandler getDataContentHandler() {
        if (DataHandler.factory != this.oldFactory) {
            this.oldFactory = DataHandler.factory;
            this.factoryDCH = null;
            this.dataContentHandler = null;
            this.transferFlavors = DataHandler.emptyFlavors;
        }
        if (this.dataContentHandler != null) {
            return this.dataContentHandler;
        }
        final String baseType = this.getBaseType();
        if (this.factoryDCH == null && DataHandler.factory != null) {
            this.factoryDCH = DataHandler.factory.createDataContentHandler(baseType);
        }
        if (this.factoryDCH != null) {
            this.dataContentHandler = this.factoryDCH;
        }
        if (this.dataContentHandler == null) {
            if (this.dataSource != null) {
                this.dataContentHandler = this.getCommandMap().createDataContentHandler(baseType, this.dataSource);
            }
            else {
                this.dataContentHandler = this.getCommandMap().createDataContentHandler(baseType);
            }
        }
        if (this.dataSource != null) {
            this.dataContentHandler = new DataSourceDataContentHandler(this.dataContentHandler, this.dataSource);
        }
        else {
            this.dataContentHandler = new ObjectDataContentHandler(this.dataContentHandler, this.object, this.objectMimeType);
        }
        return this.dataContentHandler;
    }
    
    private synchronized String getBaseType() {
        if (this.shortType == null) {
            final String contentType = this.getContentType();
            try {
                this.shortType = new MimeType(contentType).getBaseType();
            }
            catch (MimeTypeParseException ex) {
                this.shortType = contentType;
            }
        }
        return this.shortType;
    }
    
    public static synchronized void setDataContentHandlerFactory(final DataContentHandlerFactory factory) {
        if (DataHandler.factory != null) {
            throw new Error("DataContentHandlerFactory already defined");
        }
        final SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            try {
                securityManager.checkSetFactory();
            }
            catch (SecurityException ex) {
                if (DataHandler.class.getClassLoader() != factory.getClass().getClassLoader()) {
                    throw ex;
                }
            }
        }
        DataHandler.factory = factory;
    }
    
    static {
        emptyFlavors = new DataFlavor[0];
        DataHandler.factory = null;
    }
}
