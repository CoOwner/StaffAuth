package com.mongodb;

import com.mongodb.annotations.*;
import com.mongodb.assertions.*;
import java.util.*;

@Immutable
public final class MongoNamespace
{
    public static final String COMMAND_COLLECTION_NAME = "$cmd";
    private final String databaseName;
    private final String collectionName;
    private final String fullName;
    
    public static void checkDatabaseNameValidity(final String databaseName) {
        Assertions.notNull("databaseName", databaseName);
        Assertions.isTrueArgument("databaseName is not empty", !databaseName.isEmpty());
        for (final String cur : Arrays.asList(" ", ".")) {
            Assertions.isTrueArgument("databaseName does not contain '" + cur + "'", !databaseName.contains(cur));
        }
    }
    
    public static void checkCollectionNameValidity(final String collectionName) {
        Assertions.notNull("collectionName", collectionName);
        Assertions.isTrueArgument("collectionName is not empty", !collectionName.isEmpty());
    }
    
    public MongoNamespace(final String fullName) {
        Assertions.notNull("fullName", fullName);
        this.fullName = fullName;
        this.databaseName = getDatatabaseNameFromFullName(fullName);
        this.collectionName = getCollectionNameFullName(fullName);
        checkDatabaseNameValidity(this.databaseName);
        checkCollectionNameValidity(this.collectionName);
    }
    
    public MongoNamespace(final String databaseName, final String collectionName) {
        checkDatabaseNameValidity(databaseName);
        checkCollectionNameValidity(collectionName);
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.fullName = databaseName + '.' + collectionName;
    }
    
    public String getDatabaseName() {
        return this.databaseName;
    }
    
    public String getCollectionName() {
        return this.collectionName;
    }
    
    public String getFullName() {
        return this.fullName;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final MongoNamespace that = (MongoNamespace)o;
        return this.collectionName.equals(that.collectionName) && this.databaseName.equals(that.databaseName);
    }
    
    @Override
    public String toString() {
        return this.fullName;
    }
    
    @Override
    public int hashCode() {
        int result = this.databaseName.hashCode();
        result = 31 * result + this.collectionName.hashCode();
        return result;
    }
    
    private static String getCollectionNameFullName(final String namespace) {
        if (namespace == null) {
            return null;
        }
        final int firstDot = namespace.indexOf(46);
        if (firstDot == -1) {
            return namespace;
        }
        return namespace.substring(firstDot + 1);
    }
    
    private static String getDatatabaseNameFromFullName(final String namespace) {
        if (namespace == null) {
            return null;
        }
        final int firstDot = namespace.indexOf(46);
        if (firstDot == -1) {
            return "";
        }
        return namespace.substring(0, firstDot);
    }
}
