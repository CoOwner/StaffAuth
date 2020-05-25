package com.mongodb.connection;

import java.security.*;
import com.mongodb.async.*;
import javax.security.sasl.*;
import com.mongodb.*;
import javax.security.auth.*;
import org.bson.*;

abstract class SaslAuthenticator extends Authenticator
{
    SaslAuthenticator(final MongoCredential credential) {
        super(credential);
    }
    
    public void authenticate(final InternalConnection connection, final ConnectionDescription connectionDescription) {
        this.doAsSubject(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                final SaslClient saslClient = SaslAuthenticator.this.createSaslClient(connection.getDescription().getServerAddress());
                try {
                    byte[] response = (byte[])(saslClient.hasInitialResponse() ? saslClient.evaluateChallenge(new byte[0]) : null);
                    BsonDocument res = SaslAuthenticator.this.sendSaslStart(response, connection);
                    for (BsonInt32 conversationId = res.getInt32("conversationId"); !res.getBoolean("done").getValue(); res = SaslAuthenticator.this.sendSaslContinue(conversationId, response, connection)) {
                        response = saslClient.evaluateChallenge(res.getBinary("payload").getData());
                        if (response == null) {
                            throw new MongoSecurityException(SaslAuthenticator.this.getCredential(), "SASL protocol error: no client response to challenge for credential " + SaslAuthenticator.this.getCredential());
                        }
                    }
                }
                catch (Exception e) {
                    throw SaslAuthenticator.this.wrapInMongoSecurityException(e);
                }
                finally {
                    SaslAuthenticator.this.disposeOfSaslClient(saslClient);
                }
                return null;
            }
        });
    }
    
    @Override
    void authenticateAsync(final InternalConnection connection, final ConnectionDescription connectionDescription, final SingleResultCallback<Void> callback) {
        try {
            this.doAsSubject(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    final SaslClient saslClient = SaslAuthenticator.this.createSaslClient(connection.getDescription().getServerAddress());
                    try {
                        final byte[] response = (byte[])(saslClient.hasInitialResponse() ? saslClient.evaluateChallenge(new byte[0]) : null);
                        SaslAuthenticator.this.sendSaslStartAsync(response, connection, new SingleResultCallback<BsonDocument>() {
                            @Override
                            public void onResult(final BsonDocument result, final Throwable t) {
                                if (t != null) {
                                    callback.onResult(null, SaslAuthenticator.this.wrapInMongoSecurityException(t));
                                }
                                else if (result.getBoolean("done").getValue()) {
                                    callback.onResult(null, null);
                                }
                                else {
                                    new Continuator(saslClient, result, connection, callback).start();
                                }
                            }
                        });
                    }
                    catch (SaslException e) {
                        throw SaslAuthenticator.this.wrapInMongoSecurityException(e);
                    }
                    return null;
                }
            });
        }
        catch (Throwable t) {
            callback.onResult(null, t);
        }
    }
    
    public abstract String getMechanismName();
    
    protected abstract SaslClient createSaslClient(final ServerAddress p0);
    
    private Subject getSubject() {
        return this.getCredential().getMechanismProperty("JAVA_SUBJECT", (Subject)null);
    }
    
    private BsonDocument sendSaslStart(final byte[] outToken, final InternalConnection connection) {
        return CommandHelper.executeCommand(this.getCredential().getSource(), this.createSaslStartCommandDocument(outToken), connection);
    }
    
    private BsonDocument sendSaslContinue(final BsonInt32 conversationId, final byte[] outToken, final InternalConnection connection) {
        return CommandHelper.executeCommand(this.getCredential().getSource(), this.createSaslContinueDocument(conversationId, outToken), connection);
    }
    
    private void sendSaslStartAsync(final byte[] outToken, final InternalConnection connection, final SingleResultCallback<BsonDocument> callback) {
        CommandHelper.executeCommandAsync(this.getCredential().getSource(), this.createSaslStartCommandDocument(outToken), connection, callback);
    }
    
    private void sendSaslContinueAsync(final BsonInt32 conversationId, final byte[] outToken, final InternalConnection connection, final SingleResultCallback<BsonDocument> callback) {
        CommandHelper.executeCommandAsync(this.getCredential().getSource(), this.createSaslContinueDocument(conversationId, outToken), connection, callback);
    }
    
    private BsonDocument createSaslStartCommandDocument(final byte[] outToken) {
        return new BsonDocument("saslStart", new BsonInt32(1)).append("mechanism", new BsonString(this.getMechanismName())).append("payload", new BsonBinary((outToken != null) ? outToken : new byte[0]));
    }
    
    private BsonDocument createSaslContinueDocument(final BsonInt32 conversationId, final byte[] outToken) {
        return new BsonDocument("saslContinue", new BsonInt32(1)).append("conversationId", conversationId).append("payload", new BsonBinary(outToken));
    }
    
    private void disposeOfSaslClient(final SaslClient saslClient) {
        try {
            saslClient.dispose();
        }
        catch (SaslException ex) {}
    }
    
    private MongoSecurityException wrapInMongoSecurityException(final Throwable t) {
        return (MongoSecurityException)((t instanceof MongoSecurityException) ? t : new MongoSecurityException(this.getCredential(), "Exception authenticating " + this.getCredential(), t));
    }
    
    void doAsSubject(final PrivilegedAction<Void> action) {
        if (this.getSubject() == null) {
            action.run();
        }
        else {
            Subject.doAs(this.getSubject(), action);
        }
    }
    
    private final class Continuator implements SingleResultCallback<BsonDocument>
    {
        private final SaslClient saslClient;
        private final BsonDocument saslStartDocument;
        private final InternalConnection connection;
        private final SingleResultCallback<Void> callback;
        
        public Continuator(final SaslClient saslClient, final BsonDocument saslStartDocument, final InternalConnection connection, final SingleResultCallback<Void> callback) {
            this.saslClient = saslClient;
            this.saslStartDocument = saslStartDocument;
            this.connection = connection;
            this.callback = callback;
        }
        
        @Override
        public void onResult(final BsonDocument result, final Throwable t) {
            if (t != null) {
                this.callback.onResult(null, SaslAuthenticator.this.wrapInMongoSecurityException(t));
                SaslAuthenticator.this.disposeOfSaslClient(this.saslClient);
            }
            else if (result.getBoolean("done").getValue()) {
                this.callback.onResult(null, null);
                SaslAuthenticator.this.disposeOfSaslClient(this.saslClient);
            }
            else {
                this.continueConversation(result);
            }
        }
        
        public void start() {
            this.continueConversation(this.saslStartDocument);
        }
        
        private void continueConversation(final BsonDocument result) {
            try {
                SaslAuthenticator.this.doAsSubject(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        try {
                            SaslAuthenticator.this.sendSaslContinueAsync(Continuator.this.saslStartDocument.getInt32("conversationId"), Continuator.this.saslClient.evaluateChallenge(result.getBinary("payload").getData()), Continuator.this.connection, Continuator.this);
                        }
                        catch (SaslException e) {
                            throw SaslAuthenticator.this.wrapInMongoSecurityException(e);
                        }
                        return null;
                    }
                });
            }
            catch (Throwable t) {
                this.callback.onResult(null, t);
                SaslAuthenticator.this.disposeOfSaslClient(this.saslClient);
            }
        }
    }
}
