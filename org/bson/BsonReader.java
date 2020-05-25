package org.bson;

import org.bson.types.*;

public interface BsonReader
{
    BsonType getCurrentBsonType();
    
    String getCurrentName();
    
    BsonBinary readBinaryData();
    
    byte peekBinarySubType();
    
    int peekBinarySize();
    
    BsonBinary readBinaryData(final String p0);
    
    boolean readBoolean();
    
    boolean readBoolean(final String p0);
    
    BsonType readBsonType();
    
    long readDateTime();
    
    long readDateTime(final String p0);
    
    double readDouble();
    
    double readDouble(final String p0);
    
    void readEndArray();
    
    void readEndDocument();
    
    int readInt32();
    
    int readInt32(final String p0);
    
    long readInt64();
    
    long readInt64(final String p0);
    
    Decimal128 readDecimal128();
    
    Decimal128 readDecimal128(final String p0);
    
    String readJavaScript();
    
    String readJavaScript(final String p0);
    
    String readJavaScriptWithScope();
    
    String readJavaScriptWithScope(final String p0);
    
    void readMaxKey();
    
    void readMaxKey(final String p0);
    
    void readMinKey();
    
    void readMinKey(final String p0);
    
    String readName();
    
    void readName(final String p0);
    
    void readNull();
    
    void readNull(final String p0);
    
    ObjectId readObjectId();
    
    ObjectId readObjectId(final String p0);
    
    BsonRegularExpression readRegularExpression();
    
    BsonRegularExpression readRegularExpression(final String p0);
    
    BsonDbPointer readDBPointer();
    
    BsonDbPointer readDBPointer(final String p0);
    
    void readStartArray();
    
    void readStartDocument();
    
    String readString();
    
    String readString(final String p0);
    
    String readSymbol();
    
    String readSymbol(final String p0);
    
    BsonTimestamp readTimestamp();
    
    BsonTimestamp readTimestamp(final String p0);
    
    void readUndefined();
    
    void readUndefined(final String p0);
    
    void skipName();
    
    void skipValue();
    
    void mark();
    
    void reset();
}
