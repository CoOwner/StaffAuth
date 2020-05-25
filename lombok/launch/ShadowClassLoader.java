package lombok.launch;

import java.util.jar.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.*;

class ShadowClassLoader extends ClassLoader
{
    private static final String SELF_NAME = "lombok/launch/ShadowClassLoader.class";
    private static final ConcurrentMap<String, Class<?>> highlanderMap;
    private final String SELF_BASE;
    private final File SELF_BASE_FILE;
    private final int SELF_BASE_LENGTH;
    private final List<File> override;
    private final String sclSuffix;
    private final List<String> parentExclusion;
    private final List<String> highlanders;
    private final Map<String, Object> mapJarPathToTracker;
    private static final Map<Object, String> mapTrackerToJarPath;
    private static final Map<Object, Set<String>> mapTrackerToJarContents;
    
    ShadowClassLoader(final ClassLoader source, final String sclSuffix, final String selfBase, final List<String> parentExclusion, final List<String> highlanders) {
        super(source);
        this.override = new ArrayList<File>();
        this.parentExclusion = new ArrayList<String>();
        this.highlanders = new ArrayList<String>();
        this.mapJarPathToTracker = new HashMap<String, Object>();
        this.sclSuffix = sclSuffix;
        if (parentExclusion != null) {
            for (String pe : parentExclusion) {
                pe = pe.replace(".", "/");
                if (!pe.endsWith("/")) {
                    pe += "/";
                }
                this.parentExclusion.add(pe);
            }
        }
        if (highlanders != null) {
            for (final String hl : highlanders) {
                this.highlanders.add(hl);
            }
        }
        if (selfBase != null) {
            this.SELF_BASE = selfBase;
            this.SELF_BASE_LENGTH = selfBase.length();
        }
        else {
            final URL sclClassUrl = ShadowClassLoader.class.getResource("ShadowClassLoader.class");
            final String sclClassStr = (sclClassUrl == null) ? null : sclClassUrl.toString();
            if (sclClassStr == null || !sclClassStr.endsWith("lombok/launch/ShadowClassLoader.class")) {
                final ClassLoader cl = ShadowClassLoader.class.getClassLoader();
                throw new RuntimeException("ShadowLoader can't find itself. SCL loader type: " + ((cl == null) ? "*NULL*" : cl.getClass().toString()));
            }
            this.SELF_BASE_LENGTH = sclClassStr.length() - "lombok/launch/ShadowClassLoader.class".length();
            String decoded;
            try {
                decoded = URLDecoder.decode(sclClassStr.substring(0, this.SELF_BASE_LENGTH), "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                throw new InternalError("UTF-8 not available");
            }
            this.SELF_BASE = decoded;
        }
        if (this.SELF_BASE.startsWith("jar:file:") && this.SELF_BASE.endsWith("!/")) {
            this.SELF_BASE_FILE = new File(this.SELF_BASE.substring(9, this.SELF_BASE.length() - 2));
        }
        else if (this.SELF_BASE.startsWith("file:")) {
            this.SELF_BASE_FILE = new File(this.SELF_BASE.substring(5));
        }
        else {
            this.SELF_BASE_FILE = new File(this.SELF_BASE);
        }
        final String scl = System.getProperty("shadow.override." + sclSuffix);
        if (scl != null && !scl.isEmpty()) {
            for (final String part : scl.split("\\s*" + ((File.pathSeparatorChar == ';') ? ";" : ":") + "\\s*")) {
                if (part.endsWith("/*") || part.endsWith(File.separator + "*")) {
                    this.addOverrideJarDir(part.substring(0, part.length() - 2));
                }
                else {
                    this.addOverrideClasspathEntry(part);
                }
            }
        }
    }
    
    private Set<String> getOrMakeJarListing(final String absolutePathToJar) {
        synchronized (ShadowClassLoader.mapTrackerToJarPath) {
            final Object ourTracker = this.mapJarPathToTracker.get(absolutePathToJar);
            if (ourTracker != null) {
                return ShadowClassLoader.mapTrackerToJarContents.get(ourTracker);
            }
            for (final Map.Entry<Object, String> entry : ShadowClassLoader.mapTrackerToJarPath.entrySet()) {
                if (entry.getValue().equals(absolutePathToJar)) {
                    final Object otherTracker = entry.getKey();
                    this.mapJarPathToTracker.put(absolutePathToJar, otherTracker);
                    return ShadowClassLoader.mapTrackerToJarContents.get(otherTracker);
                }
            }
            final Object newTracker = new Object();
            final Set<String> jarMembers = this.getJarMemberSet(absolutePathToJar);
            ShadowClassLoader.mapTrackerToJarContents.put(newTracker, jarMembers);
            ShadowClassLoader.mapTrackerToJarPath.put(newTracker, absolutePathToJar);
            this.mapJarPathToTracker.put(absolutePathToJar, newTracker);
            return jarMembers;
        }
    }
    
    private Set<String> getJarMemberSet(final String absolutePathToJar) {
        try {
            final int shiftBits = 1;
            final JarFile jar = new JarFile(absolutePathToJar);
            int jarSizePower2 = Integer.highestOneBit(jar.size());
            if (jarSizePower2 != jar.size()) {
                jarSizePower2 <<= 1;
            }
            if (jarSizePower2 == 0) {
                jarSizePower2 = 1;
            }
            final Set<String> jarMembers = new HashSet<String>(jarSizePower2 >> shiftBits, (float)(1 << shiftBits));
            try {
                final Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry jarEntry = entries.nextElement();
                    if (jarEntry.isDirectory()) {
                        continue;
                    }
                    jarMembers.add(jarEntry.getName());
                }
            }
            catch (Exception ex) {}
            finally {
                jar.close();
            }
            return jarMembers;
        }
        catch (Exception newJarFileException) {
            return Collections.emptySet();
        }
    }
    
