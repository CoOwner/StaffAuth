package javax.activation;

import java.io.*;
import java.util.*;

public abstract class FileTypeMap
{
    private static FileTypeMap defaultMap;
    private static Map<ClassLoader, FileTypeMap> map;
    
    public abstract String getContentType(final File p0);
    
    public abstract String getContentType(final String p0);
    
    public static synchronized void setDefaultFileTypeMap(final FileTypeMap defaultMap) {
        final SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            try {
                securityManager.checkSetFactory();
            }
            catch (SecurityException ex) {
                if (FileTypeMap.class.getClassLoader() == null || FileTypeMap.class.getClassLoader() != defaultMap.getClass().getClassLoader()) {
                    throw ex;
                }
            }
        }
        FileTypeMap.map.remove(SecuritySupport.getContextClassLoader());
        FileTypeMap.defaultMap = defaultMap;
    }
    
    public static synchronized FileTypeMap getDefaultFileTypeMap() {
        if (FileTypeMap.defaultMap != null) {
            return FileTypeMap.defaultMap;
        }
        final ClassLoader contextClassLoader = SecuritySupport.getContextClassLoader();
        FileTypeMap fileTypeMap = FileTypeMap.map.get(contextClassLoader);
        if (fileTypeMap == null) {
            fileTypeMap = new MimetypesFileTypeMap();
            FileTypeMap.map.put(contextClassLoader, fileTypeMap);
        }
        return fileTypeMap;
    }
    
    static {
        FileTypeMap.defaultMap = null;
        FileTypeMap.map = new WeakHashMap<ClassLoader, FileTypeMap>();
    }
}
