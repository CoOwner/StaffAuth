package com.mongodb;

import com.mongodb.annotations.*;
import com.mongodb.assertions.*;
import java.util.*;

@Immutable
public final class MongoCredential
{
    private final AuthenticationMechanism mechanism;
    private final String userName;
    private final String source;
    private final char[] password;
    private final Map<String, Object> mechanismProperties;
    public static final String MONGODB_CR_MECHANISM;
    public static final String GSSAPI_MECHANISM;
    public static final String PLAIN_MECHANISM;
    public static final String MONGODB_X509_MECHANISM;
    public static final String SCRAM_SHA_1_MECHANISM;
    public static final String SERVICE_NAME_KEY = "SERVICE_NAME";
    public static final String CANONICALIZE_HOST_NAME_KEY = "CANONICALIZE_HOST_NAME";
    public static final String JAVA_SASL_CLIENT_PROPERTIES_KEY = "JAVA_SASL_CLIENT_PROPERTIES";
    public static final String JAVA_SUBJECT_KEY = "JAVA_SUBJECT";
    
    public static MongoCredential createCredential(final String userName, final String database, final char[] password) {
        return new MongoCredential(null, userName, database, password);
    }
    
    public static MongoCredential createScramSha1Credential(final String userName, final String source, final char[] password) {
        return new MongoCredential(AuthenticationMechanism.SCRAM_SHA_1, userName, source, password);
    }
    
    public static MongoCredential createMongoCRCredential(final String userName, final String database, final char[] password) {
        return new MongoCredential(AuthenticationMechanism.MONGODB_CR, userName, database, password);
    }
    
    public static MongoCredential createMongoX509Credential(final String userName) {
        return new MongoCredential(AuthenticationMechanism.MONGODB_X509, userName, "$external", null);
    }
    
    public static MongoCredential createMongoX509Credential() {
        return new MongoCredential(AuthenticationMechanism.MONGODB_X509, null, "$external", null);
    }
    
    public static MongoCredential createPlainCredential(final String userName, final String source, final char[] password) {
        return new MongoCredential(AuthenticationMechanism.PLAIN, userName, source, password);
    }
    
    public static MongoCredential createGSSAPICredential(final String userName) {
        return new MongoCredential(AuthenticationMechanism.GSSAPI, userName, "$external", null);
    }
    
    public <T> MongoCredential withMechanismProperty(final String key, final T value) {
        return new MongoCredential(this, key, (T)value);
    }
    
    MongoCredential(final AuthenticationMechanism mechanism, final String userName, final String source, final char[] password) {
        if (mechanism != AuthenticationMechanism.MONGODB_X509 && userName == null) {
            throw new IllegalArgumentException("username can not be null");
        }
        if (mechanism == null && password == null) {
            throw new IllegalArgumentException("Password can not be null when the authentication mechanism is unspecified");
        }
        if ((mechanism == AuthenticationMechanism.PLAIN || mechanism == AuthenticationMechanism.MONGODB_CR || mechanism == AuthenticationMechanism.SCRAM_SHA_1) && password == null) {
            throw new IllegalArgumentException("Password can not be null for " + mechanism + " mechanism");
        }
        if ((mechanism == AuthenticationMechanism.GSSAPI || mechanism == AuthenticationMechanism.MONGODB_X509) && password != null) {
            throw new IllegalArgumentException("Password must be null for the " + mechanism + " mechanism");
        }
        this.mechanism = mechanism;
        this.userName = userName;
        this.source = Assertions.notNull("source", source);
        this.password = (char[])((password != null) ? ((char[])password.clone()) : null);
        this.mechanismProperties = Collections.emptyMap();
    }
    
     <T> MongoCredential(final MongoCredential from, final String mechanismPropertyKey, final T mechanismPropertyValue) {
        Assertions.notNull("mechanismPropertyKey", mechanismPropertyKey);
        this.mechanism = from.mechanism;
        this.userName = from.userName;
        this.source = from.source;
        this.password = from.password;
        (this.mechanismProperties = new HashMap<String, Object>(from.mechanismProperties)).put(mechanismPropertyKey.toLowerCase(), mechanismPropertyValue);
    }
    
    public String getMechanism() {
        return (this.mechanism == null) ? null : this.mechanism.getMechanismName();
    }
    
    public AuthenticationMechanism getAuthenticationMechanism() {
        return this.mechanism;
    }
    
    public String getUserName() {
        return this.userName;
    }
    
    public String getSource() {
        return this.source;
    }
    
    public char[] getPassword() {
        if (this.password == null) {
            return null;
        }
        return this.password.clone();
    }
    
    public <T> T getMechanismProperty(final String key, final T defaultValue) {
        Assertions.notNull("key", key);
        final T value = (T)this.mechanismProperties.get(key.toLowerCase());
        return (value == null) ? defaultValue : value;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final MongoCredential that = (MongoCredential)o;
        if (this.mechanism != that.mechanism) {
            return false;
        }
        if (!Arrays.equals(this.password, that.password)) {
            return false;
        }
        if (!this.source.equals(that.source)) {
            return false;
        }
        if (this.userName != null) {
            if (this.userName.equals(that.userName)) {
                return this.mechanismProperties.equals(that.mechanismProperties);
            }
        }
        else if (that.userName == null) {
            return this.mechanismProperties.equals(that.mechanismProperties);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.mechanism != null) ? this.mechanism.hashCode() : 0;
        result = 31 * result + ((this.userName != null) ? this.userName.hashCode() : 0);
        result = 31 * result + this.source.hashCode();
        result = 31 * result + ((this.password != null) ? Arrays.hashCode(this.password) : 0);
        result = 31 * result + this.mechanismProperties.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "MongoCredential{mechanism=" + this.mechanism + ", userName='" + this.userName + '\'' + ", source='" + this.source + '\'' + ", password=<hidden>" + ", mechanismProperties=" + this.mechanismProperties + '}';
    }
    
    static {
        MONGODB_CR_MECHANISM = AuthenticationMechanism.MONGODB_CR.getMechanismName();
        GSSAPI_MECHANISM = AuthenticationMechanism.GSSAPI.getMechanismName();
        PLAIN_MECHANISM = AuthenticationMechanism.PLAIN.getMechanismName();
        MONGODB_X509_MECHANISM = AuthenticationMechanism.MONGODB_X509.getMechanismName();
        SCRAM_SHA_1_MECHANISM = AuthenticationMechanism.SCRAM_SHA_1.getMechanismName();
    }
}
