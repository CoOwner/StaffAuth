package com.mongodb.connection;

import com.mongodb.assertions.*;
import com.mongodb.async.*;
import org.bson.*;
import java.util.*;
import java.util.concurrent.atomic.*;

class InternalStreamConnectionInitializer implements InternalConnectionInitializer
{
    private final List<Authenticator> authenticators;
    private final BsonDocument clientMetadataDocument;
    
    InternalStreamConnectionInitializer(final List<Authenticator> authenticators, final BsonDocument clientMetadataDocument) {
        this.authenticators = Assertions.notNull("authenticators", authenticators);
        this.clientMetadataDocument = clientMetadataDocument;
    }
    
    @Override
    public ConnectionDescription initialize(final InternalConnection internalConnection) {
        Assertions.notNull("internalConnection", internalConnection);
        final ConnectionDescription connectionDescription = this.initializeConnectionDescription(internalConnection);
        this.authenticateAll(internalConnection, connectionDescription);
        return this.completeConnectionDescriptionInitialization(internalConnection, connectionDescription);
    }
    
    @Override
    public void initializeAsync(final InternalConnection internalConnection, final SingleResultCallback<ConnectionDescription> callback) {
        this.initializeConnectionDescriptionAsync(internalConnection, this.createConnectionDescriptionCallback(internalConnection, callback));
    }
    
    private SingleResultCallback<ConnectionDescription> createConnectionDescriptionCallback(final InternalConnection internalConnection, final SingleResultCallback<ConnectionDescription> callback) {
        return new SingleResultCallback<ConnectionDescription>() {
            @Override
            public void onResult(final ConnectionDescription connectionDescription, final Throwable t) {
                if (t != null) {
                    callback.onResult(null, t);
                }
                else {
                    new CompoundAuthenticator(internalConnection, connectionDescription, new SingleResultCallback<Void>() {
                        @Override
                        public void onResult(final Void result, final Throwable t) {
                            if (t != null) {
                                callback.onResult(null, t);
                            }
                            else {
                                InternalStreamConnectionInitializer.this.completeConnectionDescriptionInitializationAsync(internalConnection, connectionDescription, callback);
                            }
                        }
                    }).start();
                }
            }
        };
    }
    
    private ConnectionDescription initializeConnectionDescription(final InternalConnection internalConnection) {
        final BsonDocument isMasterResult = CommandHelper.executeCommand("admin", this.createIsMasterCommand(), internalConnection);
        final BsonDocument buildInfoResult = CommandHelper.executeCommand("admin", new BsonDocument("buildinfo", new BsonInt32(1)), internalConnection);
        return DescriptionHelper.createConnectionDescription(internalConnection.getDescription().getConnectionId(), isMasterResult, buildInfoResult);
    }
    
    private BsonDocument createIsMasterCommand() {
        final BsonDocument isMasterCommandDocument = new BsonDocument("ismaster", new BsonInt32(1));
        if (this.clientMetadataDocument != null) {
            isMasterCommandDocument.append("client", this.clientMetadataDocument);
        }
        return isMasterCommandDocument;
    }
    
    private ConnectionDescription completeConnectionDescriptionInitialization(final InternalConnection internalConnection, final ConnectionDescription connectionDescription) {
        return this.applyGetLastErrorResult(CommandHelper.executeCommandWithoutCheckingForFailure("admin", new BsonDocument("getlasterror", new BsonInt32(1)), internalConnection), connectionDescription);
    }
    
    private void authenticateAll(final InternalConnection internalConnection, final ConnectionDescription connectionDescription) {
        if (connectionDescription.getServerType() != ServerType.REPLICA_SET_ARBITER) {
            for (final Authenticator cur : this.authenticators) {
                cur.authenticate(internalConnection, connectionDescription);
            }
        }
    }
    
    private void initializeConnectionDescriptionAsync(final InternalConnection internalConnection, final SingleResultCallback<ConnectionDescription> callback) {
        CommandHelper.executeCommandAsync("admin", this.createIsMasterCommand(), internalConnection, new SingleResultCallback<BsonDocument>() {
            @Override
            public void onResult(final BsonDocument isMasterResult, final Throwable t) {
                if (t != null) {
                    callback.onResult(null, t);
                }
                else {
                    CommandHelper.executeCommandAsync("admin", new BsonDocument("buildinfo", new BsonInt32(1)), internalConnection, new SingleResultCallback<BsonDocument>() {
                        @Override
                        public void onResult(final BsonDocument buildInfoResult, final Throwable t) {
                            if (t != null) {
                                callback.onResult(null, t);
                            }
                            else {
                                final ConnectionId connectionId = internalConnection.getDescription().getConnectionId();
                                callback.onResult(DescriptionHelper.createConnectionDescription(connectionId, isMasterResult, buildInfoResult), null);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private void completeConnectionDescriptionInitializationAsync(final InternalConnection internalConnection, final ConnectionDescription connectionDescription, final SingleResultCallback<ConnectionDescription> callback) {
        CommandHelper.executeCommandAsync("admin", new BsonDocument("getlasterror", new BsonInt32(1)), internalConnection, new SingleResultCallback<BsonDocument>() {
            @Override
            public void onResult(final BsonDocument result, final Throwable t) {
                if (result == null) {
                    callback.onResult(connectionDescription, null);
                }
                else {
                    callback.onResult(InternalStreamConnectionInitializer.this.applyGetLastErrorResult(result, connectionDescription), null);
                }
            }
        });
    }
    
    private ConnectionDescription applyGetLastErrorResult(final BsonDocument getLastErrorResult, final ConnectionDescription connectionDescription) {
        ConnectionId connectionId;
        if (getLastErrorResult.containsKey("connectionId")) {
            connectionId = connectionDescription.getConnectionId().withServerValue(getLastErrorResult.getNumber("connectionId").intValue());
        }
        else {
            connectionId = connectionDescription.getConnectionId();
        }
        return connectionDescription.withConnectionId(connectionId);
    }
    
    private class CompoundAuthenticator implements SingleResultCallback<Void>
    {
        private final InternalConnection internalConnection;
        private final ConnectionDescription connectionDescription;
        private final SingleResultCallback<Void> callback;
        private final AtomicInteger currentAuthenticatorIndex;
        
        public CompoundAuthenticator(final InternalConnection internalConnection, final ConnectionDescription connectionDescription, final SingleResultCallback<Void> callback) {
            this.currentAuthenticatorIndex = new AtomicInteger(-1);
            this.internalConnection = internalConnection;
            this.connectionDescription = connectionDescription;
            this.callback = callback;
        }
        
        @Override
        public void onResult(final Void result, final Throwable t) {
            if (t != null) {
                this.callback.onResult(null, t);
            }
            else if (this.completedAuthentication()) {
                this.callback.onResult(null, null);
            }
            else {
                this.authenticateNext();
            }
        }
        
        public void start() {
            if (this.connectionDescription.getServerType() == ServerType.REPLICA_SET_ARBITER || InternalStreamConnectionInitializer.this.authenticators.isEmpty()) {
                this.callback.onResult(null, null);
            }
            else {
                this.authenticateNext();
            }
        }
        
        private boolean completedAuthentication() {
            return this.currentAuthenticatorIndex.get() == InternalStreamConnectionInitializer.this.authenticators.size() - 1;
        }
        
        private void authenticateNext() {
            InternalStreamConnectionInitializer.this.authenticators.get(this.currentAuthenticatorIndex.incrementAndGet()).authenticateAsync(this.internalConnection, this.connectionDescription, this);
        }
    }
}
