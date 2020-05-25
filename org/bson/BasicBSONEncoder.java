package org.bson;

import org.bson.io.*;
import java.util.regex.*;
import java.util.*;
import com.mongodb.*;
import org.bson.types.*;
import java.util.concurrent.atomic.*;
import java.lang.reflect.*;

public class BasicBSONEncoder implements BSONEncoder
{
    private BsonBinaryWriter bsonWriter;
    private OutputBuffer outputBuffer;
    
    @Override
    public byte[] encode(final BSONObject document) {
        final OutputBuffer outputBuffer = new BasicOutputBuffer();
        this.set(outputBuffer);
        this.putObject(document);
        this.done();
        return outputBuffer.toByteArray();
    }
    
    @Override
    public void done() {
        this.bsonWriter.close();
        this.bsonWriter = null;
    }
    
    @Override
    public void set(final OutputBuffer buffer) {
        if (this.bsonWriter != null) {
            throw new IllegalStateException("Performing another operation at this moment");
        }
        this.outputBuffer = buffer;
        this.bsonWriter = new BsonBinaryWriter(buffer);
    }
    
    protected OutputBuffer getOutputBuffer() {
        return this.outputBuffer;
    }
    
    protected BsonBinaryWriter getBsonWriter() {
        return this.bsonWriter;
    }
    
    @Override
    public int putObject(final BSONObject document) {
        final int startPosition = this.getOutputBuffer().getPosition();
        this.bsonWriter.writeStartDocument();
        if (this.isTopLevelDocument() && document.containsField("_id")) {
            this._putObjectField("_id", document.get("_id"));
        }
        for (final String key : document.keySet()) {
            if (this.isTopLevelDocument() && key.equals("_id")) {
                continue;
            }
            this._putObjectField(key, document.get(key));
        }
        this.bsonWriter.writeEndDocument();
        return this.getOutputBuffer().getPosition() - startPosition;
    }
    
    private boolean isTopLevelDocument() {
        return this.bsonWriter.getContext().getParentContext() == null;
    }
    
    protected void putName(final String name) {
        if (this.bsonWriter.getState() == AbstractBsonWriter.State.NAME) {
            this.bsonWriter.writeName(name);
        }
    }
    
    protected void _putObjectField(final String name, final Object initialValue) {
        if ("_transientFields".equals(name)) {
            return;
        }
        if (name.contains("\u0000")) {
            throw new IllegalArgumentException("Document field names can't have a NULL character. (Bad Key: '" + name + "')");
        }
        if ("$where".equals(name) && initialValue instanceof String) {
            this.putCode(name, new Code((String)initialValue));
        }
        final Object value = BSON.applyEncodingHooks(initialValue);
        if (value == null) {
            this.putNull(name);
        }
        else if (value instanceof Date) {
            this.putDate(name, (Date)value);
        }
        else if (value instanceof Number) {
            this.putNumber(name, (Number)value);
        }
        else if (value instanceof Decimal128) {
            this.putDecimal128(name, (Decimal128)value);
        }
        else if (value instanceof Character) {
            this.putString(name, value.toString());
        }
        else if (value instanceof String) {
            this.putString(name, value.toString());
        }
        else if (value instanceof ObjectId) {
            this.putObjectId(name, (ObjectId)value);
        }
        else if (value instanceof Boolean) {
            this.putBoolean(name, (Boolean)value);
        }
        else if (value instanceof Pattern) {
            this.putPattern(name, (Pattern)value);
        }
        else if (value instanceof Iterable) {
            this.putIterable(name, (Iterable)value);
        }
        else if (value instanceof BSONObject) {
            this.putObject(name, (BSONObject)value);
        }
        else if (value instanceof Map) {
            this.putMap(name, (Map)value);
        }
        else if (value instanceof byte[]) {
            this.putBinary(name, (byte[])value);
        }
        else if (value instanceof Binary) {
            this.putBinary(name, (Binary)value);
        }
        else if (value instanceof UUID) {
            this.putUUID(name, (UUID)value);
        }
        else if (value.getClass().isArray()) {
            this.putArray(name, value);
        }
        else if (value instanceof Symbol) {
            this.putSymbol(name, (Symbol)value);
        }
        else if (value instanceof BSONTimestamp) {
            this.putTimestamp(name, (BSONTimestamp)value);
        }
        else if (value instanceof CodeWScope) {
            this.putCodeWScope(name, (CodeWScope)value);
        }
        else if (value instanceof Code) {
            this.putCode(name, (Code)value);
        }
        else if (value instanceof DBRef) {
            final BSONObject temp = new BasicBSONObject();
            final DBRef dbRef = (DBRef)value;
            temp.put("$ref", dbRef.getCollectionName());
            temp.put("$id", dbRef.getId());
            if (dbRef.getDatabaseName() != null) {
                temp.put("$db", dbRef.getDatabaseName());
            }
            this.putObject(name, temp);
        }
        else if (value instanceof MinKey) {
            this.putMinKey(name);
        }
        else if (value instanceof MaxKey) {
            this.putMaxKey(name);
        }
        else if (!this.putSpecial(name, value)) {
            throw new IllegalArgumentException("Can't serialize " + value.getClass());
        }
    }
    
