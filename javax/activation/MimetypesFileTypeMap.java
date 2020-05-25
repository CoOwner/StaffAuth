package javax.activation;

import java.util.*;
import com.sun.activation.registries.*;
import java.io.*;
import java.net.*;

public class MimetypesFileTypeMap extends FileTypeMap
{
    private MimeTypeFile[] DB;
    private static final int PROG = 0;
    private static String defaultType;
    
    public MimetypesFileTypeMap() {
        final Vector<MimeTypeFile> vector = new Vector<MimeTypeFile>(5);
        vector.addElement(null);
        LogSupport.log("MimetypesFileTypeMap: load HOME");
        try {
            final String property = System.getProperty("user.home");
            if (property != null) {
                final MimeTypeFile loadFile = this.loadFile(property + File.separator + ".mime.types");
                if (loadFile != null) {
                    vector.addElement(loadFile);
                }
            }
        }
        catch (SecurityException ex) {}
        LogSupport.log("MimetypesFileTypeMap: load SYS");
        try {
            final MimeTypeFile loadFile2 = this.loadFile(System.getProperty("java.home") + File.separator + "lib" + File.separator + "mime.types");
            if (loadFile2 != null) {
                vector.addElement(loadFile2);
            }
        }
        catch (SecurityException ex2) {}
        LogSupport.log("MimetypesFileTypeMap: load JAR");
        this.loadAllResources(vector, "META-INF/mime.types");
        LogSupport.log("MimetypesFileTypeMap: load DEF");
        final MimeTypeFile loadResource = this.loadResource("/META-INF/mimetypes.default");
        if (loadResource != null) {
            vector.addElement(loadResource);
        }
        vector.copyInto(this.DB = new MimeTypeFile[vector.size()]);
    }
    
    private MimeTypeFile loadResource(final String s) {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = SecuritySupport.getResourceAsStream(this.getClass(), s);
            if (resourceAsStream != null) {
                final MimeTypeFile mimeTypeFile = new MimeTypeFile(resourceAsStream);
                if (LogSupport.isLoggable()) {
                    LogSupport.log("MimetypesFileTypeMap: successfully loaded mime types file: " + s);
                }
                return mimeTypeFile;
            }
            if (LogSupport.isLoggable()) {
                LogSupport.log("MimetypesFileTypeMap: not loading mime types file: " + s);
            }
        }
        catch (IOException ex) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MimetypesFileTypeMap: can't load " + s, ex);
            }
        }
        catch (SecurityException ex2) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MimetypesFileTypeMap: can't load " + s, ex2);
            }
        }
        finally {
            try {
                if (resourceAsStream != null) {
                    resourceAsStream.close();
                }
            }
            catch (IOException ex3) {}
        }
        return null;
    }
    
    private void loadAllResources(final Vector vector, final String s) {
        boolean b = false;
        try {
            ClassLoader classLoader = SecuritySupport.getContextClassLoader();
            if (classLoader == null) {
                classLoader = this.getClass().getClassLoader();
            }
            URL[] array;
            if (classLoader != null) {
                array = SecuritySupport.getResources(classLoader, s);
            }
            else {
                array = SecuritySupport.getSystemResources(s);
            }
            if (array != null) {
                if (LogSupport.isLoggable()) {
                    LogSupport.log("MimetypesFileTypeMap: getResources");
                }
                for (int i = 0; i < array.length; ++i) {
                    final URL url = array[i];
                    InputStream openStream = null;
                    Label_0112: {
                        if (!LogSupport.isLoggable()) {
                            break Label_0112;
                        }
                        LogSupport.log("MimetypesFileTypeMap: URL " + url);
                        try {
                            openStream = SecuritySupport.openStream(url);
                            if (openStream != null) {
                                vector.addElement(new MimeTypeFile(openStream));
                                b = true;
                                if (LogSupport.isLoggable()) {
                                    LogSupport.log("MimetypesFileTypeMap: successfully loaded mime types from URL: " + url);
                                }
                            }
                            else if (LogSupport.isLoggable()) {
                                LogSupport.log("MimetypesFileTypeMap: not loading mime types from URL: " + url);
                            }
                        }
                        catch (IOException ex) {
                            if (LogSupport.isLoggable()) {
                                LogSupport.log("MimetypesFileTypeMap: can't load " + url, ex);
                            }
                        }
                        catch (SecurityException ex2) {
                            if (LogSupport.isLoggable()) {
                                LogSupport.log("MimetypesFileTypeMap: can't load " + url, ex2);
                            }
                        }
                        finally {
                            try {
                                if (openStream != null) {
                                    openStream.close();
                                }
                            }
                            catch (IOException ex4) {}
                        }
                    }
                }
            }
        }
        catch (Exception ex3) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MimetypesFileTypeMap: can't load " + s, ex3);
            }
        }
        if (!b) {
            LogSupport.log("MimetypesFileTypeMap: !anyLoaded");
            final MimeTypeFile loadResource = this.loadResource("/" + s);
            if (loadResource != null) {
                vector.addElement(loadResource);
            }
        }
    }
    
    private MimeTypeFile loadFile(final String s) {
        MimeTypeFile mimeTypeFile = null;
        try {
            mimeTypeFile = new MimeTypeFile(s);
        }
        catch (IOException ex) {}
        return mimeTypeFile;
    }
    
    public MimetypesFileTypeMap(final String s) throws IOException {
        this();
        this.DB[0] = new MimeTypeFile(s);
    }
    
    public MimetypesFileTypeMap(final InputStream inputStream) {
        this();
        try {
            this.DB[0] = new MimeTypeFile(inputStream);
        }
        catch (IOException ex) {}
    }
    
    public synchronized void addMimeTypes(final String s) {
        if (this.DB[0] == null) {
            this.DB[0] = new MimeTypeFile();
        }
        this.DB[0].appendToRegistry(s);
    }
    
    @Override
    public String getContentType(final File file) {
        return this.getContentType(file.getName());
    }
    
    @Override
    public synchronized String getContentType(final String s) {
        final int lastIndex = s.lastIndexOf(".");
        if (lastIndex < 0) {
            return MimetypesFileTypeMap.defaultType;
        }
        final String substring = s.substring(lastIndex + 1);
        if (substring.length() == 0) {
            return MimetypesFileTypeMap.defaultType;
        }
        for (int i = 0; i < this.DB.length; ++i) {
            if (this.DB[i] != null) {
                final String mimeTypeString = this.DB[i].getMIMETypeString(substring);
                if (mimeTypeString != null) {
                    return mimeTypeString;
                }
            }
        }
        return MimetypesFileTypeMap.defaultType;
    }
    
    static {
        MimetypesFileTypeMap.defaultType = "application/octet-stream";
    }
}
