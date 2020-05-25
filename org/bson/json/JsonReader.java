package org.bson.json;

import javax.xml.bind.*;
import java.util.*;
import java.text.*;
import org.bson.types.*;
import org.bson.*;

public class JsonReader extends AbstractBsonReader
{
    private final JsonScanner scanner;
    private JsonToken pushedToken;
    private Object currentValue;
    private Mark mark;
    
    public JsonReader(final String json) {
        this.scanner = new JsonScanner(json);
        this.setContext(new Context(null, BsonContextType.TOP_LEVEL));
    }
    
    @Override
    protected BsonBinary doReadBinaryData() {
        return (BsonBinary)this.currentValue;
    }
    
    @Override
    protected byte doPeekBinarySubType() {
        return this.doReadBinaryData().getType();
    }
    
    @Override
    protected int doPeekBinarySize() {
        return this.doReadBinaryData().getData().length;
    }
    
    @Override
    protected boolean doReadBoolean() {
        return (boolean)this.currentValue;
    }
    
    @Override
    public BsonType readBsonType() {
        if (this.isClosed()) {
            throw new IllegalStateException("This instance has been closed");
        }
        if (this.getState() == State.INITIAL || this.getState() == State.DONE || this.getState() == State.SCOPE_DOCUMENT) {
            this.setState(State.TYPE);
        }
        if (this.getState() != State.TYPE) {
            this.throwInvalidState("readBSONType", State.TYPE);
        }
        if (this.getContext().getContextType() == BsonContextType.DOCUMENT) {
            final JsonToken nameToken = this.popToken();
            switch (nameToken.getType()) {
                case STRING:
                case UNQUOTED_STRING: {
                    this.setCurrentName(nameToken.getValue(String.class));
                    final JsonToken colonToken = this.popToken();
                    if (colonToken.getType() != JsonTokenType.COLON) {
                        throw new JsonParseException("JSON reader was expecting ':' but found '%s'.", new Object[] { colonToken.getValue() });
                    }
                    break;
                }
                case END_OBJECT: {
                    this.setState(State.END_OF_DOCUMENT);
                    return BsonType.END_OF_DOCUMENT;
                }
                default: {
                    throw new JsonParseException("JSON reader was expecting a name but found '%s'.", new Object[] { nameToken.getValue() });
                }
            }
        }
        final JsonToken token = this.popToken();
        if (this.getContext().getContextType() == BsonContextType.ARRAY && token.getType() == JsonTokenType.END_ARRAY) {
            this.setState(State.END_OF_ARRAY);
            return BsonType.END_OF_DOCUMENT;
        }
        boolean noValueFound = false;
        switch (token.getType()) {
            case BEGIN_ARRAY: {
                this.setCurrentBsonType(BsonType.ARRAY);
                break;
            }
            case BEGIN_OBJECT: {
                this.visitExtendedJSON();
                break;
            }
            case DOUBLE: {
                this.setCurrentBsonType(BsonType.DOUBLE);
                this.currentValue = token.getValue();
                break;
            }
            case END_OF_FILE: {
                this.setCurrentBsonType(BsonType.END_OF_DOCUMENT);
                break;
            }
            case INT32: {
                this.setCurrentBsonType(BsonType.INT32);
                this.currentValue = token.getValue();
                break;
            }
            case INT64: {
                this.setCurrentBsonType(BsonType.INT64);
                this.currentValue = token.getValue();
                break;
            }
            case REGULAR_EXPRESSION: {
                this.setCurrentBsonType(BsonType.REGULAR_EXPRESSION);
                this.currentValue = token.getValue();
                break;
            }
            case STRING: {
                this.setCurrentBsonType(BsonType.STRING);
                this.currentValue = token.getValue();
                break;
            }
            case UNQUOTED_STRING: {
                final String value = token.getValue(String.class);
                if ("false".equals(value) || "true".equals(value)) {
                    this.setCurrentBsonType(BsonType.BOOLEAN);
                    this.currentValue = Boolean.parseBoolean(value);
                    break;
                }
                if ("Infinity".equals(value)) {
                    this.setCurrentBsonType(BsonType.DOUBLE);
                    this.currentValue = Double.POSITIVE_INFINITY;
                    break;
                }
                if ("NaN".equals(value)) {
                    this.setCurrentBsonType(BsonType.DOUBLE);
                    this.currentValue = Double.NaN;
                    break;
                }
                if ("null".equals(value)) {
                    this.setCurrentBsonType(BsonType.NULL);
                    break;
                }
                if ("undefined".equals(value)) {
                    this.setCurrentBsonType(BsonType.UNDEFINED);
                    break;
                }
                if ("BinData".equals(value)) {
                    this.setCurrentBsonType(BsonType.BINARY);
                    this.currentValue = this.visitBinDataConstructor();
                    break;
                }
                if ("Date".equals(value)) {
                    this.currentValue = this.visitDateTimeConstructorWithOutNew();
                    this.setCurrentBsonType(BsonType.STRING);
                    break;
                }
                if ("HexData".equals(value)) {
                    this.setCurrentBsonType(BsonType.BINARY);
                    this.currentValue = this.visitHexDataConstructor();
                    break;
                }
                if ("ISODate".equals(value)) {
                    this.setCurrentBsonType(BsonType.DATE_TIME);
                    this.currentValue = this.visitISODateTimeConstructor();
                    break;
                }
                if ("NumberLong".equals(value)) {
                    this.setCurrentBsonType(BsonType.INT64);
                    this.currentValue = this.visitNumberLongConstructor();
                    break;
                }
                if ("NumberDecimal".equals(value)) {
                    this.setCurrentBsonType(BsonType.DECIMAL128);
                    this.currentValue = this.visitNumberDecimalConstructor();
                    break;
                }
                if ("ObjectId".equals(value)) {
                    this.setCurrentBsonType(BsonType.OBJECT_ID);
                    this.currentValue = this.visitObjectIdConstructor();
                    break;
                }
                if ("Timestamp".equals(value)) {
                    this.setCurrentBsonType(BsonType.TIMESTAMP);
                    this.currentValue = this.visitTimestampConstructor();
                    break;
                }
                if ("RegExp".equals(value)) {
                    this.setCurrentBsonType(BsonType.REGULAR_EXPRESSION);
                    this.currentValue = this.visitRegularExpressionConstructor();
                    break;
                }
                if ("DBPointer".equals(value)) {
                    this.setCurrentBsonType(BsonType.DB_POINTER);
                    this.currentValue = this.visitDBPointerConstructor();
                    break;
                }
                if ("UUID".equals(value) || "GUID".equals(value) || "CSUUID".equals(value) || "CSGUID".equals(value) || "JUUID".equals(value) || "JGUID".equals(value) || "PYUUID".equals(value) || "PYGUID".equals(value)) {
                    this.setCurrentBsonType(BsonType.BINARY);
                    this.currentValue = this.visitUUIDConstructor(value);
                    break;
                }
                if ("new".equals(value)) {
                    this.visitNew();
                    break;
                }
                noValueFound = true;
                break;
            }
            default: {
                noValueFound = true;
                break;
            }
        }
        if (noValueFound) {
            throw new JsonParseException("JSON reader was expecting a value but found '%s'.", new Object[] { token.getValue() });
        }
        if (this.getContext().getContextType() == BsonContextType.ARRAY || this.getContext().getContextType() == BsonContextType.DOCUMENT) {
            final JsonToken commaToken = this.popToken();
            if (commaToken.getType() != JsonTokenType.COMMA) {
                this.pushToken(commaToken);
            }
        }
        switch (this.getContext().getContextType()) {
            default: {
                this.setState(State.NAME);
                break;
            }
            case ARRAY:
            case JAVASCRIPT_WITH_SCOPE:
            case TOP_LEVEL: {
                this.setState(State.VALUE);
                break;
            }
        }
        return this.getCurrentBsonType();
    }
    
