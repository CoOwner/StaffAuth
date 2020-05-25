package javax.mail;

public class Header
{
    protected String name;
    protected String value;
    
    public Header(final String name, final String value) {
        this.name = name;
        this.value = value;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getValue() {
        return this.value;
    }
}
