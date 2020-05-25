package com.mongodb;

import java.util.*;

public enum AuthenticationMechanism
{
    GSSAPI("GSSAPI"), 
    PLAIN("PLAIN"), 
    MONGODB_X509("MONGODB-X509"), 
    MONGODB_CR("MONGODB-CR"), 
    SCRAM_SHA_1("SCRAM-SHA-1");
    
    private static final Map<String, AuthenticationMechanism> AUTH_MAP;
    private final String mechanismName;
    
    private AuthenticationMechanism(final String mechanismName) {
        this.mechanismName = mechanismName;
    }
    
    public String getMechanismName() {
        return this.mechanismName;
    }
    
    @Override
    public String toString() {
        return this.mechanismName;
    }
    
    public static AuthenticationMechanism fromMechanismName(final String mechanismName) {
        final AuthenticationMechanism mechanism = AuthenticationMechanism.AUTH_MAP.get(mechanismName);
        if (mechanism == null) {
            throw new IllegalArgumentException("Unsupported authMechanism: " + mechanismName);
        }
        return mechanism;
    }
    
    static {
        AUTH_MAP = new HashMap<String, AuthenticationMechanism>();
        for (final AuthenticationMechanism value : values()) {
            AuthenticationMechanism.AUTH_MAP.put(value.getMechanismName(), value);
        }
    }
}