    public Decimal128 doReadDecimal128() {
        return (Decimal128)this.currentValue;
    }
    
    @Override
    protected long doReadDateTime() {
        return (long)this.currentValue;
    }
    
    @Override
    protected double doReadDouble() {
        return (double)this.currentValue;
    }
    
    @Override
    protected void doReadEndArray() {
        this.setContext(this.getContext().getParentContext());
        if (this.getContext().getContextType() == BsonContextType.ARRAY || this.getContext().getContextType() == BsonContextType.DOCUMENT) {
            final JsonToken commaToken = this.popToken();
            if (commaToken.getType() != JsonTokenType.COMMA) {
                this.pushToken(commaToken);
            }
        }
    }
    
    @Override
    protected void doReadEndDocument() {
        this.setContext(this.getContext().getParentContext());
        if (this.getContext() != null && this.getContext().getContextType() == BsonContextType.SCOPE_DOCUMENT) {
            this.setContext(this.getContext().getParentContext());
            this.verifyToken("}");
        }
        if (this.getContext() == null) {
            throw new JsonParseException("Unexpected end of document.");
        }
        if (this.getContext().getContextType() == BsonContextType.ARRAY || this.getContext().getContextType() == BsonContextType.DOCUMENT) {
            final JsonToken commaToken = this.popToken();
            if (commaToken.getType() != JsonTokenType.COMMA) {
                this.pushToken(commaToken);
            }
        }
    }
    
