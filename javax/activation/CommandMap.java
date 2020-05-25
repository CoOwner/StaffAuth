package javax.activation;

import java.util.*;

public abstract class CommandMap
{
    private static CommandMap defaultCommandMap;
    private static Map<ClassLoader, CommandMap> map;
    
    public static synchronized CommandMap getDefaultCommandMap() {
        if (CommandMap.defaultCommandMap != null) {
            return CommandMap.defaultCommandMap;
        }
        final ClassLoader contextClassLoader = SecuritySupport.getContextClassLoader();
        CommandMap commandMap = CommandMap.map.get(contextClassLoader);
        if (commandMap == null) {
            commandMap = new MailcapCommandMap();
            CommandMap.map.put(contextClassLoader, commandMap);
        }
        return commandMap;
    }
    
    public static synchronized void setDefaultCommandMap(final CommandMap defaultCommandMap) {
        final SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            try {
                securityManager.checkSetFactory();
            }
            catch (SecurityException ex) {
                if (CommandMap.class.getClassLoader() == null || CommandMap.class.getClassLoader() != defaultCommandMap.getClass().getClassLoader()) {
                    throw ex;
                }
            }
        }
        CommandMap.map.remove(SecuritySupport.getContextClassLoader());
        CommandMap.defaultCommandMap = defaultCommandMap;
    }
    
    public abstract CommandInfo[] getPreferredCommands(final String p0);
    
    public CommandInfo[] getPreferredCommands(final String s, final DataSource dataSource) {
        return this.getPreferredCommands(s);
    }
    
    public abstract CommandInfo[] getAllCommands(final String p0);
    
    public CommandInfo[] getAllCommands(final String s, final DataSource dataSource) {
        return this.getAllCommands(s);
    }
    
    public abstract CommandInfo getCommand(final String p0, final String p1);
    
    public CommandInfo getCommand(final String s, final String s2, final DataSource dataSource) {
        return this.getCommand(s, s2);
    }
    
    public abstract DataContentHandler createDataContentHandler(final String p0);
    
    public DataContentHandler createDataContentHandler(final String s, final DataSource dataSource) {
        return this.createDataContentHandler(s);
    }
    
    public String[] getMimeTypes() {
        return null;
    }
    
    static {
        CommandMap.defaultCommandMap = null;
        CommandMap.map = new WeakHashMap<ClassLoader, CommandMap>();
    }
}
