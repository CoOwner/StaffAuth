package org.bson;

import java.nio.*;
import org.bson.assertions.*;
import org.bson.io.*;
import org.bson.types.*;

public class BsonBinaryReader extends AbstractBsonReader
{
    private final BsonInput bsonInput;
    private Mark mark;
    
    public BsonBinaryReader(final ByteBuffer byteBuffer) {
        this(new ByteBufferBsonInput(new ByteBufNIO(Assertions.notNull("byteBuffer", byteBuffer))));
    }
    
    public BsonBinaryReader(final BsonInput bsonInput) {
        if (bsonInput == null) {
            throw new IllegalArgumentException("bsonInput is null");
        }
        this.bsonInput = bsonInput;
        this.setContext(new Context(null, BsonContextType.TOP_LEVEL, 0, 0));
    }
    
    @Override
    public void close() {
        super.close();
    }
    
    public BsonInput getBsonInput() {
        return this.bsonInput;
    }
    
    @Override
    public BsonType readBsonType() {
        if (this.isClosed()) {
            throw new IllegalStateException("BSONBinaryWriter");
        }
        if (this.getState() == State.INITIAL || this.getState() == State.DONE || this.getState() == State.SCOPE_DOCUMENT) {
            this.setCurrentBsonType(BsonType.DOCUMENT);
            this.setState(State.VALUE);
            return this.getCurrentBsonType();
        }
        if (this.getState() != State.TYPE) {
            this.throwInvalidState("ReadBSONType", State.TYPE);
        }
        final byte bsonTypeByte = this.bsonInput.readByte();
        final BsonType bsonType = BsonType.findByValue(bsonTypeByte);
        if (bsonType == null) {
            final String name = this.bsonInput.readCString();
            throw new BsonSerializationException(String.format("Detected unknown BSON type \"\\x%x\" for fieldname \"%s\". Are you using the latest driver version?", bsonTypeByte, name));
        }
        this.setCurrentBsonType(bsonType);
        if (this.getCurrentBsonType() != BsonType.END_OF_DOCUMENT) {
            switch (this.getContext().getContextType()) {
                case ARRAY: {
                    this.bsonInput.skipCString();
                    this.setState(State.VALUE);
                    break;
                }
                case DOCUMENT:
                case SCOPE_DOCUMENT: {
                    this.setCurrentName(this.bsonInput.readCString());
                    this.setState(State.NAME);
                    break;
                }
                default: {
                    throw new BSONException("Unexpected ContextType.");
                }
            }
            return this.getCurrentBsonType();
        }
        switch (this.getContext().getContextType()) {
            case ARRAY: {
                this.setState(State.END_OF_ARRAY);
                return BsonType.END_OF_DOCUMENT;
            }
            case DOCUMENT:
            case SCOPE_DOCUMENT: {
                this.setState(State.END_OF_DOCUMENT);
                return BsonType.END_OF_DOCUMENT;
            }
            default: {
                throw new BsonSerializationException(String.format("BSONType EndOfDocument is not valid when ContextType is %s.", this.getContext().getContextType()));
            }
        }
    }
    
    @Override
    protected BsonBinary doReadBinaryData() {
        int numBytes = this.readSize();
        final byte type = this.bsonInput.readByte();
        if (type == BsonBinarySubType.OLD_BINARY.getValue()) {
            final int repeatedNumBytes = this.bsonInput.readInt32();
            if (repeatedNumBytes != numBytes - 4) {
                throw new BsonSerializationException("Binary sub type OldBinary has inconsistent sizes");
            }
            numBytes -= 4;
        }
        final byte[] bytes = new byte[numBytes];
        this.bsonInput.readBytes(bytes);
        return new BsonBinary(type, bytes);
    }
    
    @Override
    protected byte doPeekBinarySubType() {
        this.mark();
        this.readSize();
        final byte type = this.bsonInput.readByte();
        this.reset();
        return type;
    }
    