    @Override
    protected int doReadInt32() {
        return (int)this.currentValue;
    }
    
    @Override
    protected long doReadInt64() {
        return (long)this.currentValue;
    }
    
    @Override
    protected String doReadJavaScript() {
        return (String)this.currentValue;
    }
    
    @Override
    protected String doReadJavaScriptWithScope() {
        return (String)this.currentValue;
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
        return (ObjectId)this.currentValue;
    }
    
    @Override
    protected BsonRegularExpression doReadRegularExpression() {
        return (BsonRegularExpression)this.currentValue;
    }
    
    @Override
    protected BsonDbPointer doReadDBPointer() {
        return (BsonDbPointer)this.currentValue;
    }
    
    @Override
    protected void doReadStartArray() {
        this.setContext(new Context(this.getContext(), BsonContextType.ARRAY));
    }
    
    @Override
    protected void doReadStartDocument() {
        this.setContext(new Context(this.getContext(), BsonContextType.DOCUMENT));
    }
    
    @Override
    protected String doReadString() {
        return (String)this.currentValue;
    }
    
    @Override
    protected String doReadSymbol() {
        return (String)this.currentValue;
    }
    
    @Override
    protected BsonTimestamp doReadTimestamp() {
        return (BsonTimestamp)this.currentValue;
    }
    
    @Override
    protected void doReadUndefined() {
    }
    
    @Override
    protected void doSkipName() {
    }
    
    @Override
    protected void doSkipValue() {
        switch (this.getCurrentBsonType()) {
            case ARRAY: {
                this.readStartArray();
                while (this.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    this.skipValue();
                }
                this.readEndArray();
                break;
            }
            case BINARY: {
                this.readBinaryData();
                break;
            }
            case BOOLEAN: {
                this.readBoolean();
                break;
            }
            case DATE_TIME: {
                this.readDateTime();
                break;
            }
            case DOCUMENT: {
                this.readStartDocument();
                while (this.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    this.skipName();
                    this.skipValue();
                }
                this.readEndDocument();
                break;
            }
            case DOUBLE: {
                this.readDouble();
                break;
            }
            case INT32: {
                this.readInt32();
                break;
            }
            case INT64: {
                this.readInt64();
                break;
            }
            case DECIMAL128: {
                this.readDecimal128();
                break;
            }
            case JAVASCRIPT: {
                this.readJavaScript();
                break;
            }
            case JAVASCRIPT_WITH_SCOPE: {
                this.readJavaScriptWithScope();
                this.readStartDocument();
                while (this.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    this.skipName();
                    this.skipValue();
                }
                this.readEndDocument();
                break;
            }
            case MAX_KEY: {
                this.readMaxKey();
                break;
            }
            case MIN_KEY: {
                this.readMinKey();
                break;
            }
            case NULL: {
                this.readNull();
                break;
            }
            case OBJECT_ID: {
                this.readObjectId();
                break;
            }
            case REGULAR_EXPRESSION: {
                this.readRegularExpression();
                break;
            }
            case STRING: {
                this.readString();
                break;
            }
            case SYMBOL: {
                this.readSymbol();
                break;
            }
            case TIMESTAMP: {
                this.readTimestamp();
                break;
            }
            case UNDEFINED: {
                this.readUndefined();
                break;
            }
        }
    }
    
    private JsonToken popToken() {
        if (this.pushedToken != null) {
            final JsonToken token = this.pushedToken;
            this.pushedToken = null;
            return token;
        }
        return this.scanner.nextToken();
    }
    
    private void pushToken(final JsonToken token) {
        if (this.pushedToken == null) {
            this.pushedToken = token;
            return;
        }
        throw new BsonInvalidOperationException("There is already a pending token.");
    }
    
    private void verifyToken(final Object expected) {
        if (expected == null) {
            throw new IllegalArgumentException("Can't be null");
        }
        final JsonToken token = this.popToken();
        if (!expected.equals(token.getValue())) {
            throw new JsonParseException("JSON reader expected '%s' but found '%s'.", new Object[] { expected, token.getValue() });
        }
    }
    
    private void verifyString(final String expected) {
        if (expected == null) {
            throw new IllegalArgumentException("Can't be null");
        }
        final JsonToken token = this.popToken();
        final JsonTokenType type = token.getType();
        if ((type != JsonTokenType.STRING && type != JsonTokenType.UNQUOTED_STRING) || !expected.equals(token.getValue())) {
            throw new JsonParseException("JSON reader expected '%s' but found '%s'.", new Object[] { expected, token.getValue() });
        }
    }
    
