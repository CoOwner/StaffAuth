package com.sun.activation.registries;

import java.io.*;
import java.util.*;

public class MailcapFile
{
    private Map type_hash;
    private Map fallback_hash;
    private Map native_commands;
    private static boolean addReverse;
    
    public MailcapFile(final String s) throws IOException {
        this.type_hash = new HashMap();
        this.fallback_hash = new HashMap();
        this.native_commands = new HashMap();
        if (LogSupport.isLoggable()) {
            LogSupport.log("new MailcapFile: file " + s);
        }
        Reader reader = null;
        try {
            reader = new FileReader(s);
            this.parse(new BufferedReader(reader));
        }
        finally {
            if (reader != null) {
                try {
                    ((InputStreamReader)reader).close();
                }
                catch (IOException ex) {}
            }
        }
    }
    
    public MailcapFile(final InputStream inputStream) throws IOException {
        this.type_hash = new HashMap();
        this.fallback_hash = new HashMap();
        this.native_commands = new HashMap();
        if (LogSupport.isLoggable()) {
            LogSupport.log("new MailcapFile: InputStream");
        }
        this.parse(new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1")));
    }
    
    public MailcapFile() {
        this.type_hash = new HashMap();
        this.fallback_hash = new HashMap();
        this.native_commands = new HashMap();
        if (LogSupport.isLoggable()) {
            LogSupport.log("new MailcapFile: default");
        }
    }
    
    public Map getMailcapList(final String s) {
        Map mergeResults = this.type_hash.get(s);
        final int index = s.indexOf(47);
        if (!s.substring(index + 1).equals("*")) {
            final Map map = this.type_hash.get(s.substring(0, index + 1) + "*");
            if (map != null) {
                if (mergeResults != null) {
                    mergeResults = this.mergeResults(mergeResults, map);
                }
                else {
                    mergeResults = map;
                }
            }
        }
        return mergeResults;
    }
    
    public Map getMailcapFallbackList(final String s) {
        Map mergeResults = this.fallback_hash.get(s);
        final int index = s.indexOf(47);
        if (!s.substring(index + 1).equals("*")) {
            final Map map = this.fallback_hash.get(s.substring(0, index + 1) + "*");
            if (map != null) {
                if (mergeResults != null) {
                    mergeResults = this.mergeResults(mergeResults, map);
                }
                else {
                    mergeResults = map;
                }
            }
        }
        return mergeResults;
    }
    
    public String[] getMimeTypes() {
        final HashSet set = new HashSet(this.type_hash.keySet());
        set.addAll(this.fallback_hash.keySet());
        set.addAll(this.native_commands.keySet());
        return (String[])set.toArray(new String[set.size()]);
    }
    
    public String[] getNativeCommands(final String s) {
        String[] array = null;
        final List list = this.native_commands.get(s.toLowerCase(Locale.ENGLISH));
        if (list != null) {
            array = (String[])list.toArray(new String[list.size()]);
        }
        return array;
    }
    
    private Map mergeResults(final Map map, final Map map2) {
        final Iterator<String> iterator = map2.keySet().iterator();
        final HashMap<String, ArrayList<? extends E>> hashMap = new HashMap<String, ArrayList<? extends E>>(map);
        while (iterator.hasNext()) {
            final String s = iterator.next();
            final List<? extends E> list = hashMap.get(s);
            if (list == null) {
                hashMap.put(s, (ArrayList<? extends E>)map2.get(s));
            }
            else {
                final List list2 = (List)map2.get(s);
                final ArrayList list3 = new ArrayList<Object>(list);
                list3.addAll((Collection<?>)list2);
                hashMap.put(s, (ArrayList<? extends E>)list3);
            }
        }
        return hashMap;
    }
    
    public void appendToMailcap(final String s) {
        if (LogSupport.isLoggable()) {
            LogSupport.log("appendToMailcap: " + s);
        }
        try {
            this.parse(new StringReader(s));
        }
        catch (IOException ex) {}
    }
    
    private void parse(final Reader reader) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(reader);
        String s = null;
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            final String trim = line.trim();
            try {
                if (trim.charAt(0) == '#') {
                    continue;
                }
                if (trim.charAt(trim.length() - 1) == '\\') {
                    if (s != null) {
                        s += trim.substring(0, trim.length() - 1);
                    }
                    else {
                        s = trim.substring(0, trim.length() - 1);
                    }
                }
                else if (s != null) {
                    s += trim;
                    try {
                        this.parseLine(s);
                    }
                    catch (MailcapParseException ex) {}
                    s = null;
                }
                else {
                    try {
                        this.parseLine(trim);
                    }
                    catch (MailcapParseException ex2) {}
                }
            }
            catch (StringIndexOutOfBoundsException ex3) {}
        }
    }
    
    protected void parseLine(final String s) throws MailcapParseException, IOException {
        final MailcapTokenizer mailcapTokenizer = new MailcapTokenizer(s);
        mailcapTokenizer.setIsAutoquoting(false);
        if (LogSupport.isLoggable()) {
            LogSupport.log("parse: " + s);
        }
        final int nextToken = mailcapTokenizer.nextToken();
        if (nextToken != 2) {
            reportParseError(2, nextToken, mailcapTokenizer.getCurrentTokenValue());
        }
        final String lowerCase = mailcapTokenizer.getCurrentTokenValue().toLowerCase(Locale.ENGLISH);
        String lowerCase2 = "*";
        int n = mailcapTokenizer.nextToken();
        if (n != 47 && n != 59) {
            reportParseError(47, 59, n, mailcapTokenizer.getCurrentTokenValue());
        }
        if (n == 47) {
            final int nextToken2 = mailcapTokenizer.nextToken();
            if (nextToken2 != 2) {
                reportParseError(2, nextToken2, mailcapTokenizer.getCurrentTokenValue());
            }
            lowerCase2 = mailcapTokenizer.getCurrentTokenValue().toLowerCase(Locale.ENGLISH);
            n = mailcapTokenizer.nextToken();
        }
        final String string = lowerCase + "/" + lowerCase2;
        if (LogSupport.isLoggable()) {
            LogSupport.log("  Type: " + string);
        }
        final LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<String, Object>();
        if (n != 59) {
            reportParseError(59, n, mailcapTokenizer.getCurrentTokenValue());
        }
        mailcapTokenizer.setIsAutoquoting(true);
        int n2 = mailcapTokenizer.nextToken();
        mailcapTokenizer.setIsAutoquoting(false);
        if (n2 != 2 && n2 != 59) {
            reportParseError(2, 59, n2, mailcapTokenizer.getCurrentTokenValue());
        }
        if (n2 == 2) {
            final List<String> list = this.native_commands.get(string);
            if (list == null) {
                final ArrayList<String> list2 = new ArrayList<String>();
                list2.add(s);
                this.native_commands.put(string, list2);
            }
            else {
                list.add(s);
            }
        }
        if (n2 != 59) {
            n2 = mailcapTokenizer.nextToken();
        }
        if (n2 == 59) {
            boolean b = false;
            int i;
            do {
                final int nextToken3 = mailcapTokenizer.nextToken();
                if (nextToken3 != 2) {
                    reportParseError(2, nextToken3, mailcapTokenizer.getCurrentTokenValue());
                }
                final String lowerCase3 = mailcapTokenizer.getCurrentTokenValue().toLowerCase(Locale.ENGLISH);
                i = mailcapTokenizer.nextToken();
                if (i != 61 && i != 59 && i != 5) {
                    reportParseError(61, 59, 5, i, mailcapTokenizer.getCurrentTokenValue());
                }
                if (i == 61) {
                    mailcapTokenizer.setIsAutoquoting(true);
                    final int nextToken4 = mailcapTokenizer.nextToken();
                    mailcapTokenizer.setIsAutoquoting(false);
                    if (nextToken4 != 2) {
                        reportParseError(2, nextToken4, mailcapTokenizer.getCurrentTokenValue());
                    }
                    final String currentTokenValue = mailcapTokenizer.getCurrentTokenValue();
                    if (lowerCase3.startsWith("x-java-")) {
                        final String substring = lowerCase3.substring(7);
                        if (substring.equals("fallback-entry") && currentTokenValue.equalsIgnoreCase("true")) {
                            b = true;
                        }
                        else {
                            if (LogSupport.isLoggable()) {
                                LogSupport.log("    Command: " + substring + ", Class: " + currentTokenValue);
                            }
                            List<?> list3 = linkedHashMap.get(substring);
                            if (list3 == null) {
                                list3 = new ArrayList<Object>();
                                linkedHashMap.put(substring, list3);
                            }
                            if (MailcapFile.addReverse) {
                                list3.add(0, currentTokenValue);
                            }
                            else {
                                list3.add(currentTokenValue);
                            }
                        }
                    }
                    i = mailcapTokenizer.nextToken();
                }
            } while (i == 59);
            final Map map = b ? this.fallback_hash : this.type_hash;
            final LinkedHashMap<String, Object> linkedHashMap2 = map.get(string);
            if (linkedHashMap2 == null) {
                map.put(string, linkedHashMap);
            }
            else {
                if (LogSupport.isLoggable()) {
                    LogSupport.log("Merging commands for type " + string);
                }
                for (final String s2 : linkedHashMap2.keySet()) {
                    final List<String> list4 = linkedHashMap2.get(s2);
                    final List<?> list5 = linkedHashMap.get(s2);
                    if (list5 == null) {
                        continue;
                    }
                    for (final String s3 : list5) {
                        if (!list4.contains(s3)) {
                            if (MailcapFile.addReverse) {
                                list4.add(0, s3);
                            }
                            else {
                                list4.add(s3);
                            }
                        }
                    }
                }
                for (final String s4 : linkedHashMap.keySet()) {
                    if (linkedHashMap2.containsKey(s4)) {
                        continue;
                    }
                    linkedHashMap2.put(s4, linkedHashMap.get(s4));
                }
            }
        }
        else if (n2 != 5) {
            reportParseError(5, 59, n2, mailcapTokenizer.getCurrentTokenValue());
        }
    }
    
    protected static void reportParseError(final int n, final int n2, final String s) throws MailcapParseException {
        throw new MailcapParseException("Encountered a " + MailcapTokenizer.nameForToken(n2) + " token (" + s + ") while expecting a " + MailcapTokenizer.nameForToken(n) + " token.");
    }
    
    protected static void reportParseError(final int n, final int n2, final int n3, final String s) throws MailcapParseException {
        throw new MailcapParseException("Encountered a " + MailcapTokenizer.nameForToken(n3) + " token (" + s + ") while expecting a " + MailcapTokenizer.nameForToken(n) + " or a " + MailcapTokenizer.nameForToken(n2) + " token.");
    }
    
    protected static void reportParseError(final int n, final int n2, final int n3, final int n4, final String s) throws MailcapParseException {
        if (LogSupport.isLoggable()) {
            LogSupport.log("PARSE ERROR: Encountered a " + MailcapTokenizer.nameForToken(n4) + " token (" + s + ") while expecting a " + MailcapTokenizer.nameForToken(n) + ", a " + MailcapTokenizer.nameForToken(n2) + ", or a " + MailcapTokenizer.nameForToken(n3) + " token.");
        }
        throw new MailcapParseException("Encountered a " + MailcapTokenizer.nameForToken(n4) + " token (" + s + ") while expecting a " + MailcapTokenizer.nameForToken(n) + ", a " + MailcapTokenizer.nameForToken(n2) + ", or a " + MailcapTokenizer.nameForToken(n3) + " token.");
    }
    
    static {
        MailcapFile.addReverse = false;
        try {
            MailcapFile.addReverse = Boolean.getBoolean("javax.activation.addreverse");
        }
        catch (Throwable t) {}
    }
}
