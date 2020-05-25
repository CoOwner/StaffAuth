package org.bson;

import java.util.*;
import org.bson.types.*;
import org.bson.io.*;

public class BsonBinaryWriter extends AbstractBsonWriter
{
    private final BsonBinaryWriterSettings binaryWriterSettings;
    private final BsonOutput bsonOutput;
    private final Stack<Integer> maxDocumentSizeStack;
    private Mark mark;
    
    public BsonBinaryWriter(final BsonOutput bsonOutput, final FieldNameValidator validator) {
        this(new BsonWriterSettings(), new BsonBinaryWriterSettings(), bsonOutput, validator);
    }
    
    public BsonBinaryWriter(final BsonOutput bsonOutput) {
        this(new BsonWriterSettings(), new BsonBinaryWriterSettings(), bsonOutput);
    }
    
    public BsonBinaryWriter(final BsonWriterSettings settings, final BsonBinaryWriterSettings binaryWriterSettings, final BsonOutput bsonOutput) {
        this(settings, binaryWriterSettings, bsonOutput, new NoOpFieldNameValidator());
    }
    
    public BsonBinaryWriter(final BsonWriterSettings settings, final BsonBinaryWriterSettings binaryWriterSettings, final BsonOutput bsonOutput, final FieldNameValidator validator) {
        super(settings, validator);
        this.maxDocumentSizeStack = new Stack<Integer>();
        this.binaryWriterSettings = binaryWriterSettings;
        this.bsonOutput = bsonOutput;
        this.maxDocumentSizeStack.push(binaryWriterSettings.getMaxDocumentSize());
    }
    
    @Override
    public void close() {
        super.close();
    }
    
    public BsonOutput getBsonOutput() {
        return this.bsonOutput;
    }
    
    @Override
    public void flush() {
    }
    
    @Override
    protected Context getContext() {
        return (Context)super.getContext();
    }
    
    @Override
    protected void doWriteStartDocument() {
        if (this.getState() == State.VALUE) {
            this.bsonOutput.writeByte(BsonType.DOCUMENT.getValue());
            this.writeCurrentName();
        }
        this.setContext(new Context(this.getContext(), BsonContextType.DOCUMENT, this.bsonOutput.getPosition()));
        this.bsonOutput.writeInt32(0);
    }
    
    @Override
    protected void doWriteEndDocument() {
        this.bsonOutput.writeByte(0);
        this.backpatchSize();
        this.setContext(this.getContext().getParentContext());
        if (this.getContext() != null && this.getContext().getContextType() == BsonContextType.JAVASCRIPT_WITH_SCOPE) {
            this.backpatchSize();
            this.setContext(this.getContext().getParentContext());
        }
    }
    
    @Override
    protected void doWriteStartArray() {
        this.bsonOutput.writeByte(BsonType.ARRAY.getValue());
        this.writeCurrentName();
        this.setContext(new Context(this.getContext(), BsonContextType.ARRAY, this.bsonOutput.getPosition()));
        this.bsonOutput.writeInt32(0);
    }
    
    @Override
    protected void doWriteEndArray() {
        this.bsonOutput.writeByte(0);
        this.backpatchSize();
        this.setContext(this.getContext().getParentContext());
    }
    
    @Override
    protected void doWriteBinaryData(final BsonBinary value) {
        this.bsonOutput.writeByte(BsonType.BINARY.getValue());
        this.writeCurrentName();
        int totalLen = value.getData().length;
        if (value.getType() == BsonBinarySubType.OLD_BINARY.getValue()) {
            totalLen += 4;
        }
        this.bsonOutput.writeInt32(totalLen);
        this.bsonOutput.writeByte(value.getType());
        if (value.getType() == BsonBinarySubType.OLD_BINARY.getValue()) {
            this.bsonOutput.writeInt32(totalLen - 4);
        }
        this.bsonOutput.writeBytes(value.getData());
    }
    
