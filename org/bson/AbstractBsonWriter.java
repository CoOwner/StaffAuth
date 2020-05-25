package org.bson;

import java.io.*;
import org.bson.types.*;
import java.util.*;

public abstract class AbstractBsonWriter implements BsonWriter, Closeable
{
    private final BsonWriterSettings settings;
    private final Stack<FieldNameValidator> fieldNameValidatorStack;
    private State state;
    private Context context;
    private int serializationDepth;
    private boolean closed;
    
    protected AbstractBsonWriter(final BsonWriterSettings settings) {
        this(settings, new NoOpFieldNameValidator());
    }
    
    protected AbstractBsonWriter(final BsonWriterSettings settings, final FieldNameValidator validator) {
        this.fieldNameValidatorStack = new Stack<FieldNameValidator>();
        if (validator == null) {
            throw new IllegalArgumentException("Validator can not be null");
        }
        this.settings = settings;
        this.fieldNameValidatorStack.push(validator);
        this.state = State.INITIAL;
    }
    
    protected String getName() {
        return this.context.name;
    }
    
    protected boolean isClosed() {
        return this.closed;
    }
    
    protected void setState(final State state) {
        this.state = state;
    }
    
    protected State getState() {
        return this.state;
    }
    
    protected Context getContext() {
        return this.context;
    }
    
    protected void setContext(final Context context) {
        this.context = context;
    }
    
    protected abstract void doWriteStartDocument();
    
    protected abstract void doWriteEndDocument();
    
    protected abstract void doWriteStartArray();
    
    protected abstract void doWriteEndArray();
    
    protected abstract void doWriteBinaryData(final BsonBinary p0);
    
    protected abstract void doWriteBoolean(final boolean p0);
    
    protected abstract void doWriteDateTime(final long p0);
    
    protected abstract void doWriteDBPointer(final BsonDbPointer p0);
    
    protected abstract void doWriteDouble(final double p0);
    
    protected abstract void doWriteInt32(final int p0);
    
    protected abstract void doWriteInt64(final long p0);
    
    protected abstract void doWriteDecimal128(final Decimal128 p0);
    
    protected abstract void doWriteJavaScript(final String p0);
    
    protected abstract void doWriteJavaScriptWithScope(final String p0);
    
    protected abstract void doWriteMaxKey();
    
    protected abstract void doWriteMinKey();
    
    protected abstract void doWriteNull();
    
    protected abstract void doWriteObjectId(final ObjectId p0);
    
    protected abstract void doWriteRegularExpression(final BsonRegularExpression p0);
    
    protected abstract void doWriteString(final String p0);
    
    protected abstract void doWriteSymbol(final String p0);
    
    protected abstract void doWriteTimestamp(final BsonTimestamp p0);
    
    protected abstract void doWriteUndefined();
    
    @Override
    public void writeStartDocument(final String name) {
        this.writeName(name);
        this.writeStartDocument();
    }
    
    @Override
    public void writeStartDocument() {
        this.checkPreconditions("writeStartDocument", State.INITIAL, State.VALUE, State.SCOPE_DOCUMENT, State.DONE);
        if (this.context != null && this.context.name != null) {
            this.fieldNameValidatorStack.push(this.fieldNameValidatorStack.peek().getValidatorForField(this.getName()));
        }
        ++this.serializationDepth;
        if (this.serializationDepth > this.settings.getMaxSerializationDepth()) {
            throw new BsonSerializationException("Maximum serialization depth exceeded (does the object being serialized have a circular reference?).");
        }
        this.doWriteStartDocument();
        this.setState(State.NAME);
    }
    
    @Override
    public void writeEndDocument() {
        this.checkPreconditions("writeEndDocument", State.NAME);
        final BsonContextType contextType = this.getContext().getContextType();
        if (contextType != BsonContextType.DOCUMENT && contextType != BsonContextType.SCOPE_DOCUMENT) {
            this.throwInvalidContextType("WriteEndDocument", contextType, BsonContextType.DOCUMENT, BsonContextType.SCOPE_DOCUMENT);
        }
        if (this.context.getParentContext() != null && this.context.getParentContext().name != null) {
            this.fieldNameValidatorStack.pop();
        }
        --this.serializationDepth;
        this.doWriteEndDocument();
        if (this.getContext() == null || this.getContext().getContextType() == BsonContextType.TOP_LEVEL) {
            this.setState(State.DONE);
        }
        else {
            this.setState(this.getNextState());
        }
    }
    
    @Override
    public void writeStartArray(final String name) {
        this.writeName(name);
        this.writeStartArray();
    }
    