    private URL getResourceFromLocation(final String name, final String altName, final File location) {
        if (location.isDirectory()) {
            try {
                if (altName != null) {
                    final File f = new File(location, altName);
                    if (f.isFile() && f.canRead()) {
                        return f.toURI().toURL();
                    }
                }
                final File f = new File(location, name);
                if (f.isFile() && f.canRead()) {
                    return f.toURI().toURL();
                }
                return null;
            }
            catch (MalformedURLException e) {
                return null;
            }
        }
        if (!location.isFile() || !location.canRead()) {
            return null;
        }
        File absoluteFile;
        try {
            absoluteFile = location.getCanonicalFile();
        }
        catch (Exception e2) {
            absoluteFile = location.getAbsoluteFile();
        }
        final Set<String> jarContents = this.getOrMakeJarListing(absoluteFile.getAbsolutePath());
        final String absoluteUri = absoluteFile.toURI().toString();
        try {
            if (jarContents.contains(altName)) {
                return new URI("jar:" + absoluteUri + "!/" + altName).toURL();
            }
        }
        catch (Exception ex) {}
        try {
            if (jarContents.contains(name)) {
                return new URI("jar:" + absoluteUri + "!/" + name).toURL();
            }
        }
        catch (Exception ex2) {}
        return null;
    }
    