    protected void putNull(final String name) {
        this.putName(name);
        this.bsonWriter.writeNull();
    }
    
    protected void putUndefined(final String name) {
        this.putName(name);
        this.bsonWriter.writeUndefined();
    }
    
    protected void putTimestamp(final String name, final BSONTimestamp timestamp) {
        this.putName(name);
        this.bsonWriter.writeTimestamp(new BsonTimestamp(timestamp.getTime(), timestamp.getInc()));
    }
    
    protected void putCode(final String name, final Code code) {
        this.putName(name);
        this.bsonWriter.writeJavaScript(code.getCode());
    }
    
    protected void putCodeWScope(final String name, final CodeWScope codeWScope) {
        this.putName(name);
        this.bsonWriter.writeJavaScriptWithScope(codeWScope.getCode());
        this.putObject(codeWScope.getScope());
    }
    
    protected void putBoolean(final String name, final Boolean value) {
        this.putName(name);
        this.bsonWriter.writeBoolean(value);
    }
    
    protected void putDate(final String name, final Date date) {
        this.putName(name);
        this.bsonWriter.writeDateTime(date.getTime());
    }
    
    protected void putNumber(final String name, final Number number) {
        this.putName(name);
        if (number instanceof Integer || number instanceof Short || number instanceof Byte || number instanceof AtomicInteger) {
            this.bsonWriter.writeInt32(number.intValue());
        }
        else if (number instanceof Long || number instanceof AtomicLong) {
            this.bsonWriter.writeInt64(number.longValue());
        }
        else {
            if (!(number instanceof Float) && !(number instanceof Double)) {
                throw new IllegalArgumentException("Can't serialize " + number.getClass());
            }
            this.bsonWriter.writeDouble(number.doubleValue());
        }
    }
    
    protected void putDecimal128(final String name, final Decimal128 value) {
        this.putName(name);
        this.bsonWriter.writeDecimal128(value);
    }
    
    protected void putBinary(final String name, final byte[] bytes) {
        this.putName(name);
        this.bsonWriter.writeBinaryData(new BsonBinary(bytes));
    }
    
    protected void putBinary(final String name, final Binary binary) {
        this.putName(name);
        this.bsonWriter.writeBinaryData(new BsonBinary(binary.getType(), binary.getData()));
    }
    
    protected void putUUID(final String name, final UUID uuid) {
        this.putName(name);
        final byte[] bytes = new byte[16];
        writeLongToArrayLittleEndian(bytes, 0, uuid.getMostSignificantBits());
        writeLongToArrayLittleEndian(bytes, 8, uuid.getLeastSignificantBits());
        this.bsonWriter.writeBinaryData(new BsonBinary(BsonBinarySubType.UUID_LEGACY, bytes));
    }
    