    @Override
    protected int doPeekBinarySize() {
        this.mark();
        final int size = this.readSize();
        this.reset();
        return size;
    }
    
    @Override
    protected boolean doReadBoolean() {
        final byte booleanByte = this.bsonInput.readByte();
        if (booleanByte != 0 && booleanByte != 1) {
            throw new BsonSerializationException(String.format("Expected a boolean value but found %d", booleanByte));
        }
        return booleanByte == 1;
    }
    
    @Override
    protected long doReadDateTime() {
        return this.bsonInput.readInt64();
    }
    
    @Override
    protected double doReadDouble() {
        return this.bsonInput.readDouble();
    }
    
    @Override
    protected int doReadInt32() {
        return this.bsonInput.readInt32();
    }
    
    @Override
    protected long doReadInt64() {
        return this.bsonInput.readInt64();
    }
    
    public Decimal128 doReadDecimal128() {
        final long low = this.bsonInput.readInt64();
        final long high = this.bsonInput.readInt64();
        return Decimal128.fromIEEE754BIDEncoding(high, low);
    }
    
    @Override
    protected String doReadJavaScript() {
        return this.bsonInput.readString();
    }
    
    @Override
    protected String doReadJavaScriptWithScope() {
        final int startPosition = this.bsonInput.getPosition();
        final int size = this.readSize();
        this.setContext(new Context(this.getContext(), BsonContextType.JAVASCRIPT_WITH_SCOPE, startPosition, size));
        return this.bsonInput.readString();
    }
    
    @Override
    protected void doReadMaxKey() {
    }
    
    @Override
    protected void doReadMinKey() {
    }
    
    @Override
    protected void doReadNull() {
    }
    
    @Override
    protected ObjectId doReadObjectId() {
        return this.bsonInput.readObjectId();
    }
    
    @Override
    protected BsonRegularExpression doReadRegularExpression() {
        return new BsonRegularExpression(this.bsonInput.readCString(), this.bsonInput.readCString());
    }
    
    @Override
    protected BsonDbPointer doReadDBPointer() {
        return new BsonDbPointer(this.bsonInput.readString(), this.bsonInput.readObjectId());
    }
    
    @Override
    protected String doReadString() {
        return this.bsonInput.readString();
    }
    
    @Override
    protected String doReadSymbol() {
        return this.bsonInput.readString();
    }
    
    @Override
    protected BsonTimestamp doReadTimestamp() {
        final int increment = this.bsonInput.readInt32();
        final int time = this.bsonInput.readInt32();
        return new BsonTimestamp(time, increment);
    }
    
    @Override
    protected void doReadUndefined() {
    }
    
    public void doReadStartArray() {
        final int startPosition = this.bsonInput.getPosition();
        final int size = this.readSize();
        this.setContext(new Context(this.getContext(), BsonContextType.ARRAY, startPosition, size));
    }
    
    @Override
    protected void doReadStartDocument() {
        final BsonContextType contextType = (this.getState() == State.SCOPE_DOCUMENT) ? BsonContextType.SCOPE_DOCUMENT : BsonContextType.DOCUMENT;
        final int startPosition = this.bsonInput.getPosition();
        final int size = this.readSize();
        this.setContext(new Context(this.getContext(), contextType, startPosition, size));
    }
    
    @Override
    protected void doReadEndArray() {
        this.setContext(this.getContext().popContext(this.bsonInput.getPosition()));
    }
    
    @Override
    protected void doReadEndDocument() {
        this.setContext(this.getContext().popContext(this.bsonInput.getPosition()));
        if (this.getContext().getContextType() == BsonContextType.JAVASCRIPT_WITH_SCOPE) {
            this.setContext(this.getContext().popContext(this.bsonInput.getPosition()));
        }
    }
    
    @Override
    protected void doSkipName() {
    }
    
