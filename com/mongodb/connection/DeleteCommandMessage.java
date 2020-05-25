package com.mongodb.connection;

import com.mongodb.bulk.*;
import com.mongodb.*;
import com.mongodb.internal.validator.*;
import java.util.*;
import org.bson.io.*;
import org.bson.codecs.*;
import org.bson.*;

class DeleteCommandMessage extends BaseWriteCommandMessage
{
    private final List<DeleteRequest> deletes;
    
    public DeleteCommandMessage(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final MessageSettings settings, final List<DeleteRequest> deletes) {
        super(namespace, ordered, writeConcern, null, settings);
        this.deletes = deletes;
    }
    
    @Override
    public int getItemCount() {
        return this.deletes.size();
    }
    
    @Override
    protected FieldNameValidator getFieldNameValidator() {
        return new NoOpFieldNameValidator();
    }
    
    public List<DeleteRequest> getRequests() {
        return Collections.unmodifiableList((List<? extends DeleteRequest>)this.deletes);
    }
    
    @Override
    protected String getCommandName() {
        return "delete";
    }
    
    @Override
    protected BaseWriteCommandMessage writeTheWrites(final BsonOutput bsonOutput, final int commandStartPosition, final BsonBinaryWriter writer) {
        DeleteCommandMessage nextMessage = null;
        writer.writeStartArray("deletes");
        for (int i = 0; i < this.deletes.size(); ++i) {
            writer.mark();
            final DeleteRequest deleteRequest = this.deletes.get(i);
            writer.writeStartDocument();
            writer.pushMaxDocumentSize(this.getSettings().getMaxDocumentSize());
            writer.writeName("q");
            this.getCodec(deleteRequest.getFilter()).encode(writer, deleteRequest.getFilter(), EncoderContext.builder().build());
            writer.writeInt32("limit", deleteRequest.isMulti() ? 0 : 1);
            if (deleteRequest.getCollation() != null) {
                writer.writeName("collation");
                final BsonDocument collation = deleteRequest.getCollation().asDocument();
                this.getCodec(collation).encode(writer, collation, EncoderContext.builder().build());
            }
            writer.popMaxDocumentSize();
            writer.writeEndDocument();
            if (this.exceedsLimits(bsonOutput.getPosition() - commandStartPosition, i + 1)) {
                writer.reset();
                nextMessage = new DeleteCommandMessage(this.getWriteNamespace(), this.isOrdered(), this.getWriteConcern(), this.getSettings(), this.deletes.subList(i, this.deletes.size()));
                break;
            }
        }
        writer.writeEndArray();
        return nextMessage;
    }
}
