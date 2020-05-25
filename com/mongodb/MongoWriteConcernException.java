package com.mongodb;

import com.mongodb.bulk.*;
import com.mongodb.assertions.*;

public class MongoWriteConcernException extends MongoServerException
{
    private static final long serialVersionUID = 4577579466973523211L;
    private final WriteConcernError writeConcernError;
    private final WriteConcernResult writeConcernResult;
    
    public MongoWriteConcernException(final WriteConcernError writeConcernError, final ServerAddress serverAddress) {
        this(writeConcernError, null, serverAddress);
    }
    
    public MongoWriteConcernException(final WriteConcernError writeConcernError, final WriteConcernResult writeConcernResult, final ServerAddress serverAddress) {
        super(writeConcernError.getCode(), writeConcernError.getMessage(), serverAddress);
        this.writeConcernResult = writeConcernResult;
        this.writeConcernError = Assertions.notNull("writeConcernError", writeConcernError);
    }
    
    public WriteConcernError getWriteConcernError() {
        return this.writeConcernError;
    }
    
    public WriteConcernResult getWriteResult() {
        return this.writeConcernResult;
    }
}
