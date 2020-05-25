package com.mongodb.connection;

import com.mongodb.bulk.*;
import com.mongodb.*;
import com.mongodb.assertions.*;
import com.mongodb.internal.validator.*;
import java.util.*;
import org.bson.io.*;
import org.bson.codecs.*;
import org.bson.*;

class InsertCommandMessage extends BaseWriteCommandMessage
{
    private final List<InsertRequest> insertRequestList;
    
    public InsertCommandMessage(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final Boolean bypassDocumentValidation, final MessageSettings settings, final List<InsertRequest> insertRequestList) {
        super(namespace, ordered, writeConcern, bypassDocumentValidation, settings);
        this.insertRequestList = Assertions.notNull("insertRequestList", insertRequestList);
    }
    
    @Override
    public int getItemCount() {
        return this.insertRequestList.size();
    }
    
    @Override
    protected FieldNameValidator getFieldNameValidator() {
        final Map<String, FieldNameValidator> map = new HashMap<String, FieldNameValidator>();
        map.put("documents", new CollectibleDocumentFieldNameValidator());
        return new MappedFieldNameValidator(new NoOpFieldNameValidator(), map);
    }
    
    public List<InsertRequest> getRequests() {
        return Collections.unmodifiableList((List<? extends InsertRequest>)this.insertRequestList);
    }
    
    @Override
    protected String getCommandName() {
        return "insert";
    }
    
    @Override
    protected InsertCommandMessage writeTheWrites(final BsonOutput bsonOutput, final int commandStartPosition, final BsonBinaryWriter writer) {
        InsertCommandMessage nextMessage = null;
        writer.writeStartArray("documents");
        writer.pushMaxDocumentSize(this.getSettings().getMaxDocumentSize());
        for (int i = 0; i < this.insertRequestList.size(); ++i) {
            writer.mark();
            final BsonDocument document = this.insertRequestList.get(i).getDocument();
            this.getCodec(document).encode(writer, document, EncoderContext.builder().isEncodingCollectibleDocument(true).build());
            if (this.exceedsLimits(bsonOutput.getPosition() - commandStartPosition, i + 1)) {
                writer.reset();
                nextMessage = new InsertCommandMessage(this.getWriteNamespace(), this.isOrdered(), this.getWriteConcern(), this.getBypassDocumentValidation(), this.getSettings(), this.insertRequestList.subList(i, this.insertRequestList.size()));
                break;
            }
        }
        writer.popMaxDocumentSize();
        writer.writeEndArray();
        return nextMessage;
    }
}