    protected void putSymbol(final String name, final Symbol symbol) {
        this.putName(name);
        this.bsonWriter.writeSymbol(symbol.getSymbol());
    }
    
    protected void putString(final String name, final String value) {
        this.putName(name);
        this.bsonWriter.writeString(value);
    }
    
    protected void putPattern(final String name, final Pattern value) {
        this.putName(name);
        this.bsonWriter.writeRegularExpression(new BsonRegularExpression(value.pattern(), BSON.regexFlags(value.flags())));
    }
    
    protected void putObjectId(final String name, final ObjectId objectId) {
        this.putName(name);
        this.bsonWriter.writeObjectId(objectId);
    }
    
    protected void putArray(final String name, final Object object) {
        this.putName(name);
        this.bsonWriter.writeStartArray();
        if (object instanceof int[]) {
            for (final int i : (int[])object) {
                this.bsonWriter.writeInt32(i);
            }
        }
        else if (object instanceof long[]) {
            for (final long j : (long[])object) {
                this.bsonWriter.writeInt64(j);
            }
        }
        else if (object instanceof float[]) {
            for (final float k : (float[])object) {
                this.bsonWriter.writeDouble(k);
            }
        }
        else if (object instanceof short[]) {
            for (final short l : (short[])object) {
                this.bsonWriter.writeInt32(l);
            }
        }
        else if (object instanceof byte[]) {
            for (final byte m : (byte[])object) {
                this.bsonWriter.writeInt32(m);
            }
        }
        else if (object instanceof double[]) {
            for (final double i2 : (double[])object) {
                this.bsonWriter.writeDouble(i2);
            }
        }
        else if (object instanceof boolean[]) {
            for (final boolean i3 : (boolean[])object) {
                this.bsonWriter.writeBoolean(i3);
            }
        }
        else if (object instanceof String[]) {
            for (final String i4 : (String[])object) {
                this.bsonWriter.writeString(i4);
            }
        }
        else {
            for (int length = Array.getLength(object), i5 = 0; i5 < length; ++i5) {
                this._putObjectField(String.valueOf(i5), Array.get(object, i5));
            }
        }
        this.bsonWriter.writeEndArray();
    }
    
    protected void putIterable(final String name, final Iterable iterable) {
        this.putName(name);
        this.bsonWriter.writeStartArray();
        final int i = 0;
        for (final Object o : iterable) {
            this._putObjectField(String.valueOf(i), o);
        }
        this.bsonWriter.writeEndArray();
    }
    
    protected void putMap(final String name, final Map map) {
        this.putName(name);
        this.bsonWriter.writeStartDocument();
        for (final Map.Entry entry : map.entrySet()) {
            this._putObjectField(entry.getKey(), entry.getValue());
        }
        this.bsonWriter.writeEndDocument();
    }
    
    protected int putObject(final String name, final BSONObject document) {
        this.putName(name);
        return this.putObject(document);
    }
    
    protected boolean putSpecial(final String name, final Object special) {
        return false;
    }
    
    protected void putMinKey(final String name) {
        this.putName(name);
        this.bsonWriter.writeMinKey();
    }
    
    protected void putMaxKey(final String name) {
        this.putName(name);
        this.bsonWriter.writeMaxKey();
    }
    
    private static void writeLongToArrayLittleEndian(final byte[] bytes, final int offset, final long x) {
        bytes[offset] = (byte)(0xFFL & x);
        bytes[offset + 1] = (byte)(0xFFL & x >> 8);
        bytes[offset + 2] = (byte)(0xFFL & x >> 16);
        bytes[offset + 3] = (byte)(0xFFL & x >> 24);
        bytes[offset + 4] = (byte)(0xFFL & x >> 32);
        bytes[offset + 5] = (byte)(0xFFL & x >> 40);
        bytes[offset + 6] = (byte)(0xFFL & x >> 48);
        bytes[offset + 7] = (byte)(0xFFL & x >> 56);
    }
}