    public void doWriteBoolean(final boolean value) {
        this.bsonOutput.writeByte(BsonType.BOOLEAN.getValue());
        this.writeCurrentName();
        this.bsonOutput.writeByte(value ? 1 : 0);
    }
    
    @Override
    protected void doWriteDateTime(final long value) {
        this.bsonOutput.writeByte(BsonType.DATE_TIME.getValue());
        this.writeCurrentName();
        this.bsonOutput.writeInt64(value);
    }
    
    @Override
    protected void doWriteDBPointer(final BsonDbPointer value) {
        this.bsonOutput.writeByte(BsonType.DB_POINTER.getValue());
        this.writeCurrentName();
        this.bsonOutput.writeString(value.getNamespace());
        this.bsonOutput.writeBytes(value.getId().toByteArray());
    }
    
    @Override
    protected void doWriteDouble(final double value) {
        this.bsonOutput.writeByte(BsonType.DOUBLE.getValue());
        this.writeCurrentName();
        this.bsonOutput.writeDouble(value);
    }
    
    @Override
    protected void doWriteInt32(final int value) {
        this.bsonOutput.writeByte(BsonType.INT32.getValue());
        this.writeCurrentName();
        this.bsonOutput.writeInt32(value);
    }
    
    @Override
    protected void doWriteInt64(final long value) {
        this.bsonOutput.writeByte(BsonType.INT64.getValue());
        this.writeCurrentName();
        this.bsonOutput.writeInt64(value);
    }
    
    @Override
    protected void doWriteDecimal128(final Decimal128 value) {
        this.bsonOutput.writeByte(BsonType.DECIMAL128.getValue());
        this.writeCurrentName();
        this.bsonOutput.writeInt64(value.getLow());
        this.bsonOutput.writeInt64(value.getHigh());
    }
    
    @Override
    protected void doWriteJavaScript(final String value) {
        this.bsonOutput.writeByte(BsonType.JAVASCRIPT.getValue());
        this.writeCurrentName();
        this.bsonOutput.writeString(value);
    }
    
    @Override
    protected void doWriteJavaScriptWithScope(final String value) {
        this.bsonOutput.writeByte(BsonType.JAVASCRIPT_WITH_SCOPE.getValue());
        this.writeCurrentName();
        this.setContext(new Context(this.getContext(), BsonContextType.JAVASCRIPT_WITH_SCOPE, this.bsonOutput.getPosition()));
        this.bsonOutput.writeInt32(0);
        this.bsonOutput.writeString(value);
    }
    
    @Override
    protected void doWriteMaxKey() {
        this.bsonOutput.writeByte(BsonType.MAX_KEY.getValue());
        this.writeCurrentName();
    }
    
    @Override
    protected void doWriteMinKey() {
        this.bsonOutput.writeByte(BsonType.MIN_KEY.getValue());
        this.writeCurrentName();
    }
    
    public void doWriteNull() {
        this.bsonOutput.writeByte(BsonType.NULL.getValue());
        this.writeCurrentName();
    }
    
    public void doWriteObjectId(final ObjectId value) {
        this.bsonOutput.writeByte(BsonType.OBJECT_ID.getValue());
        this.writeCurrentName();
        this.bsonOutput.writeBytes(value.toByteArray());
    }
    
    public void doWriteRegularExpression(final BsonRegularExpression value) {
        this.bsonOutput.writeByte(BsonType.REGULAR_EXPRESSION.getValue());
        this.writeCurrentName();
        this.bsonOutput.writeCString(value.getPattern());
        this.bsonOutput.writeCString(value.getOptions());
    }
    
    public void doWriteString(final String value) {
        this.bsonOutput.writeByte(BsonType.STRING.getValue());
        this.writeCurrentName();
        this.bsonOutput.writeString(value);
    }
    
    public void doWriteSymbol(final String value) {
        this.bsonOutput.writeByte(BsonType.SYMBOL.getValue());
        this.writeCurrentName();
        this.bsonOutput.writeString(value);
    }
    
