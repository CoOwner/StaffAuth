package com.sun.mail.imap;

import java.util.*;

public class Rights implements Cloneable
{
    private boolean[] rights;
    
    public Rights() {
        this.rights = new boolean[128];
    }
    
    public Rights(final Rights rights) {
        this.rights = new boolean[128];
        System.arraycopy(rights.rights, 0, this.rights, 0, this.rights.length);
    }
    
    public Rights(final String rights) {
        this.rights = new boolean[128];
        for (int i = 0; i < rights.length(); ++i) {
            this.add(Right.getInstance(rights.charAt(i)));
        }
    }
    
    public Rights(final Right right) {
        (this.rights = new boolean[128])[right.right] = true;
    }
    
    public void add(final Right right) {
        this.rights[right.right] = true;
    }
    
    public void add(final Rights rights) {
        for (int i = 0; i < rights.rights.length; ++i) {
            if (rights.rights[i]) {
                this.rights[i] = true;
            }
        }
    }
    
    public void remove(final Right right) {
        this.rights[right.right] = false;
    }
    
    public void remove(final Rights rights) {
        for (int i = 0; i < rights.rights.length; ++i) {
            if (rights.rights[i]) {
                this.rights[i] = false;
            }
        }
    }
    
    public boolean contains(final Right right) {
        return this.rights[right.right];
    }
    
    public boolean contains(final Rights rights) {
        for (int i = 0; i < rights.rights.length; ++i) {
            if (rights.rights[i] && !this.rights[i]) {
                return false;
            }
        }
        return true;
    }
    
    public boolean equals(final Object obj) {
        if (!(obj instanceof Rights)) {
            return false;
        }
        final Rights rights = (Rights)obj;
        for (int i = 0; i < rights.rights.length; ++i) {
            if (rights.rights[i] != this.rights[i]) {
                return false;
            }
        }
        return true;
    }
    
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < this.rights.length; ++i) {
            if (this.rights[i]) {
                ++hash;
            }
        }
        return hash;
    }
    
    public Right[] getRights() {
        final Vector v = new Vector();
        for (int i = 0; i < this.rights.length; ++i) {
            if (this.rights[i]) {
                v.addElement(Right.getInstance((char)i));
            }
        }
        final Right[] rights = new Right[v.size()];
        v.copyInto(rights);
        return rights;
    }
    
    public Object clone() {
        return new Rights(this);
    }
    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.rights.length; ++i) {
            if (this.rights[i]) {
                sb.append((char)i);
            }
        }
        return sb.toString();
    }
    
    public static final class Right
    {
        private static Right[] cache;
        public static final Right LOOKUP;
        public static final Right READ;
        public static final Right KEEP_SEEN;
        public static final Right WRITE;
        public static final Right INSERT;
        public static final Right POST;
        public static final Right CREATE;
        public static final Right DELETE;
        public static final Right ADMINISTER;
        char right;
        
        private Right(final char right) {
            if (right >= '\u0080') {
                throw new IllegalArgumentException("Right must be ASCII");
            }
            this.right = right;
        }
        
        public static synchronized Right getInstance(final char right) {
            if (right >= '\u0080') {
                throw new IllegalArgumentException("Right must be ASCII");
            }
            if (Right.cache[right] == null) {
                Right.cache[right] = new Right(right);
            }
            return Right.cache[right];
        }
        
        public String toString() {
            return String.valueOf(this.right);
        }
        
        static {
            Right.cache = new Right[128];
            LOOKUP = getInstance('l');
            READ = getInstance('r');
            KEEP_SEEN = getInstance('s');
            WRITE = getInstance('w');
            INSERT = getInstance('i');
            POST = getInstance('p');
            CREATE = getInstance('c');
            DELETE = getInstance('d');
            ADMINISTER = getInstance('a');
        }
    }
}