    @Override
    public void writeStartArray() {
        this.checkPreconditions("writeStartArray", State.VALUE);
        if (this.context != null && this.context.name != null) {
            this.fieldNameValidatorStack.push(this.fieldNameValidatorStack.peek().getValidatorForField(this.getName()));
        }
        ++this.serializationDepth;
        if (this.serializationDepth > this.settings.getMaxSerializationDepth()) {
            throw new BsonSerializationException("Maximum serialization depth exceeded (does the object being serialized have a circular reference?).");
        }
        this.doWriteStartArray();
        this.setState(State.VALUE);
    }
    
    @Override
    public void writeEndArray() {
        this.checkPreconditions("writeEndArray", State.VALUE);
        if (this.getContext().getContextType() != BsonContextType.ARRAY) {
            this.throwInvalidContextType("WriteEndArray", this.getContext().getContextType(), BsonContextType.ARRAY);
        }
        if (this.context.getParentContext() != null && this.context.getParentContext().name != null) {
            this.fieldNameValidatorStack.pop();
        }
        --this.serializationDepth;
        this.doWriteEndArray();
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeBinaryData(final String name, final BsonBinary binary) {
        this.writeName(name);
        this.writeBinaryData(binary);
    }
    
    @Override
    public void writeBinaryData(final BsonBinary binary) {
        this.checkPreconditions("writeBinaryData", State.VALUE, State.INITIAL);
        this.doWriteBinaryData(binary);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeBoolean(final String name, final boolean value) {
        this.writeName(name);
        this.writeBoolean(value);
    }
    
    @Override
    public void writeBoolean(final boolean value) {
        this.checkPreconditions("writeBoolean", State.VALUE, State.INITIAL);
        this.doWriteBoolean(value);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeDateTime(final String name, final long value) {
        this.writeName(name);
        this.writeDateTime(value);
    }
    
    @Override
    public void writeDateTime(final long value) {
        this.checkPreconditions("writeDateTime", State.VALUE, State.INITIAL);
        this.doWriteDateTime(value);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeDBPointer(final String name, final BsonDbPointer value) {
        this.writeName(name);
        this.writeDBPointer(value);
    }
    
    @Override
    public void writeDBPointer(final BsonDbPointer value) {
        this.checkPreconditions("writeDBPointer", State.VALUE, State.INITIAL);
        this.doWriteDBPointer(value);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeDouble(final String name, final double value) {
        this.writeName(name);
        this.writeDouble(value);
    }
    
    @Override
    public void writeDouble(final double value) {
        this.checkPreconditions("writeDBPointer", State.VALUE, State.INITIAL);
        this.doWriteDouble(value);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeInt32(final String name, final int value) {
        this.writeName(name);
        this.writeInt32(value);
    }
    
    @Override
    public void writeInt32(final int value) {
        this.checkPreconditions("writeInt32", State.VALUE);
        this.doWriteInt32(value);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeInt64(final String name, final long value) {
        this.writeName(name);
        this.writeInt64(value);
    }
    
    @Override
    public void writeInt64(final long value) {
        this.checkPreconditions("writeInt64", State.VALUE);
        this.doWriteInt64(value);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeDecimal128(final Decimal128 value) {
        this.checkPreconditions("writeInt64", State.VALUE);
        this.doWriteDecimal128(value);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeDecimal128(final String name, final Decimal128 value) {
        this.writeName(name);
        this.writeDecimal128(value);
    }
    
    @Override
    public void writeJavaScript(final String name, final String code) {
        this.writeName(name);
        this.writeJavaScript(code);
    }
    
    @Override
    public void writeJavaScript(final String code) {
        this.checkPreconditions("writeJavaScript", State.VALUE);
        this.doWriteJavaScript(code);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeJavaScriptWithScope(final String name, final String code) {
        this.writeName(name);
        this.writeJavaScriptWithScope(code);
    }
    
    @Override
    public void writeJavaScriptWithScope(final String code) {
        this.checkPreconditions("writeJavaScriptWithScope", State.VALUE);
        this.doWriteJavaScriptWithScope(code);
        this.setState(State.SCOPE_DOCUMENT);
    }
    
    @Override
    public void writeMaxKey(final String name) {
        this.writeName(name);
        this.writeMaxKey();
    }
    
    @Override
    public void writeMaxKey() {
        this.checkPreconditions("writeMaxKey", State.VALUE);
        this.doWriteMaxKey();
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeMinKey(final String name) {
        this.writeName(name);
        this.writeMinKey();
    }
    
    @Override
    public void writeMinKey() {
        this.checkPreconditions("writeMinKey", State.VALUE);
        this.doWriteMinKey();
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeName(final String name) {
        if (this.state != State.NAME) {
            this.throwInvalidState("WriteName", State.NAME);
        }
        if (name == null) {
            throw new IllegalArgumentException("BSON field name can not be null");
        }
        if (!this.fieldNameValidatorStack.peek().validate(name)) {
            throw new IllegalArgumentException(String.format("Invalid BSON field name %s", name));
        }
        this.context.name = name;
        this.state = State.VALUE;
    }
    
    @Override
    public void writeNull(final String name) {
        this.writeName(name);
        this.writeNull();
    }
    
    @Override
    public void writeNull() {
        this.checkPreconditions("writeNull", State.VALUE);
        this.doWriteNull();
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeObjectId(final String name, final ObjectId objectId) {
        this.writeName(name);
        this.writeObjectId(objectId);
    }
    
    @Override
    public void writeObjectId(final ObjectId objectId) {
        this.checkPreconditions("writeObjectId", State.VALUE);
        this.doWriteObjectId(objectId);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeRegularExpression(final String name, final BsonRegularExpression regularExpression) {
        this.writeName(name);
        this.writeRegularExpression(regularExpression);
    }
    
    @Override
    public void writeRegularExpression(final BsonRegularExpression regularExpression) {
        this.checkPreconditions("writeRegularExpression", State.VALUE);
        this.doWriteRegularExpression(regularExpression);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeString(final String name, final String value) {
        this.writeName(name);
        this.writeString(value);
    }
    
    @Override
    public void writeString(final String value) {
        this.checkPreconditions("writeString", State.VALUE);
        this.doWriteString(value);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeSymbol(final String name, final String value) {
        this.writeName(name);
        this.writeSymbol(value);
    }
    
    @Override
    public void writeSymbol(final String value) {
        this.checkPreconditions("writeSymbol", State.VALUE);
        this.doWriteSymbol(value);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeTimestamp(final String name, final BsonTimestamp value) {
        this.writeName(name);
        this.writeTimestamp(value);
    }
    
    @Override
    public void writeTimestamp(final BsonTimestamp value) {
        this.checkPreconditions("writeTimestamp", State.VALUE);
        this.doWriteTimestamp(value);
        this.setState(this.getNextState());
    }
    
    @Override
    public void writeUndefined(final String name) {
        this.writeName(name);
        this.writeUndefined();
    }
    
    @Override
    public void writeUndefined() {
        this.checkPreconditions("writeUndefined", State.VALUE);
        this.doWriteUndefined();
        this.setState(this.getNextState());
    }
    
    protected State getNextState() {
        if (this.getContext().getContextType() == BsonContextType.ARRAY) {
            return State.VALUE;
        }
        return State.NAME;
    }
    
    protected boolean checkState(final State[] validStates) {
        for (final State cur : validStates) {
            if (cur == this.getState()) {
                return true;
            }
        }
        return false;
    }
    
    protected void checkPreconditions(final String methodName, final State... validStates) {
        if (this.isClosed()) {
            throw new IllegalStateException("BsonWriter is closed");
        }
        if (!this.checkState(validStates)) {
            this.throwInvalidState(methodName, validStates);
        }
    }
    
    protected void throwInvalidContextType(final String methodName, final BsonContextType actualContextType, final BsonContextType... validContextTypes) {
        final String validContextTypesString = StringUtils.join(" or ", Arrays.asList(validContextTypes));
        throw new BsonInvalidOperationException(String.format("%s can only be called when ContextType is %s, not when ContextType is %s.", methodName, validContextTypesString, actualContextType));
    }
    
    protected void throwInvalidState(final String methodName, final State... validStates) {
        if ((this.state == State.INITIAL || this.state == State.SCOPE_DOCUMENT || this.state == State.DONE) && !methodName.startsWith("end") && !methodName.equals("writeName")) {
            String typeName = methodName.substring(5);
            if (typeName.startsWith("start")) {
                typeName = typeName.substring(5);
            }
            String article = "A";
            if (Arrays.asList('A', 'E', 'I', 'O', 'U').contains(typeName.charAt(0))) {
                article = "An";
            }
            throw new BsonInvalidOperationException(String.format("%s %s value cannot be written to the root level of a BSON document.", article, typeName));
        }
        final String validStatesString = StringUtils.join(" or ", Arrays.asList(validStates));
        throw new BsonInvalidOperationException(String.format("%s can only be called when State is %s, not when State is %s", methodName, validStatesString, this.state));
    }
    
    @Override
    public void close() {
        this.closed = true;
    }
    
    @Override
    public void pipe(final BsonReader reader) {
        this.pipeDocument(reader);
    }
    
    private void pipeDocument(final BsonReader reader) {
        reader.readStartDocument();
        this.writeStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            this.writeName(reader.readName());
            this.pipeValue(reader);
        }
        reader.readEndDocument();
        this.writeEndDocument();
    }
    
    private void pipeArray(final BsonReader reader) {
        reader.readStartArray();
        this.writeStartArray();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            this.pipeValue(reader);
        }
        reader.readEndArray();
        this.writeEndArray();
    }
    
    private void pipeJavascriptWithScope(final BsonReader reader) {
        this.writeJavaScriptWithScope(reader.readJavaScriptWithScope());
        this.pipeDocument(reader);
    }
    
    private void pipeValue(final BsonReader reader) {
        switch (reader.getCurrentBsonType()) {
            case DOCUMENT: {
                this.pipeDocument(reader);
                break;
            }
            case ARRAY: {
                this.pipeArray(reader);
                break;
            }
            case DOUBLE: {
                this.writeDouble(reader.readDouble());
                break;
            }
            case STRING: {
                this.writeString(reader.readString());
                break;
            }
            case BINARY: {
                this.writeBinaryData(reader.readBinaryData());
                break;
            }
            case UNDEFINED: {
                reader.readUndefined();
                this.writeUndefined();
                break;
            }
            case OBJECT_ID: {
                this.writeObjectId(reader.readObjectId());
                break;
            }
            case BOOLEAN: {
                this.writeBoolean(reader.readBoolean());
                break;
            }
            case DATE_TIME: {
                this.writeDateTime(reader.readDateTime());
                break;
            }
            case NULL: {
                reader.readNull();
                this.writeNull();
                break;
            }
            case REGULAR_EXPRESSION: {
                this.writeRegularExpression(reader.readRegularExpression());
                break;
            }
            case JAVASCRIPT: {
                this.writeJavaScript(reader.readJavaScript());
                break;
            }
            case SYMBOL: {
                this.writeSymbol(reader.readSymbol());
                break;
            }
            case JAVASCRIPT_WITH_SCOPE: {
                this.pipeJavascriptWithScope(reader);
                break;
            }
            case INT32: {
                this.writeInt32(reader.readInt32());
                break;
            }
            case TIMESTAMP: {
                this.writeTimestamp(reader.readTimestamp());
                break;
            }
            case INT64: {
                this.writeInt64(reader.readInt64());
                break;
            }
            case DECIMAL128: {
                this.writeDecimal128(reader.readDecimal128());
                break;
            }
            case MIN_KEY: {
                reader.readMinKey();
                this.writeMinKey();
                break;
            }
            case DB_POINTER: {
                this.writeDBPointer(reader.readDBPointer());
                break;
            }
            case MAX_KEY: {
                reader.readMaxKey();
                this.writeMaxKey();
                break;
            }
            default: {
                throw new IllegalArgumentException("unhandled BSON type: " + reader.getCurrentBsonType());
            }
        }
    }
    
    public enum State
    {
        INITIAL, 
        NAME, 
        VALUE, 
        SCOPE_DOCUMENT, 
        DONE, 
        CLOSED;
    }
    
    public class Context
    {
        private final Context parentContext;
        private final BsonContextType contextType;
        private String name;
        
        public Context(final Context from) {
            this.parentContext = from.parentContext;
            this.contextType = from.contextType;
        }
        
        public Context(final Context parentContext, final BsonContextType contextType) {
            this.parentContext = parentContext;
            this.contextType = contextType;
        }
        
        public Context getParentContext() {
            return this.parentContext;
        }
        
        public BsonContextType getContextType() {
            return this.contextType;
        }
        
        public Context copy() {
            return new Context(this);
        }
    }
    
    protected class Mark
    {
        private final Context markedContext;
        private final State markedState;
        private final String currentName;
        private final int serializationDepth;
        
        protected Mark() {
            this.markedContext = AbstractBsonWriter.this.context.copy();
            this.markedState = AbstractBsonWriter.this.state;
            this.currentName = AbstractBsonWriter.this.context.name;
            this.serializationDepth = AbstractBsonWriter.this.serializationDepth;
        }
        
        protected void reset() {
            AbstractBsonWriter.this.setContext(this.markedContext);
            AbstractBsonWriter.this.setState(this.markedState);
            AbstractBsonWriter.this.context.name = this.currentName;
            AbstractBsonWriter.this.serializationDepth = this.serializationDepth;
        }
    }
}