    private void visitNew() {
        final JsonToken typeToken = this.popToken();
        if (typeToken.getType() != JsonTokenType.UNQUOTED_STRING) {
            throw new JsonParseException("JSON reader expected a type name but found '%s'.", new Object[] { typeToken.getValue() });
        }
        final String value = typeToken.getValue(String.class);
        if ("BinData".equals(value)) {
            this.currentValue = this.visitBinDataConstructor();
            this.setCurrentBsonType(BsonType.BINARY);
        }
        else if ("Date".equals(value)) {
            this.currentValue = this.visitDateTimeConstructor();
            this.setCurrentBsonType(BsonType.DATE_TIME);
        }
        else if ("HexData".equals(value)) {
            this.currentValue = this.visitHexDataConstructor();
            this.setCurrentBsonType(BsonType.BINARY);
        }
        else if ("ISODate".equals(value)) {
            this.currentValue = this.visitISODateTimeConstructor();
            this.setCurrentBsonType(BsonType.DATE_TIME);
        }
        else if ("NumberLong".equals(value)) {
            this.currentValue = this.visitNumberLongConstructor();
            this.setCurrentBsonType(BsonType.INT64);
        }
        else if ("NumberDecimal".equals(value)) {
            this.currentValue = this.visitNumberDecimalConstructor();
            this.setCurrentBsonType(BsonType.DECIMAL128);
        }
        else if ("ObjectId".equals(value)) {
            this.currentValue = this.visitObjectIdConstructor();
            this.setCurrentBsonType(BsonType.OBJECT_ID);
        }
        else if ("RegExp".equals(value)) {
            this.currentValue = this.visitRegularExpressionConstructor();
            this.setCurrentBsonType(BsonType.REGULAR_EXPRESSION);
        }
        else if ("DBPointer".equals(value)) {
            this.currentValue = this.visitDBPointerConstructor();
            this.setCurrentBsonType(BsonType.DB_POINTER);
        }
        else {
            if (!"UUID".equals(value) && !"GUID".equals(value) && !"CSUUID".equals(value) && !"CSGUID".equals(value) && !"JUUID".equals(value) && !"JGUID".equals(value) && !"PYUUID".equals(value) && !"PYGUID".equals(value)) {
                throw new JsonParseException("JSON reader expected a type name but found '%s'.", new Object[] { value });
            }
            this.currentValue = this.visitUUIDConstructor(value);
            this.setCurrentBsonType(BsonType.BINARY);
        }
    }
    
    private void visitExtendedJSON() {
        final JsonToken nameToken = this.popToken();
        final String value = nameToken.getValue(String.class);
        final JsonTokenType type = nameToken.getType();
        if (type == JsonTokenType.STRING || type == JsonTokenType.UNQUOTED_STRING) {
            if ("$binary".equals(value)) {
                this.currentValue = this.visitBinDataExtendedJson();
                this.setCurrentBsonType(BsonType.BINARY);
                return;
            }
            if ("$code".equals(value)) {
                this.visitJavaScriptExtendedJson();
                return;
            }
            if ("$date".equals(value)) {
                this.currentValue = this.visitDateTimeExtendedJson();
                this.setCurrentBsonType(BsonType.DATE_TIME);
                return;
            }
            if ("$maxKey".equals(value)) {
                this.currentValue = this.visitMaxKeyExtendedJson();
                this.setCurrentBsonType(BsonType.MAX_KEY);
                return;
            }
            if ("$minKey".equals(value)) {
                this.currentValue = this.visitMinKeyExtendedJson();
                this.setCurrentBsonType(BsonType.MIN_KEY);
                return;
            }
            if ("$oid".equals(value)) {
                this.currentValue = this.visitObjectIdExtendedJson();
                this.setCurrentBsonType(BsonType.OBJECT_ID);
                return;
            }
            if ("$regex".equals(value)) {
                this.currentValue = this.visitRegularExpressionExtendedJson();
                this.setCurrentBsonType(BsonType.REGULAR_EXPRESSION);
                return;
            }
            if ("$symbol".equals(value)) {
                this.currentValue = this.visitSymbolExtendedJson();
                this.setCurrentBsonType(BsonType.SYMBOL);
                return;
            }
            if ("$timestamp".equals(value)) {
                this.currentValue = this.visitTimestampExtendedJson();
                this.setCurrentBsonType(BsonType.TIMESTAMP);
                return;
            }
            if ("$undefined".equals(value)) {
                this.currentValue = this.visitUndefinedExtendedJson();
                this.setCurrentBsonType(BsonType.UNDEFINED);
                return;
            }
            if ("$numberLong".equals(value)) {
                this.currentValue = this.visitNumberLongExtendedJson();
                this.setCurrentBsonType(BsonType.INT64);
                return;
            }
            if ("$numberDecimal".equals(value)) {
                this.currentValue = this.visitNumberDecimalExtendedJson();
                this.setCurrentBsonType(BsonType.DECIMAL128);
                return;
            }
        }
        this.pushToken(nameToken);
        this.setCurrentBsonType(BsonType.DOCUMENT);
    }
    
