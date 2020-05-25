package org.bson;

import org.bson.types.*;
import java.util.*;

public class BsonDocumentReader extends AbstractBsonReader
{
    private BsonValue currentValue;
    private Mark mark;
    
    public BsonDocumentReader(final BsonDocument document) {
        this.setContext(new Context(null, BsonContextType.TOP_LEVEL, document));
        this.currentValue = document;
    }
    
    @Override
    protected BsonBinary doReadBinaryData() {
        return this.currentValue.asBinary();
    }
    
    @Override
    protected byte doPeekBinarySubType() {
        return this.currentValue.asBinary().getType();
    }
    
    @Override
    protected int doPeekBinarySize() {
        return this.currentValue.asBinary().getData().length;
    }
    
    @Override
    protected boolean doReadBoolean() {
        return this.currentValue.asBoolean().getValue();
    }
    
    @Override
    protected long doReadDateTime() {
        return this.currentValue.asDateTime().getValue();
    }
    
    @Override
    protected double doReadDouble() {
        return this.currentValue.asDouble().getValue();
    }
    
    @Override
    protected void doReadEndArray() {
        this.setContext(this.getContext().getParentContext());
    }
    
    @Override
    protected void doReadEndDocument() {
        this.setContext(this.getContext().getParentContext());
        switch (this.getContext().getContextType()) {
            case ARRAY:
            case DOCUMENT: {
                this.setState(State.TYPE);
                break;
            }
            case TOP_LEVEL: {
                this.setState(State.DONE);
                break;
            }
            default: {
                throw new BSONException("Unexpected ContextType.");
            }
        }
    }
    
    @Override
    protected int doReadInt32() {
        return this.currentValue.asInt32().getValue();
    }
    
    @Override
    protected long doReadInt64() {
        return this.currentValue.asInt64().getValue();
    }
    
    public Decimal128 doReadDecimal128() {
        return this.currentValue.asDecimal128().getValue();
    }
    
    @Override
    protected String doReadJavaScript() {
        return this.currentValue.asJavaScript().getCode();
    }
    
    @Override
    protected String doReadJavaScriptWithScope() {
        return this.currentValue.asJavaScriptWithScope().getCode();
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
        return this.currentValue.asObjectId().getValue();
    }
    
    @Override
    protected BsonRegularExpression doReadRegularExpression() {
        return this.currentValue.asRegularExpression();
    }
    
    @Override
    protected BsonDbPointer doReadDBPointer() {
        return this.currentValue.asDBPointer();
    }
    
    @Override
    protected void doReadStartArray() {
        final BsonArray array = this.currentValue.asArray();
        this.setContext(new Context(this.getContext(), BsonContextType.ARRAY, array));
    }
    
    @Override
    protected void doReadStartDocument() {
        BsonDocument document;
        if (this.currentValue.getBsonType() == BsonType.JAVASCRIPT_WITH_SCOPE) {
            document = this.currentValue.asJavaScriptWithScope().getScope();
        }
        else {
            document = this.currentValue.asDocument();
        }
        this.setContext(new Context(this.getContext(), BsonContextType.DOCUMENT, document));
    }
    
    @Override
    protected String doReadString() {
        return this.currentValue.asString().getValue();
    }
    
    @Override
    protected String doReadSymbol() {
        return this.currentValue.asSymbol().getSymbol();
    }
    
    @Override
    protected BsonTimestamp doReadTimestamp() {
        return this.currentValue.asTimestamp();
    }
    
    @Override
    protected void doReadUndefined() {
    }
    
    @Override
    protected void doSkipName() {
    }
    
    @Override
    protected void doSkipValue() {
    }
    
