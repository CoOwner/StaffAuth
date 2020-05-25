package com.mongodb.operation;

import com.mongodb.internal.authentication.*;
import java.util.*;
import org.bson.types.*;
import org.bson.*;
import com.mongodb.*;
import com.mongodb.async.*;

final class UserOperationHelper
{
    static BsonDocument asCommandDocument(final MongoCredential credential, final boolean readOnly, final String commandName) {
        final BsonDocument document = new BsonDocument();
        document.put(commandName, new BsonString(credential.getUserName()));
        document.put("pwd", new BsonString(NativeAuthenticationHelper.createAuthenticationHash(credential.getUserName(), credential.getPassword())));
        document.put("digestPassword", BsonBoolean.FALSE);
        document.put("roles", new BsonArray(Arrays.asList(new BsonString(getRoleName(credential, readOnly)))));
        return document;
    }
    
    private static String getRoleName(final MongoCredential credential, final boolean readOnly) {
        return credential.getSource().equals("admin") ? (readOnly ? "readAnyDatabase" : "root") : (readOnly ? "read" : "dbOwner");
    }
    
    static BsonDocument asCollectionQueryDocument(final MongoCredential credential) {
        return new BsonDocument("user", new BsonString(credential.getUserName()));
    }
    
    static BsonDocument asCollectionUpdateDocument(final MongoCredential credential, final boolean readOnly) {
        return asCollectionQueryDocument(credential).append("pwd", new BsonString(NativeAuthenticationHelper.createAuthenticationHash(credential.getUserName(), credential.getPassword()))).append("readOnly", BsonBoolean.valueOf(readOnly));
    }
    
    static BsonDocument asCollectionInsertDocument(final MongoCredential credential, final boolean readOnly) {
        return asCollectionUpdateDocument(credential, readOnly).append("_id", new BsonObjectId(new ObjectId()));
    }
    
    static void translateUserCommandException(final MongoCommandException e) {
        if (e.getErrorCode() == 100 && WriteConcernHelper.hasWriteConcernError(e.getResponse())) {
            throw WriteConcernHelper.createWriteConcernError(e.getResponse(), e.getServerAddress());
        }
        throw e;
    }
    
    static SingleResultCallback<Void> userCommandCallback(final SingleResultCallback<Void> wrappedCallback) {
        return new SingleResultCallback<Void>() {
            @Override
            public void onResult(final Void result, final Throwable t) {
                if (t != null) {
                    if (t instanceof MongoCommandException && WriteConcernHelper.hasWriteConcernError(((MongoCommandException)t).getResponse())) {
                        wrappedCallback.onResult(null, WriteConcernHelper.createWriteConcernError(((MongoCommandException)t).getResponse(), ((MongoCommandException)t).getServerAddress()));
                    }
                    else {
                        wrappedCallback.onResult(null, t);
                    }
                }
                else {
                    wrappedCallback.onResult(null, null);
                }
            }
        };
    }
    
    private UserOperationHelper() {
    }
}
