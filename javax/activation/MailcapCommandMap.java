package javax.activation;

import com.sun.activation.registries.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class MailcapCommandMap extends CommandMap
{
    private MailcapFile[] DB;
    private static final int PROG = 0;
    
    public MailcapCommandMap() {
        final ArrayList<MailcapFile> list = new ArrayList<MailcapFile>(5);
        list.add(null);
        LogSupport.log("MailcapCommandMap: load HOME");
        try {
            final String property = System.getProperty("user.home");
            if (property != null) {
                final MailcapFile loadFile = this.loadFile(property + File.separator + ".mailcap");
                if (loadFile != null) {
                    list.add(loadFile);
                }
            }
        }
        catch (SecurityException ex) {}
        LogSupport.log("MailcapCommandMap: load SYS");
        try {
            final MailcapFile loadFile2 = this.loadFile(System.getProperty("java.home") + File.separator + "lib" + File.separator + "mailcap");
            if (loadFile2 != null) {
                list.add(loadFile2);
            }
        }
        catch (SecurityException ex2) {}
        LogSupport.log("MailcapCommandMap: load JAR");
        this.loadAllResources(list, "META-INF/mailcap");
        LogSupport.log("MailcapCommandMap: load DEF");
        final MailcapFile loadResource = this.loadResource("/META-INF/mailcap.default");
        if (loadResource != null) {
            list.add(loadResource);
        }
        this.DB = new MailcapFile[list.size()];
        this.DB = list.toArray(this.DB);
    }
    
    private MailcapFile loadResource(final String s) {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = SecuritySupport.getResourceAsStream(this.getClass(), s);
            if (resourceAsStream != null) {
                final MailcapFile mailcapFile = new MailcapFile(resourceAsStream);
                if (LogSupport.isLoggable()) {
                    LogSupport.log("MailcapCommandMap: successfully loaded mailcap file: " + s);
                }
                return mailcapFile;
            }
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: not loading mailcap file: " + s);
            }
        }
        catch (IOException ex) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: can't load " + s, ex);
            }
        }
        catch (SecurityException ex2) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: can't load " + s, ex2);
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
    
    private void loadAllResources(final List list, final String s) {
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
                    LogSupport.log("MailcapCommandMap: getResources");
                }
                for (int i = 0; i < array.length; ++i) {
                    final URL url = array[i];
                    InputStream openStream = null;
                    Label_0112: {
                        if (!LogSupport.isLoggable()) {
                            break Label_0112;
                        }
                        LogSupport.log("MailcapCommandMap: URL " + url);
                        try {
                            openStream = SecuritySupport.openStream(url);
                            if (openStream != null) {
                                list.add(new MailcapFile(openStream));
                                b = true;
                                if (LogSupport.isLoggable()) {
                                    LogSupport.log("MailcapCommandMap: successfully loaded mailcap file from URL: " + url);
                                }
                            }
                            else if (LogSupport.isLoggable()) {
                                LogSupport.log("MailcapCommandMap: not loading mailcap file from URL: " + url);
                            }
                        }
                        catch (IOException ex) {
                            if (LogSupport.isLoggable()) {
                                LogSupport.log("MailcapCommandMap: can't load " + url, ex);
                            }
                        }
                        catch (SecurityException ex2) {
                            if (LogSupport.isLoggable()) {
                                LogSupport.log("MailcapCommandMap: can't load " + url, ex2);
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
                LogSupport.log("MailcapCommandMap: can't load " + s, ex3);
            }
        }
        if (!b) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: !anyLoaded");
            }
            final MailcapFile loadResource = this.loadResource("/" + s);
            if (loadResource != null) {
                list.add(loadResource);
            }
        }
    }
    
    private MailcapFile loadFile(final String s) {
        MailcapFile mailcapFile = null;
        try {
            mailcapFile = new MailcapFile(s);
        }
        catch (IOException ex) {}
        return mailcapFile;
    }
    
    public MailcapCommandMap(final String s) throws IOException {
        this();
        if (LogSupport.isLoggable()) {
            LogSupport.log("MailcapCommandMap: load PROG from " + s);
        }
        if (this.DB[0] == null) {
            this.DB[0] = new MailcapFile(s);
        }
    }
    
    public MailcapCommandMap(final InputStream inputStream) {
        this();
        LogSupport.log("MailcapCommandMap: load PROG");
        if (this.DB[0] == null) {
            try {
                this.DB[0] = new MailcapFile(inputStream);
            }
            catch (IOException ex) {}
        }
    }
    
    @Override
    public synchronized CommandInfo[] getPreferredCommands(String lowerCase) {
        final ArrayList list = new ArrayList();
        if (lowerCase != null) {
            lowerCase = lowerCase.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.DB.length; ++i) {
            if (this.DB[i] != null) {
                final Map mailcapList = this.DB[i].getMailcapList(lowerCase);
                if (mailcapList != null) {
                    this.appendPrefCmdsToList(mailcapList, list);
                }
            }
        }
        for (int j = 0; j < this.DB.length; ++j) {
            if (this.DB[j] != null) {
                final Map mailcapFallbackList = this.DB[j].getMailcapFallbackList(lowerCase);
                if (mailcapFallbackList != null) {
                    this.appendPrefCmdsToList(mailcapFallbackList, list);
                }
            }
        }
        return (CommandInfo[])list.toArray(new CommandInfo[list.size()]);
    }
    
    private void appendPrefCmdsToList(final Map map, final List list) {
        for (final String s : map.keySet()) {
            if (!this.checkForVerb(list, s)) {
                list.add(new CommandInfo(s, (String)((List)map.get(s)).get(0)));
            }
        }
    }
    
    private boolean checkForVerb(final List list, final String s) {
        final Iterator<CommandInfo> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getCommandName().equals(s)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public synchronized CommandInfo[] getAllCommands(String lowerCase) {
        final ArrayList list = new ArrayList();
        if (lowerCase != null) {
            lowerCase = lowerCase.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.DB.length; ++i) {
            if (this.DB[i] != null) {
                final Map mailcapList = this.DB[i].getMailcapList(lowerCase);
                if (mailcapList != null) {
                    this.appendCmdsToList(mailcapList, list);
                }
            }
        }
        for (int j = 0; j < this.DB.length; ++j) {
            if (this.DB[j] != null) {
                final Map mailcapFallbackList = this.DB[j].getMailcapFallbackList(lowerCase);
                if (mailcapFallbackList != null) {
                    this.appendCmdsToList(mailcapFallbackList, list);
                }
            }
        }
        return (CommandInfo[])list.toArray(new CommandInfo[list.size()]);
    }
    
    private void appendCmdsToList(final Map map, final List list) {
        for (final String s : map.keySet()) {
            final Iterator iterator2 = ((List)map.get(s)).iterator();
            while (iterator2.hasNext()) {
                list.add(new CommandInfo(s, iterator2.next()));
            }
        }
    }
    
    @Override
    public synchronized CommandInfo getCommand(String lowerCase, final String s) {
        if (lowerCase != null) {
            lowerCase = lowerCase.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.DB.length; ++i) {
            if (this.DB[i] != null) {
                final Map mailcapList = this.DB[i].getMailcapList(lowerCase);
                if (mailcapList != null) {
                    final List<String> list = mailcapList.get(s);
                    if (list != null) {
                        final String s2 = list.get(0);
                        if (s2 != null) {
                            return new CommandInfo(s, s2);
                        }
                    }
                }
            }
        }
        for (int j = 0; j < this.DB.length; ++j) {
            if (this.DB[j] != null) {
                final Map mailcapFallbackList = this.DB[j].getMailcapFallbackList(lowerCase);
                if (mailcapFallbackList != null) {
                    final List<String> list2 = mailcapFallbackList.get(s);
                    if (list2 != null) {
                        final String s3 = list2.get(0);
                        if (s3 != null) {
                            return new CommandInfo(s, s3);
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public synchronized void addMailcap(final String s) {
        LogSupport.log("MailcapCommandMap: add to PROG");
        if (this.DB[0] == null) {
            this.DB[0] = new MailcapFile();
        }
        this.DB[0].appendToMailcap(s);
    }
    
    @Override
    public synchronized DataContentHandler createDataContentHandler(String lowerCase) {
        if (LogSupport.isLoggable()) {
            LogSupport.log("MailcapCommandMap: createDataContentHandler for " + lowerCase);
        }
        if (lowerCase != null) {
            lowerCase = lowerCase.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.DB.length; ++i) {
            if (this.DB[i] != null) {
                if (LogSupport.isLoggable()) {
                    LogSupport.log("  search DB #" + i);
                }
                final Map mailcapList = this.DB[i].getMailcapList(lowerCase);
                if (mailcapList != null) {
                    final List<String> list = mailcapList.get("content-handler");
                    if (list != null) {
                        final DataContentHandler dataContentHandler = this.getDataContentHandler(list.get(0));
                        if (dataContentHandler != null) {
                            return dataContentHandler;
                        }
                    }
                }
            }
        }
        for (int j = 0; j < this.DB.length; ++j) {
            if (this.DB[j] != null) {
                if (LogSupport.isLoggable()) {
                    LogSupport.log("  search fallback DB #" + j);
                }
                final Map mailcapFallbackList = this.DB[j].getMailcapFallbackList(lowerCase);
                if (mailcapFallbackList != null) {
                    final List<String> list2 = mailcapFallbackList.get("content-handler");
                    if (list2 != null) {
                        final DataContentHandler dataContentHandler2 = this.getDataContentHandler(list2.get(0));
                        if (dataContentHandler2 != null) {
                            return dataContentHandler2;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private DataContentHandler getDataContentHandler(final String s) {
        if (LogSupport.isLoggable()) {
            LogSupport.log("    got content-handler");
        }
        if (LogSupport.isLoggable()) {
            LogSupport.log("      class " + s);
        }
        try {
            ClassLoader classLoader = SecuritySupport.getContextClassLoader();
            if (classLoader == null) {
                classLoader = this.getClass().getClassLoader();
            }
            Class<?> clazz;
            try {
                clazz = classLoader.loadClass(s);
            }
            catch (Exception ex4) {
                clazz = Class.forName(s);
            }
            if (clazz != null) {
                return (DataContentHandler)clazz.newInstance();
            }
        }
        catch (IllegalAccessException ex) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("Can't load DCH " + s, ex);
            }
        }
        catch (ClassNotFoundException ex2) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("Can't load DCH " + s, ex2);
            }
        }
        catch (InstantiationException ex3) {
            if (LogSupport.isLoggable()) {
                LogSupport.log("Can't load DCH " + s, ex3);
            }
        }
        return null;
    }
    
    @Override
    public synchronized String[] getMimeTypes() {
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < this.DB.length; ++i) {
            if (this.DB[i] != null) {
                final String[] mimeTypes = this.DB[i].getMimeTypes();
                if (mimeTypes != null) {
                    for (int j = 0; j < mimeTypes.length; ++j) {
                        if (!list.contains(mimeTypes[j])) {
                            list.add(mimeTypes[j]);
                        }
                    }
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }
    
    public synchronized String[] getNativeCommands(String lowerCase) {
        final ArrayList<String> list = new ArrayList<String>();
        if (lowerCase != null) {
            lowerCase = lowerCase.toLowerCase(Locale.ENGLISH);
        }
        for (int i = 0; i < this.DB.length; ++i) {
            if (this.DB[i] != null) {
                final String[] nativeCommands = this.DB[i].getNativeCommands(lowerCase);
                if (nativeCommands != null) {
                    for (int j = 0; j < nativeCommands.length; ++j) {
                        if (!list.contains(nativeCommands[j])) {
                            list.add(nativeCommands[j]);
                        }
                    }
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }
}
