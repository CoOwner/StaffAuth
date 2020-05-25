package org.bson;

import org.bson.types.*;

public interface BsonWriter
{
    void flush();
    
    void writeBinaryData(final BsonBinary p0);
    
    void writeBinaryData(final String p0, final BsonBinary p1);
    
    void writeBoolean(final boolean p0);
    
    void writeBoolean(final String p0, final boolean p1);
    
    void writeDateTime(final long p0);
    
    void writeDateTime(final String p0, final long p1);
    
    void writeDBPointer(final BsonDbPointer p0);
    
    void writeDBPointer(final String p0, final BsonDbPointer p1);
    
    void writeDouble(final double p0);
    
    void writeDouble(final String p0, final double p1);
    
    void writeEndArray();
    
    void writeEndDocument();
    
    void writeInt32(final int p0);
    
    void writeInt32(final String p0, final int p1);
    
    void writeInt64(final long p0);
    
    void writeInt64(final String p0, final long p1);
    
    void writeDecimal128(final Decimal128 p0);
    
    void writeDecimal128(final String p0, final Decimal128 p1);
    
    void writeJavaScript(final String p0);
    
    void writeJavaScript(final String p0, final String p1);
    
    void writeJavaScriptWithScope(final String p0);
    
    void writeJavaScriptWithScope(final String p0, final String p1);
    
    void writeMaxKey();
    
    void writeMaxKey(final String p0);
    
    void writeMinKey();
    
    void writeMinKey(final String p0);
    
    void writeName(final String p0);
    
    void writeNull();
    
    void writeNull(final String p0);
    
    void writeObjectId(final ObjectId p0);
    
    void writeObjectId(final String p0, final ObjectId p1);
    
    void writeRegularExpression(final BsonRegularExpression p0);
    
    void writeRegularExpression(final String p0, final BsonRegularExpression p1);
    
    void writeStartArray();
    
    void writeStartArray(final String p0);
    
    void writeStartDocument();
    
    void writeStartDocument(final String p0);
    
    void writeString(final String p0);
    
    void writeString(final String p0, final String p1);
    
    void writeSymbol(final String p0);
    
    void writeSymbol(final String p0, final String p1);
    
    void writeTimestamp(final BsonTimestamp p0);
    
    void writeTimestamp(final String p0, final BsonTimestamp p1);
    
    void writeUndefined();
    
    void writeUndefined(final String p0);
    
    void pipe(final BsonReader p0);
}
