package com.mongodb;

import java.io.*;
import com.mongodb.assertions.*;

public class DBRef implements Serializable
{
    private static final long serialVersionUID = -849581217713362618L;
    private final Object id;
    private final String collectionName;
    private final String databaseName;
    
    public DBRef(final String collectionName, final Object id) {
        this(null, collectionName, id);
    }
    
    public DBRef(final String databaseName, final String collectionName, final Object id) {
        this.id = Assertions.notNull("id", id);
        this.collectionName = Assertions.notNull("ns", collectionName);
        this.databaseName = databaseName;
    }
    
    public Object getId() {
        return this.id;
    }
    
    public String getCollectionName() {
        return this.collectionName;
    }
    
    public String getDatabaseName() {
        return this.databaseName;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final DBRef dbRef = (DBRef)o;
        if (!this.id.equals(dbRef.id)) {
            return false;
        }
        if (!this.collectionName.equals(dbRef.collectionName)) {
            return false;
        }
        if (this.databaseName != null) {
            if (this.databaseName.equals(dbRef.databaseName)) {
                return true;
            }
        }
        else if (dbRef.databaseName == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = this.id.hashCode();
        result = 31 * result + this.collectionName.hashCode();
        result = 31 * result + ((this.databaseName != null) ? this.databaseName.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "{ \"$ref\" : \"" + this.collectionName + "\", \"$id\" : \"" + this.id + "" + ((this.databaseName == null) ? "" : (", \"$db\" : \"" + this.databaseName + "\"")) + " }";
    }
}
