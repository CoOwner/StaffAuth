package javax.mail;

import java.util.*;

public class FetchProfile
{
    private Vector specials;
    private Vector headers;
    
    public FetchProfile() {
        this.specials = null;
        this.headers = null;
    }
    
    public void add(final Item item) {
        if (this.specials == null) {
            this.specials = new Vector();
        }
        this.specials.addElement(item);
    }
    
    public void add(final String headerName) {
        if (this.headers == null) {
            this.headers = new Vector();
        }
        this.headers.addElement(headerName);
    }
    
    public boolean contains(final Item item) {
        return this.specials != null && this.specials.contains(item);
    }
    
    public boolean contains(final String headerName) {
        return this.headers != null && this.headers.contains(headerName);
    }
    
    public Item[] getItems() {
        if (this.specials == null) {
            return new Item[0];
        }
        final Item[] s = new Item[this.specials.size()];
        this.specials.copyInto(s);
        return s;
    }
    
    public String[] getHeaderNames() {
        if (this.headers == null) {
            return new String[0];
        }
        final String[] s = new String[this.headers.size()];
        this.headers.copyInto(s);
        return s;
    }
    
    public static class Item
    {
        public static final Item ENVELOPE;
        public static final Item CONTENT_INFO;
        public static final Item FLAGS;
        private String name;
        
        protected Item(final String name) {
            this.name = name;
        }
        
        static {
            ENVELOPE = new Item("ENVELOPE");
            CONTENT_INFO = new Item("CONTENT_INFO");
            FLAGS = new Item("FLAGS");
        }
    }
}
