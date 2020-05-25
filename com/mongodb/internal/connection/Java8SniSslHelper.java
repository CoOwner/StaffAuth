package com.mongodb.internal.connection;

import com.mongodb.*;
import java.util.*;
import javax.net.ssl.*;

final class Java8SniSslHelper implements SniSslHelper
{
    @Override
    public void enableSni(final ServerAddress address, final SSLParameters sslParameters) {
        try {
            final SNIServerName sniHostName = new SNIHostName(address.getHost());
            sslParameters.setServerNames(Collections.singletonList(sniHostName));
        }
        catch (IllegalArgumentException ex) {}
    }
    
    static {
        try {
            Class.forName("javax.net.ssl.SNIHostName");
        }
        catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
