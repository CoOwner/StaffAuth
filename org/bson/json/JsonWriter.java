package org.bson.json;

import java.io.*;
import javax.xml.bind.*;
import java.text.*;
import java.util.*;
import org.bson.types.*;
import org.bson.*;

public class JsonWriter extends AbstractBsonWriter
{
    private final Writer writer;
    private final JsonWriterSettings settings;
    
    public JsonWriter(final Writer writer) {
        this(writer, new JsonWriterSettings());
    }
    
    public JsonWriter(final Writer writer, final JsonWriterSettings settings) {
        super(settings);
        this.settings = settings;
        this.writer = writer;
        this.setContext(new Context(null, BsonContextType.TOP_LEVEL, ""));
    }
    
    public Writer getWriter() {
        return this.writer;
    }
    
    @Override
    protected Context getContext() {
        return (Context)super.getContext();
    }
    
    @Override
    protected void doWriteStartDocument() {
        try {
            if (this.getState() == State.VALUE || this.getState() == State.SCOPE_DOCUMENT) {
                this.writeNameHelper(this.getName());
            }
            this.writer.write("{");
            final BsonContextType contextType = (this.getState() == State.SCOPE_DOCUMENT) ? BsonContextType.SCOPE_DOCUMENT : BsonContextType.DOCUMENT;
            this.setContext(new Context(this.getContext(), contextType, this.settings.getIndentCharacters()));
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    @Override
    protected void doWriteEndDocument() {
        try {
            if (this.settings.isIndent() && this.getContext().hasElements) {
                this.writer.write(this.settings.getNewLineCharacters());
                if (this.getContext().getParentContext() != null) {
                    this.writer.write(this.getContext().getParentContext().indentation);
                }
                this.writer.write("}");
            }
            else {
                this.writer.write(" }");
            }
            if (this.getContext().getContextType() == BsonContextType.SCOPE_DOCUMENT) {
                this.setContext(this.getContext().getParentContext());
                this.writeEndDocument();
            }
            else {
                this.setContext(this.getContext().getParentContext());
            }
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    @Override
    protected void doWriteStartArray() {
        try {
            this.writeNameHelper(this.getName());
            this.writer.write("[");
            this.setContext(new Context(this.getContext(), BsonContextType.ARRAY, this.settings.getIndentCharacters()));
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    @Override
    protected void doWriteEndArray() {
        try {
            this.writer.write("]");
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
        this.setContext(this.getContext().getParentContext());
    }
    
    @Override
    protected void doWriteBinaryData(final BsonBinary binary) {
        try {
            switch (this.settings.getOutputMode()) {
                case SHELL: {
                    this.writeNameHelper(this.getName());
                    this.writer.write(String.format("new BinData(%s, \"%s\")", Integer.toString(binary.getType() & 0xFF), DatatypeConverter.printBase64Binary(binary.getData())));
                    break;
                }
                default: {
                    this.writeStartDocument();
                    this.writeString("$binary", DatatypeConverter.printBase64Binary(binary.getData()));
                    this.writeString("$type", String.format("%02X", binary.getType()));
                    this.writeEndDocument();
                    break;
                }
            }
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    public void doWriteBoolean(final boolean value) {
        try {
            this.writeNameHelper(this.getName());
            this.writer.write(value ? "true" : "false");
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    @Override
    protected void doWriteDateTime(final long value) {
        try {
            switch (this.settings.getOutputMode()) {
                case STRICT: {
                    this.writeStartDocument();
                    this.writeNameHelper("$date");
                    this.writer.write(Long.toString(value));
                    this.writeEndDocument();
                    break;
                }
                case SHELL: {
                    this.writeNameHelper(this.getName());
                    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    if (value >= -59014396800000L && value <= 253399536000000L) {
                        this.writer.write(String.format("ISODate(\"%s\")", dateFormat.format(new Date(value))));
                        break;
                    }
                    this.writer.write(String.format("new Date(%d)", value));
                    break;
                }
                default: {
                    throw new BSONException("Unexpected JSONMode.");
                }
            }
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    @Override
    protected void doWriteDBPointer(final BsonDbPointer value) {
        this.writeStartDocument();
        this.writeString("$ref", value.getNamespace());
        this.writeObjectId("$id", value.getId());
        this.writeEndDocument();
    }
    
    @Override
    protected void doWriteDouble(final double value) {
        try {
            this.writeNameHelper(this.getName());
            this.writer.write(Double.toString(value));
            this.setState(this.getNextState());
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    @Override
    protected void doWriteInt32(final int value) {
        try {
            this.writeNameHelper(this.getName());
            this.writer.write(Integer.toString(value));
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    @Override
    protected void doWriteInt64(final long value) {
        try {
            switch (this.settings.getOutputMode()) {
                case STRICT: {
                    this.writeStartDocument();
                    this.writeNameHelper("$numberLong");
                    this.writer.write(String.format("\"%d\"", value));
                    this.writeEndDocument();
                    break;
                }
                case SHELL: {
                    this.writeNameHelper(this.getName());
                    if (value >= -2147483648L && value <= 2147483647L) {
                        this.writer.write(String.format("NumberLong(%d)", value));
                        break;
                    }
                    this.writer.write(String.format("NumberLong(\"%d\")", value));
                    break;
                }
                default: {
                    this.writeNameHelper(this.getName());
                    this.writer.write(Long.toString(value));
                    break;
                }
            }
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    @Override
    protected void doWriteDecimal128(final Decimal128 value) {
        try {
            switch (this.settings.getOutputMode()) {
                case SHELL: {
                    this.writeNameHelper(this.getName());
                    this.writer.write(String.format("NumberDecimal(\"%s\")", value.toString()));
                    break;
                }
                default: {
                    this.writeStartDocument();
                    this.writeNameHelper("$numberDecimal");
                    this.writer.write(String.format("\"%s\"", value.toString()));
                    this.writeEndDocument();
                    break;
                }
            }
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    @Override
    protected void doWriteJavaScript(final String code) {
        this.writeStartDocument();
        this.writeString("$code", code);
        this.writeEndDocument();
    }
    
    @Override
    protected void doWriteJavaScriptWithScope(final String code) {
        this.writeStartDocument();
        this.writeString("$code", code);
        this.writeName("$scope");
    }
    
    @Override
    protected void doWriteMaxKey() {
        this.writeStartDocument();
        this.writeInt32("$maxKey", 1);
        this.writeEndDocument();
    }
    
    @Override
    protected void doWriteMinKey() {
        this.writeStartDocument();
        this.writeInt32("$minKey", 1);
        this.writeEndDocument();
    }
    
    public void doWriteNull() {
        try {
            this.writeNameHelper(this.getName());
            this.writer.write("null");
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    public void doWriteObjectId(final ObjectId objectId) {
        try {
            switch (this.settings.getOutputMode()) {
                case STRICT: {
                    this.writeStartDocument();
                    this.writeString("$oid", objectId.toString());
                    this.writeEndDocument();
                    break;
                }
                case SHELL: {
                    this.writeNameHelper(this.getName());
                    this.writer.write(String.format("ObjectId(\"%s\")", objectId.toString()));
                    break;
                }
                default: {
                    throw new BSONException("Unknown output mode" + this.settings.getOutputMode());
                }
            }
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    public void doWriteRegularExpression(final BsonRegularExpression regularExpression) {
        try {
            switch (this.settings.getOutputMode()) {
                case STRICT: {
                    this.writeStartDocument();
                    this.writeString("$regex", regularExpression.getPattern());
                    this.writeString("$options", regularExpression.getOptions());
                    this.writeEndDocument();
                    break;
                }
                case SHELL: {
                    this.writeNameHelper(this.getName());
                    this.writer.write("/");
                    final String escaped = regularExpression.getPattern().equals("") ? "(?:)" : regularExpression.getPattern().replace("/", "\\/");
                    this.writer.write(escaped);
                    this.writer.write("/");
                    this.writer.write(regularExpression.getOptions());
                    break;
                }
                default: {
                    throw new BSONException("Unknown output mode" + this.settings.getOutputMode());
                }
            }
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    public void doWriteString(final String value) {
        try {
            this.writeNameHelper(this.getName());
            this.writeStringHelper(value);
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    public void doWriteSymbol(final String value) {
        this.writeStartDocument();
        this.writeString("$symbol", value);
        this.writeEndDocument();
    }
    
    public void doWriteTimestamp(final BsonTimestamp value) {
        try {
            switch (this.settings.getOutputMode()) {
                case STRICT: {
                    this.writeStartDocument();
                    this.writeStartDocument("$timestamp");
                    this.writeInt32("t", value.getTime());
                    this.writeInt32("i", value.getInc());
                    this.writeEndDocument();
                    this.writeEndDocument();
                    break;
                }
                case SHELL: {
                    this.writeNameHelper(this.getName());
                    this.writer.write(String.format("Timestamp(%d, %d)", value.getTime(), value.getInc()));
                    break;
                }
                default: {
                    throw new BSONException("Unknown output mode" + this.settings.getOutputMode());
                }
            }
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    public void doWriteUndefined() {
        try {
            switch (this.settings.getOutputMode()) {
                case STRICT: {
                    this.writeStartDocument();
                    this.writeBoolean("$undefined", true);
                    this.writeEndDocument();
                    break;
                }
                case SHELL: {
                    this.writeNameHelper(this.getName());
                    this.writer.write("undefined");
                    break;
                }
                default: {
                    throw new BSONException("Unknown output mode" + this.settings.getOutputMode());
                }
            }
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    @Override
    public void flush() {
        try {
            this.writer.flush();
        }
        catch (IOException e) {
            this.throwBSONException(e);
        }
    }
    
    private void writeNameHelper(final String name) throws IOException {
        switch (this.getContext().getContextType()) {
            case ARRAY: {
                if (this.getContext().hasElements) {
                    this.writer.write(", ");
                    break;
                }
                break;
            }
            case DOCUMENT:
            case SCOPE_DOCUMENT: {
                if (this.getContext().hasElements) {
                    this.writer.write(",");
                }
                if (this.settings.isIndent()) {
                    this.writer.write(this.settings.getNewLineCharacters());
                    this.writer.write(this.getContext().indentation);
                }
                else {
                    this.writer.write(" ");
                }
                this.writeStringHelper(name);
                this.writer.write(" : ");
                break;
            }
            case TOP_LEVEL: {
                break;
            }
            default: {
                throw new BSONException("Invalid contextType.");
            }
        }
        this.getContext().hasElements = true;
    }
    
    private void writeStringHelper(final String str) throws IOException {
        this.writer.write(34);
        for (final char c : str.toCharArray()) {
            Label_0415: {
                switch (c) {
                    case '\"': {
                        this.writer.write("\\\"");
                        break;
                    }
                    case '\\': {
                        this.writer.write("\\\\");
                        break;
                    }
                    case '\b': {
                        this.writer.write("\\b");
                        break;
                    }
                    case '\f': {
                        this.writer.write("\\f");
                        break;
                    }
                    case '\n': {
                        this.writer.write("\\n");
                        break;
                    }
                    case '\r': {
                        this.writer.write("\\r");
                        break;
                    }
                    case '\t': {
                        this.writer.write("\\t");
                        break;
                    }
                    default: {
                        switch (Character.getType(c)) {
                            case 1:
                            case 2:
                            case 3:
                            case 5:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                            case 24:
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                            case 29:
                            case 30: {
                                this.writer.write(c);
                                break Label_0415;
                            }
                            default: {
                                this.writer.write("\\u");
                                this.writer.write(Integer.toHexString((c & '\uf000') >> 12));
                                this.writer.write(Integer.toHexString((c & '\u0f00') >> 8));
                                this.writer.write(Integer.toHexString((c & '\u00f0') >> 4));
                                this.writer.write(Integer.toHexString(c & '\u000f'));
                                break Label_0415;
                            }
                        }
                        break;
                    }
                }
            }
        }
        this.writer.write(34);
    }
    
    private void throwBSONException(final IOException e) {
        throw new BSONException("Wrapping IOException", e);
    }
    
    public class Context extends AbstractBsonWriter.Context
    {
        private final String indentation;
        private boolean hasElements;
        
        public Context(final Context parentContext, final BsonContextType contextType, final String indentChars) {
            super(parentContext, contextType);
            this.indentation = ((parentContext == null) ? indentChars : (parentContext.indentation + indentChars));
        }
        
        @Override
        public Context getParentContext() {
            return (Context)super.getParentContext();
        }
    }
}
