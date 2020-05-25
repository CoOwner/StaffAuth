package com.mongodb;

import java.util.*;
import com.mongodb.bulk.*;

public class MongoBulkWriteException extends MongoServerException
{
    private static final long serialVersionUID = -4345399805987210275L;
    private final BulkWriteResult writeResult;
    private final List<BulkWriteError> errors;
    private final ServerAddress serverAddress;
    private final WriteConcernError writeConcernError;
    
    public MongoBulkWriteException(final BulkWriteResult writeResult, final List<BulkWriteError> writeErrors, final WriteConcernError writeConcernError, final ServerAddress serverAddress) {
        super("Bulk write operation error on server " + serverAddress + ". " + (writeErrors.isEmpty() ? "" : ("Write errors: " + writeErrors + ". ")) + ((writeConcernError == null) ? "" : ("Write concern error: " + writeConcernError + ". ")), serverAddress);
        this.writeResult = writeResult;
        this.errors = writeErrors;
        this.writeConcernError = writeConcernError;
        this.serverAddress = serverAddress;
    }
    
    public BulkWriteResult getWriteResult() {
        return this.writeResult;
    }
    
    public List<BulkWriteError> getWriteErrors() {
        return this.errors;
    }
    
    public WriteConcernError getWriteConcernError() {
        return this.writeConcernError;
    }
    
    @Override
    public ServerAddress getServerAddress() {
        return this.serverAddress;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final MongoBulkWriteException that = (MongoBulkWriteException)o;
        if (!this.errors.equals(that.errors)) {
            return false;
        }
        if (!this.serverAddress.equals(that.serverAddress)) {
            return false;
        }
        if (this.writeConcernError != null) {
            if (this.writeConcernError.equals(that.writeConcernError)) {
                return this.writeResult.equals(that.writeResult);
            }
        }
        else if (that.writeConcernError == null) {
            return this.writeResult.equals(that.writeResult);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = this.writeResult.hashCode();
        result = 31 * result + this.errors.hashCode();
        result = 31 * result + this.serverAddress.hashCode();
        result = 31 * result + ((this.writeConcernError != null) ? this.writeConcernError.hashCode() : 0);
        return result;
    }
}
