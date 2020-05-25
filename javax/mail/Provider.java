package javax.mail;

public class Provider
{
    private Type type;
    private String protocol;
    private String className;
    private String vendor;
    private String version;
    
    public Provider(final Type type, final String protocol, final String classname, final String vendor, final String version) {
        this.type = type;
        this.protocol = protocol;
        this.className = classname;
        this.vendor = vendor;
        this.version = version;
    }
    
    public Type getType() {
        return this.type;
    }
    
    public String getProtocol() {
        return this.protocol;
    }
    
    public String getClassName() {
        return this.className;
    }
    
    public String getVendor() {
        return this.vendor;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public String toString() {
        String s = "javax.mail.Provider[" + this.type + "," + this.protocol + "," + this.className;
        if (this.vendor != null) {
            s = s + "," + this.vendor;
        }
        if (this.version != null) {
            s = s + "," + this.version;
        }
        s += "]";
        return s;
    }
    
    public static class Type
    {
        public static final Type STORE;
        public static final Type TRANSPORT;
        private String type;
        
        private Type(final String type) {
            this.type = type;
        }
        
        public String toString() {
            return this.type;
        }
        
        static {
            STORE = new Type("STORE");
            TRANSPORT = new Type("TRANSPORT");
        }
    }
}
