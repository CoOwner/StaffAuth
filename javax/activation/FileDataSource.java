package javax.activation;

import java.io.*;

public class FileDataSource implements DataSource
{
    private File _file;
    private FileTypeMap typeMap;
    
    public FileDataSource(final File file) {
        this._file = null;
        this.typeMap = null;
        this._file = file;
    }
    
    public FileDataSource(final String s) {
        this(new File(s));
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(this._file);
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(this._file);
    }
    
    @Override
    public String getContentType() {
        if (this.typeMap == null) {
            return FileTypeMap.getDefaultFileTypeMap().getContentType(this._file);
        }
        return this.typeMap.getContentType(this._file);
    }
    
    @Override
    public String getName() {
        return this._file.getName();
    }
    
    public File getFile() {
        return this._file;
    }
    
    public void setFileTypeMap(final FileTypeMap typeMap) {
        this.typeMap = typeMap;
    }
}
