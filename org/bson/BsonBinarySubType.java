package org.bson;

public enum BsonBinarySubType
{
    BINARY((byte)0), 
    FUNCTION((byte)1), 
    OLD_BINARY((byte)2), 
    UUID_LEGACY((byte)3), 
    UUID_STANDARD((byte)4), 
    MD5((byte)5), 
    USER_DEFINED((byte)(-128));
    
    private final byte value;
    
    public static boolean isUuid(final byte value) {
        return value == BsonBinarySubType.UUID_LEGACY.getValue() || value == BsonBinarySubType.UUID_STANDARD.getValue();
    }
    
    private BsonBinarySubType(final byte value) {
        this.value = value;
    }
    
    public byte getValue() {
        return this.value;
    }
}
