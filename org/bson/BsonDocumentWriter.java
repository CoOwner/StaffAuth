package org.bson;

import org.bson.types.*;

public class BsonDocumentWriter extends AbstractBsonWriter
{
    private final BsonDocument document;
    
    public BsonDocumentWriter(final BsonDocument document) {
        super(new BsonWriterSettings());
        this.document = document;
        this.setContext(new Context());
    }
    
    public BsonDocument getDocument() {
        return this.document;
    }
    
    @Override
    protected void doWriteStartDocument() {
        switch (this.getState()) {
            case INITIAL: {
                this.setContext(new Context(this.document, BsonContextType.DOCUMENT, this.getContext()));
                break;
            }
            case VALUE: {
                this.setContext(new Context(new BsonDocument(), BsonContextType.DOCUMENT, this.getContext()));
                break;
            }
            case SCOPE_DOCUMENT: {
                this.setContext(new Context(new BsonDocument(), BsonContextType.SCOPE_DOCUMENT, this.getContext()));
                break;
            }
            default: {
                throw new BsonInvalidOperationException("Unexpected state " + this.getState());
            }
        }
    }
    
    @Override
    protected void doWriteEndDocument() {
        final BsonValue value = this.getContext().container;
        this.setContext(this.getContext().getParentContext());
        if (this.getContext().getContextType() == BsonContextType.JAVASCRIPT_WITH_SCOPE) {
            final BsonDocument scope = (BsonDocument)value;
            final BsonString code = (BsonString)this.getContext().container;
            this.setContext(this.getContext().getParentContext());
            this.write(new BsonJavaScriptWithScope(code.getValue(), scope));
        }
        else if (this.getContext().getContextType() != BsonContextType.TOP_LEVEL) {
            this.write(value);
        }
    }
    
    @Override
    protected void doWriteStartArray() {
        this.setContext(new Context(new BsonArray(), BsonContextType.ARRAY, this.getContext()));
    }
    
    @Override
    protected void doWriteEndArray() {
        final BsonValue array = this.getContext().container;
        this.setContext(this.getContext().getParentContext());
        this.write(array);
    }
    
    @Override
    protected void doWriteBinaryData(final BsonBinary value) {
        this.write(value);
    }
    
    public void doWriteBoolean(final boolean value) {
        this.write(BsonBoolean.valueOf(value));
    }
    
    @Override
    protected void doWriteDateTime(final long value) {
        this.write(new BsonDateTime(value));
    }
    
    @Override
    protected void doWriteDBPointer(final BsonDbPointer value) {
        this.write(value);
    }
    
    @Override
    protected void doWriteDouble(final double value) {
        this.write(new BsonDouble(value));
    }
    
    @Override
    protected void doWriteInt32(final int value) {
        this.write(new BsonInt32(value));
    }
    
    @Override
    protected void doWriteInt64(final long value) {
        this.write(new BsonInt64(value));
    }
    
    @Override
    protected void doWriteDecimal128(final Decimal128 value) {
        this.write(new BsonDecimal128(value));
    }
    
    @Override
    protected void doWriteJavaScript(final String value) {
        this.write(new BsonJavaScript(value));
    }
    
    @Override
    protected void doWriteJavaScriptWithScope(final String value) {
        this.setContext(new Context(new BsonString(value), BsonContextType.JAVASCRIPT_WITH_SCOPE, this.getContext()));
    }
    
    @Override
    protected void doWriteMaxKey() {
        this.write(new BsonMaxKey());
    }
    
    @Override
    protected void doWriteMinKey() {
        this.write(new BsonMinKey());
    }
    
    public void doWriteNull() {
        this.write(BsonNull.VALUE);
    }
    
    public void doWriteObjectId(final ObjectId value) {
        this.write(new BsonObjectId(value));
    }
    
    public void doWriteRegularExpression(final BsonRegularExpression value) {
        this.write(value);
    }
    
    public void doWriteString(final String value) {
        this.write(new BsonString(value));
    }
    
    public void doWriteSymbol(final String value) {
        this.write(new BsonSymbol(value));
    }
    
    public void doWriteTimestamp(final BsonTimestamp value) {
        this.write(value);
    }
    
    public void doWriteUndefined() {
        this.write(new BsonUndefined());
    }
    
    @Override
    public void flush() {
    }
    
    @Override
    protected Context getContext() {
        return (Context)super.getContext();
    }
    
    private void write(final BsonValue value) {
        this.getContext().add(value);
    }
    
    private class Context extends AbstractBsonWriter.Context
    {
        private BsonValue container;
        
        public Context(final BsonValue container, final BsonContextType contextType, final Context parent) {
            super(parent, contextType);
            this.container = container;
        }
        
        public Context() {
            super(null, BsonContextType.TOP_LEVEL);
        }
        
        void add(final BsonValue value) {
            if (this.container instanceof BsonArray) {
                ((BsonArray)this.container).add(value);
            }
            else {
                ((BsonDocument)this.container).put(BsonDocumentWriter.this.getName(), value);
            }
        }
    }
}
