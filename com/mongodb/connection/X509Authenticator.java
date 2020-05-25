package com.mongodb.connection;

import com.mongodb.async.*;
import org.bson.*;
import com.mongodb.*;

class X509Authenticator extends Authenticator
{
    X509Authenticator(final MongoCredential credential) {
        super(credential);
    }
    
    @Override
    void authenticate(final InternalConnection connection, final ConnectionDescription connectionDescription) {
        try {
            this.validateUserName(connectionDescription);
            final BsonDocument authCommand = this.getAuthCommand(this.getCredential().getUserName());
            CommandHelper.executeCommand(this.getCredential().getSource(), authCommand, connection);
        }
        catch (MongoCommandException e) {
            throw new MongoSecurityException(this.getCredential(), "Exception authenticating", e);
        }
    }
    
    @Override
    void authenticateAsync(final InternalConnection connection, final ConnectionDescription connectionDescription, final SingleResultCallback<Void> callback) {
        try {
            this.validateUserName(connectionDescription);
            CommandHelper.executeCommandAsync(this.getCredential().getSource(), this.getAuthCommand(this.getCredential().getUserName()), connection, new SingleResultCallback<BsonDocument>() {
                @Override
                public void onResult(final BsonDocument nonceResult, final Throwable t) {
                    if (t != null) {
                        callback.onResult(null, X509Authenticator.this.translateThrowable(t));
                    }
                    else {
                        callback.onResult(null, null);
                    }
                }
            });
        }
        catch (Throwable t) {
            callback.onResult(null, t);
        }
    }
    
    private BsonDocument getAuthCommand(final String userName) {
        final BsonDocument cmd = new BsonDocument();
        cmd.put("authenticate", new BsonInt32(1));
        if (userName != null) {
            cmd.put("user", new BsonString(userName));
        }
        cmd.put("mechanism", new BsonString(AuthenticationMechanism.MONGODB_X509.getMechanismName()));
        return cmd;
    }
    
    private Throwable translateThrowable(final Throwable t) {
        if (t instanceof MongoCommandException) {
            return new MongoSecurityException(this.getCredential(), "Exception authenticating", t);
        }
        return t;
    }
    
    private void validateUserName(final ConnectionDescription connectionDescription) {
        if (this.getCredential().getUserName() == null && connectionDescription.getServerVersion().compareTo(new ServerVersion(3, 4)) < 0) {
            throw new MongoSecurityException(this.getCredential(), "User name is required for the MONGODB-X509 authentication mechanism on server versions less than 3.4");
        }
    }
}