    private BsonBinary visitBinDataConstructor() {
        this.verifyToken("(");
        final JsonToken subTypeToken = this.popToken();
        if (subTypeToken.getType() != JsonTokenType.INT32) {
            throw new JsonParseException("JSON reader expected a binary subtype but found '%s'.", new Object[] { subTypeToken.getValue() });
        }
        this.verifyToken(",");
        final JsonToken bytesToken = this.popToken();
        if (bytesToken.getType() != JsonTokenType.UNQUOTED_STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { bytesToken.getValue() });
        }
        this.verifyToken(")");
        final byte[] bytes = DatatypeConverter.parseBase64Binary(bytesToken.getValue(String.class));
        return new BsonBinary(subTypeToken.getValue(Integer.class).byteValue(), bytes);
    }
    
    private BsonBinary visitUUIDConstructor(final String uuidConstructorName) {
        this.verifyToken("(");
        final JsonToken bytesToken = this.popToken();
        if (bytesToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { bytesToken.getValue() });
        }
        this.verifyToken(")");
        final String hexString = bytesToken.getValue(String.class).replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("-", "");
        final byte[] bytes = DatatypeConverter.parseHexBinary(hexString);
        BsonBinarySubType subType = BsonBinarySubType.UUID_STANDARD;
        if (!"UUID".equals(uuidConstructorName) || !"GUID".equals(uuidConstructorName)) {
            subType = BsonBinarySubType.UUID_LEGACY;
        }
        return new BsonBinary(subType, bytes);
    }
    
    private BsonRegularExpression visitRegularExpressionConstructor() {
        this.verifyToken("(");
        final JsonToken patternToken = this.popToken();
        if (patternToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { patternToken.getValue() });
        }
        String options = "";
        final JsonToken commaToken = this.popToken();
        if (commaToken.getType() == JsonTokenType.COMMA) {
            final JsonToken optionsToken = this.popToken();
            if (optionsToken.getType() != JsonTokenType.STRING) {
                throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { optionsToken.getValue() });
            }
            options = optionsToken.getValue(String.class);
        }
        else {
            this.pushToken(commaToken);
        }
        this.verifyToken(")");
        return new BsonRegularExpression(patternToken.getValue(String.class), options);
    }
    
    private ObjectId visitObjectIdConstructor() {
        this.verifyToken("(");
        final JsonToken valueToken = this.popToken();
        if (valueToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { valueToken.getValue() });
        }
        this.verifyToken(")");
        return new ObjectId(valueToken.getValue(String.class));
    }
    
    private BsonTimestamp visitTimestampConstructor() {
        this.verifyToken("(");
        final JsonToken timeToken = this.popToken();
        if (timeToken.getType() != JsonTokenType.INT32) {
            throw new JsonParseException("JSON reader expected an integer but found '%s'.", new Object[] { timeToken.getValue() });
        }
        final int time = timeToken.getValue(Integer.class);
        this.verifyToken(",");
        final JsonToken incrementToken = this.popToken();
        if (incrementToken.getType() != JsonTokenType.INT32) {
            throw new JsonParseException("JSON reader expected an integer but found '%s'.", new Object[] { timeToken.getValue() });
        }
        final int increment = incrementToken.getValue(Integer.class);
        this.verifyToken(")");
        return new BsonTimestamp(time, increment);
    }
    
    private BsonDbPointer visitDBPointerConstructor() {
        this.verifyToken("(");
        final JsonToken namespaceToken = this.popToken();
        if (namespaceToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { namespaceToken.getValue() });
        }
        this.verifyToken(",");
        final JsonToken idToken = this.popToken();
        if (namespaceToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { idToken.getValue() });
        }
        this.verifyToken(")");
        return new BsonDbPointer(namespaceToken.getValue(String.class), new ObjectId(idToken.getValue(String.class)));
    }
    
    private long visitNumberLongConstructor() {
        this.verifyToken("(");
        final JsonToken valueToken = this.popToken();
        long value;
        if (valueToken.getType() == JsonTokenType.INT32 || valueToken.getType() == JsonTokenType.INT64) {
            value = valueToken.getValue(Long.class);
        }
        else {
            if (valueToken.getType() != JsonTokenType.STRING) {
                throw new JsonParseException("JSON reader expected an integer or a string but found '%s'.", new Object[] { valueToken.getValue() });
            }
            value = Long.parseLong(valueToken.getValue(String.class));
        }
        this.verifyToken(")");
        return value;
    }
    
    private Decimal128 visitNumberDecimalConstructor() {
        this.verifyToken("(");
        final JsonToken valueToken = this.popToken();
        Decimal128 value;
        if (valueToken.getType() == JsonTokenType.INT32 || valueToken.getType() == JsonTokenType.INT64 || valueToken.getType() == JsonTokenType.DOUBLE) {
            value = valueToken.getValue(Decimal128.class);
        }
        else {
            if (valueToken.getType() != JsonTokenType.STRING) {
                throw new JsonParseException("JSON reader expected a number or a string but found '%s'.", new Object[] { valueToken.getValue() });
            }
            value = Decimal128.parse(valueToken.getValue(String.class));
        }
        this.verifyToken(")");
        return value;
    }
    
    private long visitISODateTimeConstructor() {
        this.verifyToken("(");
        final JsonToken token = this.popToken();
        if (token.getType() == JsonTokenType.RIGHT_PAREN) {
            return new Date().getTime();
        }
        if (token.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { token.getValue() });
        }
        this.verifyToken(")");
        final String[] patterns = { "yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ssz", "yyyy-MM-dd'T'HH:mm:ss.SSSz" };
        final SimpleDateFormat format = new SimpleDateFormat(patterns[0], Locale.ENGLISH);
        final ParsePosition pos = new ParsePosition(0);
        String s = token.getValue(String.class);
        if (s.endsWith("Z")) {
            s = s.substring(0, s.length() - 1) + "GMT-00:00";
        }
        for (final String pattern : patterns) {
            format.applyPattern(pattern);
            format.setLenient(true);
            pos.setIndex(0);
            final Date date = format.parse(s, pos);
            if (date != null && pos.getIndex() == s.length()) {
                return date.getTime();
            }
        }
        throw new JsonParseException("Invalid date format.");
    }
    
    private BsonBinary visitHexDataConstructor() {
        this.verifyToken("(");
        final JsonToken subTypeToken = this.popToken();
        if (subTypeToken.getType() != JsonTokenType.INT32) {
            throw new JsonParseException("JSON reader expected a binary subtype but found '%s'.", new Object[] { subTypeToken.getValue() });
        }
        this.verifyToken(",");
        final JsonToken bytesToken = this.popToken();
        if (bytesToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { bytesToken.getValue() });
        }
        this.verifyToken(")");
        String hex = bytesToken.getValue(String.class);
        if ((hex.length() & 0x1) != 0x0) {
            hex = "0" + hex;
        }
        for (final BsonBinarySubType subType : BsonBinarySubType.values()) {
            if (subType.getValue() == subTypeToken.getValue(Integer.class)) {
                return new BsonBinary(subType, DatatypeConverter.parseHexBinary(hex));
            }
        }
        return new BsonBinary(DatatypeConverter.parseHexBinary(hex));
    }
    
    private long visitDateTimeConstructor() {
        final DateFormat format = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z", Locale.ENGLISH);
        this.verifyToken("(");
        JsonToken token = this.popToken();
        if (token.getType() == JsonTokenType.RIGHT_PAREN) {
            return new Date().getTime();
        }
        if (token.getType() == JsonTokenType.STRING) {
            this.verifyToken(")");
            final String s = token.getValue(String.class);
            final ParsePosition pos = new ParsePosition(0);
            final Date dateTime = format.parse(s, pos);
            if (dateTime != null && pos.getIndex() == s.length()) {
                return dateTime.getTime();
            }
            throw new JsonParseException("JSON reader expected a date in 'EEE MMM dd yyyy HH:mm:ss z' format but found '%s'.", new Object[] { s });
        }
        else {
            if (token.getType() == JsonTokenType.INT32 || token.getType() == JsonTokenType.INT64) {
                final long[] values = new long[7];
                int pos2 = 0;
                do {
                    if (pos2 < values.length) {
                        values[pos2++] = token.getValue(Long.class);
                    }
                    token = this.popToken();
                    if (token.getType() == JsonTokenType.RIGHT_PAREN) {
                        if (pos2 == 1) {
                            return values[0];
                        }
                        if (pos2 < 3 || pos2 > 7) {
                            throw new JsonParseException("JSON reader expected 1 or 3-7 integers but found %d.", new Object[] { pos2 });
                        }
                        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        calendar.set(1, (int)values[0]);
                        calendar.set(2, (int)values[1]);
                        calendar.set(5, (int)values[2]);
                        calendar.set(11, (int)values[3]);
                        calendar.set(12, (int)values[4]);
                        calendar.set(13, (int)values[5]);
                        calendar.set(14, (int)values[6]);
                        return calendar.getTimeInMillis();
                    }
                    else {
                        if (token.getType() != JsonTokenType.COMMA) {
                            throw new JsonParseException("JSON reader expected a ',' or a ')' but found '%s'.", new Object[] { token.getValue() });
                        }
                        token = this.popToken();
                    }
                } while (token.getType() == JsonTokenType.INT32 || token.getType() == JsonTokenType.INT64);
                throw new JsonParseException("JSON reader expected an integer but found '%s'.", new Object[] { token.getValue() });
            }
            throw new JsonParseException("JSON reader expected an integer or a string but found '%s'.", new Object[] { token.getValue() });
        }
    }
    
    private String visitDateTimeConstructorWithOutNew() {
        this.verifyToken("(");
        JsonToken token = this.popToken();
        if (token.getType() != JsonTokenType.RIGHT_PAREN) {
            while (token.getType() != JsonTokenType.END_OF_FILE) {
                token = this.popToken();
                if (token.getType() == JsonTokenType.RIGHT_PAREN) {
                    break;
                }
            }
            if (token.getType() != JsonTokenType.RIGHT_PAREN) {
                throw new JsonParseException("JSON reader expected a ')' but found '%s'.", new Object[] { token.getValue() });
            }
        }
        final DateFormat df = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z", Locale.ENGLISH);
        return df.format(new Date());
    }
    
    private BsonBinary visitBinDataExtendedJson() {
        this.verifyToken(":");
        final JsonToken bytesToken = this.popToken();
        if (bytesToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { bytesToken.getValue() });
        }
        this.verifyToken(",");
        this.verifyString("$type");
        this.verifyToken(":");
        final JsonToken subTypeToken = this.popToken();
        if (subTypeToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { subTypeToken.getValue() });
        }
        this.verifyToken("}");
        final byte subType = (byte)Integer.parseInt(subTypeToken.getValue(String.class), 16);
        for (final BsonBinarySubType st : BsonBinarySubType.values()) {
            if (st.getValue() == subType) {
                return new BsonBinary(st, DatatypeConverter.parseBase64Binary(bytesToken.getValue(String.class)));
            }
        }
        return new BsonBinary(DatatypeConverter.parseBase64Binary(bytesToken.getValue(String.class)));
    }
    
    private long visitDateTimeExtendedJson() {
        this.verifyToken(":");
        final JsonToken valueToken = this.popToken();
        this.verifyToken("}");
        if (valueToken.getType() == JsonTokenType.INT32 || valueToken.getType() == JsonTokenType.INT64) {
            return valueToken.getValue(Long.class);
        }
        if (valueToken.getType() == JsonTokenType.STRING) {
            final String dateTimeString = valueToken.getValue(String.class);
            try {
                return DatatypeConverter.parseDateTime(dateTimeString).getTimeInMillis();
            }
            catch (IllegalArgumentException e) {
                throw new JsonParseException("JSON reader expected an ISO-8601 date time string but found '%s'.", new Object[] { dateTimeString });
            }
        }
        throw new JsonParseException("JSON reader expected an integer or string but found '%s'.", new Object[] { valueToken.getValue() });
    }
    
    private MaxKey visitMaxKeyExtendedJson() {
        this.verifyToken(":");
        this.verifyToken(1);
        this.verifyToken("}");
        return new MaxKey();
    }
    
    private MinKey visitMinKeyExtendedJson() {
        this.verifyToken(":");
        this.verifyToken(1);
        this.verifyToken("}");
        return new MinKey();
    }
    
    private ObjectId visitObjectIdExtendedJson() {
        this.verifyToken(":");
        final JsonToken valueToken = this.popToken();
        if (valueToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { valueToken.getValue() });
        }
        this.verifyToken("}");
        return new ObjectId(valueToken.getValue(String.class));
    }
    
    private BsonRegularExpression visitRegularExpressionExtendedJson() {
        this.verifyToken(":");
        final JsonToken patternToken = this.popToken();
        if (patternToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { patternToken.getValue() });
        }
        String options = "";
        final JsonToken commaToken = this.popToken();
        if (commaToken.getType() == JsonTokenType.COMMA) {
            this.verifyString("$options");
            this.verifyToken(":");
            final JsonToken optionsToken = this.popToken();
            if (optionsToken.getType() != JsonTokenType.STRING) {
                throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { optionsToken.getValue() });
            }
            options = optionsToken.getValue(String.class);
        }
        else {
            this.pushToken(commaToken);
        }
        this.verifyToken("}");
        return new BsonRegularExpression(patternToken.getValue(String.class), options);
    }
    
    private String visitSymbolExtendedJson() {
        this.verifyToken(":");
        final JsonToken nameToken = this.popToken();
        if (nameToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { nameToken.getValue() });
        }
        this.verifyToken("}");
        return nameToken.getValue(String.class);
    }
    
    private BsonTimestamp visitTimestampExtendedJson() {
        this.verifyToken(":");
        this.verifyToken("{");
        this.verifyString("t");
        this.verifyToken(":");
        final JsonToken timeToken = this.popToken();
        if (timeToken.getType() != JsonTokenType.INT32) {
            throw new JsonParseException("JSON reader expected an integer but found '%s'.", new Object[] { timeToken.getValue() });
        }
        final int time = timeToken.getValue(Integer.class);
        this.verifyToken(",");
        this.verifyString("i");
        this.verifyToken(":");
        final JsonToken incrementToken = this.popToken();
        if (incrementToken.getType() == JsonTokenType.INT32) {
            final int increment = incrementToken.getValue(Integer.class);
            this.verifyToken("}");
            this.verifyToken("}");
            return new BsonTimestamp(time, increment);
        }
        throw new JsonParseException("JSON reader expected an integer but found '%s'.", new Object[] { timeToken.getValue() });
    }
    
    private void visitJavaScriptExtendedJson() {
        this.verifyToken(":");
        final JsonToken codeToken = this.popToken();
        if (codeToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { codeToken.getValue() });
        }
        final JsonToken nextToken = this.popToken();
        switch (nextToken.getType()) {
            case COMMA: {
                this.verifyString("$scope");
                this.verifyToken(":");
                this.setState(State.VALUE);
                this.currentValue = codeToken.getValue();
                this.setCurrentBsonType(BsonType.JAVASCRIPT_WITH_SCOPE);
                this.setContext(new Context(this.getContext(), BsonContextType.SCOPE_DOCUMENT));
                break;
            }
            case END_OBJECT: {
                this.currentValue = codeToken.getValue();
                this.setCurrentBsonType(BsonType.JAVASCRIPT);
                break;
            }
            default: {
                throw new JsonParseException("JSON reader expected ',' or '}' but found '%s'.", new Object[] { codeToken.getValue() });
            }
        }
    }
    
    private BsonUndefined visitUndefinedExtendedJson() {
        this.verifyToken(":");
        final JsonToken nameToken = this.popToken();
        if (!nameToken.getValue(String.class).equals("true")) {
            throw new JsonParseException("JSON reader requires $undefined to have the value of true but found '%s'.", new Object[] { nameToken.getValue() });
        }
        this.verifyToken("}");
        return new BsonUndefined();
    }
    
    private Long visitNumberLongExtendedJson() {
        this.verifyToken(":");
        final JsonToken nameToken = this.popToken();
        if (nameToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { nameToken.getValue() });
        }
        this.verifyToken("}");
        return nameToken.getValue(Long.class);
    }
    
    private Decimal128 visitNumberDecimalExtendedJson() {
        this.verifyToken(":");
        final JsonToken nameToken = this.popToken();
        if (nameToken.getType() != JsonTokenType.STRING) {
            throw new JsonParseException("JSON reader expected a string but found '%s'.", new Object[] { nameToken.getValue() });
        }
        this.verifyToken("}");
        return nameToken.getValue(Decimal128.class);
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
        private JsonToken pushedToken;
        private Object currentValue;
        private int position;
        
        protected Mark() {
            this.pushedToken = JsonReader.this.pushedToken;
            this.currentValue = JsonReader.this.currentValue;
            this.position = JsonReader.this.scanner.getBufferPosition();
        }
        
        @Override
        protected void reset() {
            super.reset();
            JsonReader.this.pushedToken = this.pushedToken;
            JsonReader.this.currentValue = this.currentValue;
            JsonReader.this.scanner.setBufferPosition(this.position);
            AbstractBsonReader.this.setContext(new JsonReader.Context(this.getParentContext(), this.getContextType()));
        }
    }
    
    protected class Context extends AbstractBsonReader.Context
    {
        protected Context(final AbstractBsonReader.Context parentContext, final BsonContextType contextType) {
            super(parentContext, contextType);
        }
        
        @Override
        protected Context getParentContext() {
            return (Context)super.getParentContext();
        }
        
        @Override
        protected BsonContextType getContextType() {
            return super.getContextType();
        }
    }
}
