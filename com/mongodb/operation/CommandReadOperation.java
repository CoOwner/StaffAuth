package com.mongodb.operation;

import org.bson.*;
import org.bson.codecs.*;
import com.mongodb.assertions.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;

public class CommandReadOperation<T> implements AsyncReadOperation<T>, ReadOperation<T>
{
    private final String databaseName;
    private final BsonDocument command;
    private final Decoder<T> decoder;
    
    public CommandReadOperation(final String databaseName, final BsonDocument command, final Decoder<T> decoder) {
        this.databaseName = Assertions.notNull("databaseName", databaseName);
        this.command = Assertions.notNull("command", command);
        this.decoder = Assertions.notNull("decoder", decoder);
    }
    
    @Override
    public T execute(final ReadBinding binding) {
        return CommandOperationHelper.executeWrappedCommandProtocol(binding, this.databaseName, this.command, this.decoder);
    }
    
    @Override
    public void executeAsync(final AsyncReadBinding binding, final SingleResultCallback<T> callback) {
        CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, this.databaseName, this.command, this.decoder, callback);
    }
}
