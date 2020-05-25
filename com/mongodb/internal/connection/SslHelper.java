package com.mongodb.internal.connection;

import javax.net.ssl.*;
import com.mongodb.*;

public final class SslHelper
{
    private static final SniSslHelper SNI_SSL_HELPER;
    
    public static void enableHostNameVerification(final SSLParameters sslParameters) {
        sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
    }
    
    public static void enableSni(final ServerAddress address, final SSLParameters sslParameters) {
        if (SslHelper.SNI_SSL_HELPER != null) {
            SslHelper.SNI_SSL_HELPER.enableSni(address, sslParameters);
        }
    }
    
    private SslHelper() {
    }
    
    static {
        SniSslHelper sniSslHelper;
        try {
            sniSslHelper = (SniSslHelper)Class.forName("com.mongodb.internal.connection.Java8SniSslHelper").newInstance();
        }
        catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
        catch (InstantiationException e2) {
            throw new ExceptionInInitializerError(e2);
        }
        catch (IllegalAccessException e3) {
            throw new ExceptionInInitializerError(e3);
        }
        catch (LinkageError t) {
            sniSslHelper = null;
        }
        SNI_SSL_HELPER = sniSslHelper;
    }
}