    private boolean inOwnBase(final URL item, final String name) {
        if (item == null) {
            return false;
        }
        final String itemString = item.toString();
        return itemString.length() == this.SELF_BASE_LENGTH + name.length() && this.SELF_BASE.regionMatches(0, itemString, 0, this.SELF_BASE_LENGTH);
    }
    
    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        String altName = null;
        if (name.endsWith(".class")) {
            altName = name.substring(0, name.length() - 6) + ".SCL." + this.sclSuffix;
        }
        final Vector<URL> vector = new Vector<URL>();
        for (final File ce : this.override) {
            final URL url = this.getResourceFromLocation(name, altName, ce);
            if (url != null) {
                vector.add(url);
            }
        }
        if (this.override.isEmpty()) {
            final URL fromSelf = this.getResourceFromLocation(name, altName, this.SELF_BASE_FILE);
            if (fromSelf != null) {
                vector.add(fromSelf);
            }
        }
        final Enumeration<URL> sec = super.getResources(name);
        while (sec.hasMoreElements()) {
            final URL item = sec.nextElement();
            if (!this.inOwnBase(item, name)) {
                vector.add(item);
            }
        }
        if (altName != null) {
            final Enumeration<URL> tern = super.getResources(altName);
            while (tern.hasMoreElements()) {
                final URL item2 = tern.nextElement();
                if (!this.inOwnBase(item2, altName)) {
                    vector.add(item2);
                }
            }
        }
        return vector.elements();
    }
    
    @Override
    public URL getResource(final String name) {
        return this.getResource_(name, false);
    }
    
    private URL getResource_(final String name, final boolean noSuper) {
        String altName = null;
        if (name.endsWith(".class")) {
            altName = name.substring(0, name.length() - 6) + ".SCL." + this.sclSuffix;
        }
        for (final File ce : this.override) {
            final URL url = this.getResourceFromLocation(name, altName, ce);
            if (url != null) {
                return url;
            }
        }
        if (!this.override.isEmpty()) {
            if (noSuper) {
                return null;
            }
            if (altName != null) {
                try {
                    final URL res = this.getResourceSkippingSelf(altName);
                    if (res != null) {
                        return res;
                    }
                }
                catch (IOException ex) {}
            }
            try {
                return this.getResourceSkippingSelf(name);
            }
            catch (IOException e) {
                return null;
            }
        }
        final URL url2 = this.getResourceFromLocation(name, altName, this.SELF_BASE_FILE);
        if (url2 != null) {
            return url2;
        }
        if (altName != null) {
            final URL res2 = super.getResource(altName);
            if (res2 != null && (!noSuper || this.inOwnBase(res2, altName))) {
                return res2;
            }
        }
        final URL res2 = super.getResource(name);
        if (res2 != null && (!noSuper || this.inOwnBase(res2, name))) {
            return res2;
        }
        return null;
    }
    
    private boolean exclusionListMatch(final String name) {
        for (final String pe : this.parentExclusion) {
            if (name.startsWith(pe)) {
                return true;
            }
        }
        return false;
    }
    
    private URL getResourceSkippingSelf(final String name) throws IOException {
        URL candidate = super.getResource(name);
        if (candidate == null) {
            return null;
        }
        if (!this.inOwnBase(candidate, name)) {
            return candidate;
        }
        final Enumeration<URL> en = super.getResources(name);
        while (en.hasMoreElements()) {
            candidate = en.nextElement();
            if (!this.inOwnBase(candidate, name)) {
                return candidate;
            }
        }
        return null;
    }
    
    public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        final Class<?> alreadyLoaded = this.findLoadedClass(name);
        if (alreadyLoaded != null) {
            return alreadyLoaded;
        }
        if (this.highlanders.contains(name)) {
            final Class<?> c = ShadowClassLoader.highlanderMap.get(name);
            if (c != null) {
                return c;
            }
        }
        final String fileNameOfClass = name.replace(".", "/") + ".class";
        final URL res = this.getResource_(fileNameOfClass, true);
        if (res != null) {
            int p = 0;
            byte[] b;
            try {
                final InputStream in = res.openStream();
                try {
                    b = new byte[65536];
                    while (true) {
                        final int r = in.read(b, p, b.length - p);
                        if (r == -1) {
                            break;
                        }
                        p += r;
                        if (p != b.length) {
                            continue;
                        }
                        final byte[] nb = new byte[b.length * 2];
                        System.arraycopy(b, 0, nb, 0, p);
                        b = nb;
                    }
                }
                finally {
                    in.close();
                }
            }
            catch (IOException e) {
                throw new ClassNotFoundException("I/O exception reading class " + name, e);
            }
            Class<?> c2;
            try {
                c2 = this.defineClass(name, b, 0, p);
            }
            catch (LinkageError e2) {
                if (this.highlanders.contains(name)) {
                    final Class<?> alreadyDefined = ShadowClassLoader.highlanderMap.get(name);
                    if (alreadyDefined != null) {
                        return alreadyDefined;
                    }
                }
                try {
                    c2 = this.findLoadedClass(name);
                }
                catch (LinkageError e3) {
                    throw e2;
                }
                if (c2 == null) {
                    throw e2;
                }
            }
            if (this.highlanders.contains(name)) {
                final Class<?> alreadyDefined2 = ShadowClassLoader.highlanderMap.putIfAbsent(name, c2);
                if (alreadyDefined2 != null) {
                    c2 = alreadyDefined2;
                }
            }
            if (resolve) {
                this.resolveClass(c2);
            }
            return c2;
        }
        if (!this.exclusionListMatch(fileNameOfClass)) {
            return super.loadClass(name, resolve);
        }
        throw new ClassNotFoundException(name);
    }
    
    public void addOverrideJarDir(final String dir) {
        final File f = new File(dir);
        for (final File j : f.listFiles()) {
            if (j.getName().toLowerCase().endsWith(".jar") && j.canRead() && j.isFile()) {
                this.override.add(j);
            }
        }
    }
    
    public void addOverrideClasspathEntry(final String entry) {
        this.override.add(new File(entry));
    }
    
    static {
        highlanderMap = new ConcurrentHashMap<String, Class<?>>();
        mapTrackerToJarPath = new WeakHashMap<Object, String>();
        mapTrackerToJarContents = new WeakHashMap<Object, Set<String>>();
    }
}
