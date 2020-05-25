package com.mongodb.connection;

import com.mongodb.assertions.*;
import java.io.*;
import javax.security.auth.callback.*;
import java.util.*;
import com.mongodb.*;
import javax.security.sasl.*;

class PlainAuthenticator extends SaslAuthenticator
{
    private static final String DEFAULT_PROTOCOL = "mongodb";
    
    PlainAuthenticator(final MongoCredential credential) {
        super(credential);
    }
    
    @Override
    public String getMechanismName() {
        return AuthenticationMechanism.PLAIN.getMechanismName();
    }
    
    @Override
    protected SaslClient createSaslClient(final ServerAddress serverAddress) {
        final MongoCredential credential = this.getCredential();
        Assertions.isTrue("mechanism is PLAIN", credential.getAuthenticationMechanism() == AuthenticationMechanism.PLAIN);
        try {
            return Sasl.createSaslClient(new String[] { AuthenticationMechanism.PLAIN.getMechanismName() }, credential.getUserName(), "mongodb", serverAddress.getHost(), null, new CallbackHandler() {
                @Override
                public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    for (final Callback callback : callbacks) {
                        if (callback instanceof PasswordCallback) {
                            ((PasswordCallback)callback).setPassword(credential.getPassword());
                        }
                        else if (callback instanceof NameCallback) {
                            ((NameCallback)callback).setName(credential.getUserName());
                        }
                    }
                }
            });
        }
        catch (SaslException e) {
            throw new MongoSecurityException(credential, "Exception initializing SASL client", e);
        }
    }
}