    @Override
    protected void doSkipValue() {
        if (this.isClosed()) {
            throw new IllegalStateException("BSONBinaryWriter");
        }
        if (this.getState() != State.VALUE) {
            this.throwInvalidState("skipValue", State.VALUE);
        }
        int skip = 0;
        switch (this.getCurrentBsonType()) {
            case ARRAY: {
                skip = this.readSize() - 4;
                break;
            }
            case BINARY: {
                skip = this.readSize() + 1;
                break;
            }
            case BOOLEAN: {
                skip = 1;
                break;
            }
            case DATE_TIME: {
                skip = 8;
                break;
            }
            case DOCUMENT: {
                skip = this.readSize() - 4;
                break;
            }
            case DOUBLE: {
                skip = 8;
                break;
            }
            case INT32: {
                skip = 4;
                break;
            }
            case INT64: {
                skip = 8;
                break;
            }
            case DECIMAL128: {
                skip = 16;
                break;
            }
            case JAVASCRIPT: {
                skip = this.readSize();
                break;
            }
            case JAVASCRIPT_WITH_SCOPE: {
                skip = this.readSize() - 4;
                break;
            }
            case MAX_KEY: {
                skip = 0;
                break;
            }
            case MIN_KEY: {
                skip = 0;
                break;
            }
            case NULL: {
                skip = 0;
                break;
            }
            case OBJECT_ID: {
                skip = 12;
                break;
            }
            case REGULAR_EXPRESSION: {
                this.bsonInput.skipCString();
                this.bsonInput.skipCString();
                skip = 0;
                break;
            }
            case STRING: {
                skip = this.readSize();
                break;
            }
            case SYMBOL: {
                skip = this.readSize();
                break;
            }
            case TIMESTAMP: {
                skip = 8;
                break;
            }
            case UNDEFINED: {
                skip = 0;
                break;
            }
            case DB_POINTER: {
                skip = this.readSize() + 12;
                break;
            }
            default: {
                throw new BSONException("Unexpected BSON type: " + this.getCurrentBsonType());
            }
        }
        this.bsonInput.skip(skip);
        this.setState(State.TYPE);
    }
    
    private int readSize() {
        final int size = this.bsonInput.readInt32();
        if (size < 0) {
            final String message = String.format("Size %s is not valid because it is negative.", size);
            throw new BsonSerializationException(message);
        }
        return size;
    }
    
    @Override
    protected Context getContext() {
        return (Context)super.getContext();
    }
    
    @Override
    public void mark() {
        if (this.mark != null) {
            throw new BSONException("A mark already exists; it needs to be reset before creating a new one");
        }
        this.mark = new Mark();
    }
    
    @Override
    public void reset() {
        if (this.mark == null) {
            throw new BSONException("trying to reset a mark before creating it");
        }
        this.mark.reset();
        this.mark = null;
    }
    
    protected class Mark extends AbstractBsonReader.Mark
    {
        private final int startPosition;
        private final int size;
        
        protected Mark() {
            this.startPosition = BsonBinaryReader.this.getContext().startPosition;
            this.size = BsonBinaryReader.this.getContext().size;
            BsonBinaryReader.this.bsonInput.mark(Integer.MAX_VALUE);
        }
        
        @Override
        protected void reset() {
            super.reset();
            BsonBinaryReader.this.bsonInput.reset();
            BsonBinaryReader.this.setContext(new BsonBinaryReader.Context((BsonBinaryReader.Context)this.getParentContext(), this.getContextType(), this.startPosition, this.size));
        }
    }
    
    protected class Context extends AbstractBsonReader.Context
    {
        private final int startPosition;
        private final int size;
        
        Context(final Context parentContext, final BsonContextType contextType, final int startPosition, final int size) {
            super(parentContext, contextType);
            this.startPosition = startPosition;
            this.size = size;
        }
        
        Context popContext(final int position) {
            final int actualSize = position - this.startPosition;
            if (actualSize != this.size) {
                throw new BsonSerializationException(String.format("Expected size to be %d, not %d.", this.size, actualSize));
            }
            return this.getParentContext();
        }
        
        @Override
        protected Context getParentContext() {
            return (Context)super.getParentContext();
        }
    }
}