    @Override
    public BsonType readBsonType() {
        if (this.getState() == State.INITIAL || this.getState() == State.SCOPE_DOCUMENT) {
            this.setCurrentBsonType(BsonType.DOCUMENT);
            this.setState(State.VALUE);
            return this.getCurrentBsonType();
        }
        if (this.getState() != State.TYPE) {
            this.throwInvalidState("ReadBSONType", State.TYPE);
        }
        switch (this.getContext().getContextType()) {
            case ARRAY: {
                this.currentValue = this.getContext().getNextValue();
                if (this.currentValue == null) {
                    this.setState(State.END_OF_ARRAY);
                    return BsonType.END_OF_DOCUMENT;
                }
                this.setState(State.VALUE);
                break;
            }
            case DOCUMENT: {
                final Map.Entry<String, BsonValue> currentElement = this.getContext().getNextElement();
                if (currentElement == null) {
                    this.setState(State.END_OF_DOCUMENT);
                    return BsonType.END_OF_DOCUMENT;
                }
                this.setCurrentName(currentElement.getKey());
                this.currentValue = currentElement.getValue();
                this.setState(State.NAME);
                break;
            }
            default: {
                throw new BSONException("Invalid ContextType.");
            }
        }
        this.setCurrentBsonType(this.currentValue.getBsonType());
        return this.getCurrentBsonType();
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
    
    @Override
    protected Context getContext() {
        return (Context)super.getContext();
    }
    
    protected class Mark extends AbstractBsonReader.Mark
    {
        private BsonValue currentValue;
        private BsonDocumentReader.Context context;
        
        protected Mark() {
            this.currentValue = BsonDocumentReader.this.currentValue;
            (this.context = BsonDocumentReader.this.getContext()).mark();
        }
        
        @Override
        protected void reset() {
            super.reset();
            BsonDocumentReader.this.currentValue = this.currentValue;
            BsonDocumentReader.this.setContext(this.context);
            this.context.reset();
        }
    }
    
    private static class BsonDocumentMarkableIterator<T> implements Iterator<T>
    {
        private Iterator<T> baseIterator;
        private List<T> markIterator;
        private int curIndex;
        private boolean marking;
        
        protected BsonDocumentMarkableIterator(final Iterator<T> baseIterator) {
            this.markIterator = new ArrayList<T>();
            this.baseIterator = baseIterator;
            this.curIndex = 0;
            this.marking = false;
        }
        
        protected void mark() {
            this.marking = true;
        }
        
        protected void reset() {
            this.curIndex = 0;
            this.marking = false;
        }
        
        @Override
        public boolean hasNext() {
            return this.baseIterator.hasNext() || this.curIndex < this.markIterator.size();
        }
        
        @Override
        public T next() {
            T value;
            if (this.curIndex < this.markIterator.size()) {
                value = this.markIterator.get(this.curIndex);
                if (this.marking) {
                    ++this.curIndex;
                }
                else {
                    this.markIterator.remove(0);
                }
            }
            else {
                value = this.baseIterator.next();
                if (this.marking) {
                    this.markIterator.add(value);
                    ++this.curIndex;
                }
            }
            return value;
        }
        
        @Override
        public void remove() {
        }
    }
    
    protected class Context extends AbstractBsonReader.Context
    {
        private BsonDocumentMarkableIterator<Map.Entry<String, BsonValue>> documentIterator;
        private BsonDocumentMarkableIterator<BsonValue> arrayIterator;
        
        protected Context(final Context parentContext, final BsonContextType contextType, final BsonArray array) {
            super(parentContext, contextType);
            this.arrayIterator = new BsonDocumentMarkableIterator<BsonValue>(array.iterator());
        }
        
        protected Context(final Context parentContext, final BsonContextType contextType, final BsonDocument document) {
            super(parentContext, contextType);
            this.documentIterator = new BsonDocumentMarkableIterator<Map.Entry<String, BsonValue>>(document.entrySet().iterator());
        }
        
        public Map.Entry<String, BsonValue> getNextElement() {
            if (this.documentIterator.hasNext()) {
                return this.documentIterator.next();
            }
            return null;
        }
        
        protected void mark() {
            if (this.documentIterator != null) {
                this.documentIterator.mark();
            }
            else {
                this.arrayIterator.mark();
            }
            if (this.getParentContext() != null) {
                ((Context)this.getParentContext()).mark();
            }
        }
        
        protected void reset() {
            if (this.documentIterator != null) {
                this.documentIterator.reset();
            }
            else {
                this.arrayIterator.reset();
            }
            if (this.getParentContext() != null) {
                ((Context)this.getParentContext()).reset();
            }
        }
        
        public BsonValue getNextValue() {
            if (this.arrayIterator.hasNext()) {
                return this.arrayIterator.next();
            }
            return null;
        }
    }
}
