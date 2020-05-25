package com.mongodb.connection;

import com.mongodb.*;
import com.mongodb.assertions.*;
import com.mongodb.async.*;

class DefaultAuthenticator extends Authenticator
{
    public DefaultAuthenticator(final MongoCredential credential) {
        super(credential);
        Assertions.isTrueArgument("unspecified authentication mechanism", credential.getAuthenticationMechanism() == null);
    }
    
    @Override
    void authenticate(final InternalConnection connection, final ConnectionDescription connectionDescription) {
        this.createAuthenticator(connectionDescription).authenticate(connection, connectionDescription);
    }
    
    @Override
    void authenticateAsync(final InternalConnection connection, final ConnectionDescription connectionDescription, final SingleResultCallback<Void> callback) {
        this.createAuthenticator(connectionDescription).authenticateAsync(connection, connectionDescription, callback);
    }
    
    Authenticator createAuthenticator(final ConnectionDescription connectionDescription) {
        if (connectionDescription.getServerVersion().compareTo(new ServerVersion(2, 7)) >= 0) {
            return new ScramSha1Authenticator(this.getCredential());
        }
        return new NativeAuthenticator(this.getCredential());
    }
}