    public void doWriteTimestamp(final BsonTimestamp value) {
        this.bsonOutput.writeByte(BsonType.TIMESTAMP.getValue());
        this.writeCurrentName();
        this.bsonOutput.writeInt32(value.getInc());
        this.bsonOutput.writeInt32(value.getTime());
    }
    
    public void doWriteUndefined() {
        this.bsonOutput.writeByte(BsonType.UNDEFINED.getValue());
        this.writeCurrentName();
    }
    
    @Override
    public void pipe(final BsonReader reader) {
        if (reader instanceof BsonBinaryReader) {
            final BsonBinaryReader binaryReader = (BsonBinaryReader)reader;
            if (this.getState() == State.VALUE) {
                this.bsonOutput.writeByte(BsonType.DOCUMENT.getValue());
                this.writeCurrentName();
            }
            final BsonInput bsonInput = binaryReader.getBsonInput();
            final int size = bsonInput.readInt32();
            if (size < 5) {
                throw new BsonSerializationException("Document size must be at least 5");
            }
            this.bsonOutput.writeInt32(size);
            final byte[] bytes = new byte[size - 4];
            bsonInput.readBytes(bytes);
            this.bsonOutput.writeBytes(bytes);
            binaryReader.setState(AbstractBsonReader.State.TYPE);
            if (this.getContext() == null) {
                this.setState(State.DONE);
            }
            else {
                if (this.getContext().getContextType() == BsonContextType.JAVASCRIPT_WITH_SCOPE) {
                    this.backpatchSize();
                    this.setContext(this.getContext().getParentContext());
                }
                this.setState(this.getNextState());
            }
        }
        else {
            super.pipe(reader);
        }
    }
    
    public void pushMaxDocumentSize(final int maxDocumentSize) {
        this.maxDocumentSizeStack.push(maxDocumentSize);
    }
    
    public void popMaxDocumentSize() {
        this.maxDocumentSizeStack.pop();
    }
    
    public void mark() {
        this.mark = new Mark();
    }
    
    public void reset() {
        if (this.mark == null) {
            throw new IllegalStateException("Can not reset without first marking");
        }
        this.mark.reset();
        this.mark = null;
    }
    
    private void writeCurrentName() {
        if (this.getContext().getContextType() == BsonContextType.ARRAY) {
            this.bsonOutput.writeCString(Integer.toString(this.getContext().index++));
        }
        else {
            this.bsonOutput.writeCString(this.getName());
        }
    }
    
    private void backpatchSize() {
        final int size = this.bsonOutput.getPosition() - this.getContext().startPosition;
        if (size > this.maxDocumentSizeStack.peek()) {
            throw new BsonSerializationException(String.format("Document size of %d is larger than maximum of %d.", size, this.maxDocumentSizeStack.peek()));
        }
        this.bsonOutput.writeInt32(this.bsonOutput.getPosition() - size, size);
    }
    
    protected class Context extends AbstractBsonWriter.Context
    {
        private final int startPosition;
        private int index;
        
        public Context(final Context parentContext, final BsonContextType contextType, final int startPosition) {
            super(parentContext, contextType);
            this.startPosition = startPosition;
        }
        
        public Context(final Context from) {
            super(from);
            this.startPosition = from.startPosition;
            this.index = from.index;
        }
        
        @Override
        public Context getParentContext() {
            return (Context)super.getParentContext();
        }
        
        @Override
        public Context copy() {
            return new Context(this);
        }
    }
    
    protected class Mark extends AbstractBsonWriter.Mark
    {
        private final int position;
        
        protected Mark() {
            this.position = BsonBinaryWriter.this.bsonOutput.getPosition();
        }
        
        @Override
        protected void reset() {
            super.reset();
            BsonBinaryWriter.this.bsonOutput.truncateToPosition(BsonBinaryWriter.this.mark.position);
        }
    }
}
