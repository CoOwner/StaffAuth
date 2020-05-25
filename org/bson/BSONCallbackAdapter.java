package org.bson;

import org.bson.io.*;
import org.bson.types.*;

class BSONCallbackAdapter extends AbstractBsonWriter
{
    private BSONCallback bsonCallback;
    
    protected BSONCallbackAdapter(final BsonWriterSettings settings, final BSONCallback bsonCallback) {
        super(settings);
        this.bsonCallback = bsonCallback;
    }
    
    @Override
    public void flush() {
    }
    
    public void doWriteStartDocument() {
        final BsonContextType contextType = (this.getState() == State.SCOPE_DOCUMENT) ? BsonContextType.SCOPE_DOCUMENT : BsonContextType.DOCUMENT;
        if (this.getContext() == null || contextType == BsonContextType.SCOPE_DOCUMENT) {
            this.bsonCallback.objectStart();
        }
        else {
            this.bsonCallback.objectStart(this.getName());
        }
        this.setContext(new Context(this.getContext(), contextType));
    }
    
    @Override
    protected void doWriteEndDocument() {
        final BsonContextType contextType = this.getContext().getContextType();
        this.setContext(this.getContext().getParentContext());
        this.bsonCallback.objectDone();
        if (contextType == BsonContextType.SCOPE_DOCUMENT) {
            final Object scope = this.bsonCallback.get();
            (this.bsonCallback = this.getContext().callback).gotCodeWScope(this.getContext().name, this.getContext().code, scope);
        }
    }
    
    public void doWriteStartArray() {
        this.bsonCallback.arrayStart(this.getName());
        this.setContext(new Context(this.getContext(), BsonContextType.ARRAY));
    }
    
    @Override
    protected void doWriteEndArray() {
        this.setContext(this.getContext().getParentContext());
        this.bsonCallback.arrayDone();
    }
    
    @Override
    protected void doWriteBinaryData(final BsonBinary value) {
        if (value.getType() == BsonBinarySubType.UUID_LEGACY.getValue()) {
            this.bsonCallback.gotUUID(this.getName(), Bits.readLong(value.getData(), 0), Bits.readLong(value.getData(), 8));
        }
        else {
            this.bsonCallback.gotBinary(this.getName(), value.getType(), value.getData());
        }
    }
    
    public void doWriteBoolean(final boolean value) {
        this.bsonCallback.gotBoolean(this.getName(), value);
        this.setState(this.getNextState());
    }
    
    @Override
    protected void doWriteDateTime(final long value) {
        this.bsonCallback.gotDate(this.getName(), value);
    }
    
    @Override
    protected void doWriteDBPointer(final BsonDbPointer value) {
        this.bsonCallback.gotDBRef(this.getName(), value.getNamespace(), value.getId());
    }
    
    @Override
    protected void doWriteDouble(final double value) {
        this.bsonCallback.gotDouble(this.getName(), value);
    }
    
    @Override
    protected void doWriteInt32(final int value) {
        this.bsonCallback.gotInt(this.getName(), value);
    }
    
    @Override
    protected void doWriteInt64(final long value) {
        this.bsonCallback.gotLong(this.getName(), value);
    }
    
    @Override
    protected void doWriteDecimal128(final Decimal128 value) {
        this.bsonCallback.gotDecimal128(this.getName(), value);
    }
    
    @Override
    protected void doWriteJavaScript(final String value) {
        this.bsonCallback.gotCode(this.getName(), value);
    }
    
    @Override
    protected void doWriteJavaScriptWithScope(final String value) {
        this.getContext().callback = this.bsonCallback;
        this.getContext().code = value;
        this.getContext().name = this.getName();
        this.bsonCallback = this.bsonCallback.createBSONCallback();
    }
    
    @Override
    protected void doWriteMaxKey() {
        this.bsonCallback.gotMaxKey(this.getName());
    }
    
    @Override
    protected void doWriteMinKey() {
        this.bsonCallback.gotMinKey(this.getName());
    }
    
    public void doWriteNull() {
        this.bsonCallback.gotNull(this.getName());
    }
    
    public void doWriteObjectId(final ObjectId value) {
        this.bsonCallback.gotObjectId(this.getName(), value);
    }
    
    public void doWriteRegularExpression(final BsonRegularExpression value) {
        this.bsonCallback.gotRegex(this.getName(), value.getPattern(), value.getOptions());
    }
    
    public void doWriteString(final String value) {
        this.bsonCallback.gotString(this.getName(), value);
    }
    
    public void doWriteSymbol(final String value) {
        this.bsonCallback.gotSymbol(this.getName(), value);
    }
    
    public void doWriteTimestamp(final BsonTimestamp value) {
        this.bsonCallback.gotTimestamp(this.getName(), value.getTime(), value.getInc());
    }
    
    public void doWriteUndefined() {
        this.bsonCallback.gotUndefined(this.getName());
    }
    
    @Override
    protected Context getContext() {
        return (Context)super.getContext();
    }
    
    @Override
    protected String getName() {
        if (this.getContext().getContextType() == BsonContextType.ARRAY) {
            return Integer.toString(this.getContext().index++);
        }
        return super.getName();
    }
    
    public class Context extends AbstractBsonWriter.Context
    {
        private int index;
        private BSONCallback callback;
        private String code;
        private String name;
        
        public Context(final Context parentContext, final BsonContextType contextType) {
            super(parentContext, contextType);
        }
        
        @Override
        public Context getParentContext() {
            return (Context)super.getParentContext();
        }
    }
}
