package com.mongodb.operation;

import com.mongodb.binding.*;
import com.mongodb.connection.*;
import org.bson.*;
import com.mongodb.*;
import org.bson.codecs.*;

public class FsyncUnlockOperation implements WriteOperation<BsonDocument>
{
    @Override
    public BsonDocument execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<BsonDocument>)new OperationHelper.CallableWithConnection<BsonDocument>() {
            @Override
            public BsonDocument call(final Connection connection) {
                if (OperationHelper.serverIsAtLeastVersionThreeDotTwo(connection.getDescription())) {
                    return CommandOperationHelper.executeWrappedCommandProtocol(binding, "admin", new BsonDocument("fsyncUnlock", new BsonInt32(1)), connection);
                }
                return connection.query(new MongoNamespace("admin", "$cmd.sys.unlock"), new BsonDocument(), null, 0, 1, 0, false, false, false, false, false, false, (Decoder<BsonDocument>)new BsonDocumentCodec()).getResults().get(0);
            }
        });
    }
}
