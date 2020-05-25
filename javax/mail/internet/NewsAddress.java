package javax.mail.internet;

import javax.mail.*;
import java.util.*;

public class NewsAddress extends Address
{
    protected String newsgroup;
    protected String host;
    private static final long serialVersionUID = -4203797299824684143L;
    
    public NewsAddress() {
    }
    
    public NewsAddress(final String newsgroup) {
        this(newsgroup, null);
    }
    
    public NewsAddress(final String newsgroup, final String host) {
        this.newsgroup = newsgroup;
        this.host = host;
    }
    
    public String getType() {
        return "news";
    }
    
    public void setNewsgroup(final String newsgroup) {
        this.newsgroup = newsgroup;
    }
    
    public String getNewsgroup() {
        return this.newsgroup;
    }
    
    public void setHost(final String host) {
        this.host = host;
    }
    
    public String getHost() {
        return this.host;
    }
    
    public String toString() {
        return this.newsgroup;
    }
    
    public boolean equals(final Object a) {
        if (!(a instanceof NewsAddress)) {
            return false;
        }
        final NewsAddress s = (NewsAddress)a;
        return this.newsgroup.equals(s.newsgroup) && ((this.host == null && s.host == null) || (this.host != null && s.host != null && this.host.equalsIgnoreCase(s.host)));
    }
    
    public int hashCode() {
        int hash = 0;
        if (this.newsgroup != null) {
            hash += this.newsgroup.hashCode();
        }
        if (this.host != null) {
            hash += this.host.toLowerCase().hashCode();
        }
        return hash;
    }
    
    public static String toString(final Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        final StringBuffer s = new StringBuffer(((NewsAddress)addresses[0]).toString());
        for (int i = 1; i < addresses.length; ++i) {
            s.append(",").append(((NewsAddress)addresses[i]).toString());
        }
        return s.toString();
    }
    
    public static NewsAddress[] parse(final String newsgroups) throws AddressException {
        final StringTokenizer st = new StringTokenizer(newsgroups, ",");
        final Vector nglist = new Vector();
        while (st.hasMoreTokens()) {
            final String ng = st.nextToken();
            nglist.addElement(new NewsAddress(ng));
        }
        final int size = nglist.size();
        final NewsAddress[] na = new NewsAddress[size];
        if (size > 0) {
            nglist.copyInto(na);
        }
        return na;
    }
}
