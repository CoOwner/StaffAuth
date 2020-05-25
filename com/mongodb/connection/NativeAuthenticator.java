package com.mongodb.connection;

import com.mongodb.internal.authentication.*;
import com.mongodb.*;
import org.bson.*;
import com.mongodb.async.*;

class NativeAuthenticator extends Authenticator
{
    public NativeAuthenticator(final MongoCredential credential) {
        super(credential);
    }
    
    public void authenticate(final InternalConnection connection, final ConnectionDescription connectionDescription) {
        try {
            final BsonDocument nonceResponse = CommandHelper.executeCommand(this.getCredential().getSource(), NativeAuthenticationHelper.getNonceCommand(), connection);
            final BsonDocument authCommand = NativeAuthenticationHelper.getAuthCommand(this.getCredential().getUserName(), this.getCredential().getPassword(), ((BsonString)nonceResponse.get("nonce")).getValue());
            CommandHelper.executeCommand(this.getCredential().getSource(), authCommand, connection);
        }
        catch (MongoCommandException e) {
            throw new MongoSecurityException(this.getCredential(), "Exception authenticating", e);
        }
    }
    
    @Override
    void authenticateAsync(final InternalConnection connection, final ConnectionDescription connectionDescription, final SingleResultCallback<Void> callback) {
        CommandHelper.executeCommandAsync(this.getCredential().getSource(), NativeAuthenticationHelper.getNonceCommand(), connection, new SingleResultCallback<BsonDocument>() {
            @Override
            public void onResult(final BsonDocument nonceResult, final Throwable t) {
                if (t != null) {
                    callback.onResult(null, NativeAuthenticator.this.translateThrowable(t));
                }
                else {
                    CommandHelper.executeCommandAsync(NativeAuthenticator.this.getCredential().getSource(), NativeAuthenticationHelper.getAuthCommand(NativeAuthenticator.this.getCredential().getUserName(), NativeAuthenticator.this.getCredential().getPassword(), ((BsonString)nonceResult.get("nonce")).getValue()), connection, new SingleResultCallback<BsonDocument>() {
                        @Override
                        public void onResult(final BsonDocument result, final Throwable t) {
                            if (t != null) {
                                callback.onResult(null, NativeAuthenticator.this.translateThrowable(t));
                            }
                            else {
                                callback.onResult(null, null);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private Throwable translateThrowable(final Throwable t) {
        if (t instanceof MongoCommandException) {
            return new MongoSecurityException(this.getCredential(), "Exception authenticating", t);
        }
        return t;
    }
}
