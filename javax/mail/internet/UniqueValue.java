package javax.mail.internet;

import javax.mail.*;

class UniqueValue
{
    private static int part;
    private static int id;
    
    public static String getUniqueBoundaryValue() {
        final StringBuffer s = new StringBuffer();
        s.append("----=_Part_").append(UniqueValue.part++).append("_").append(s.hashCode()).append('.').append(System.currentTimeMillis());
        return s.toString();
    }
    
    public static String getUniqueMessageIDValue(final Session ssn) {
        String suffix = null;
        final InternetAddress addr = InternetAddress.getLocalAddress(ssn);
        if (addr != null) {
            suffix = addr.getAddress();
        }
        else {
            suffix = "javamailuser@localhost";
        }
        final StringBuffer s = new StringBuffer();
        s.append(s.hashCode()).append('.').append(UniqueValue.id++).append(System.currentTimeMillis()).append('.').append("JavaMail.").append(suffix);
        return s.toString();
    }
    
    static {
        UniqueValue.part = 0;
        UniqueValue.id = 0;
    }
}
