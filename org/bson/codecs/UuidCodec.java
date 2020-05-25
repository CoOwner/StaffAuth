package org.bson.codecs;

import java.util.*;
import org.bson.*;

public class UuidCodec implements Codec<UUID>
{
    private final UuidRepresentation encoderUuidRepresentation;
    private final UuidRepresentation decoderUuidRepresentation;
    
    public UuidCodec(final UuidRepresentation uuidRepresentation) {
        this.encoderUuidRepresentation = uuidRepresentation;
        this.decoderUuidRepresentation = uuidRepresentation;
    }
    
    public UuidCodec() {
        this.encoderUuidRepresentation = UuidRepresentation.JAVA_LEGACY;
        this.decoderUuidRepresentation = UuidRepresentation.JAVA_LEGACY;
    }
    
    @Override
    public void encode(final BsonWriter writer, final UUID value, final EncoderContext encoderContext) {
        final byte[] binaryData = new byte[16];
        writeLongToArrayBigEndian(binaryData, 0, value.getMostSignificantBits());
        writeLongToArrayBigEndian(binaryData, 8, value.getLeastSignificantBits());
        switch (this.encoderUuidRepresentation) {
            case C_SHARP_LEGACY: {
                UuidCodecHelper.reverseByteArray(binaryData, 0, 4);
                UuidCodecHelper.reverseByteArray(binaryData, 4, 2);
                UuidCodecHelper.reverseByteArray(binaryData, 6, 2);
                break;
            }
            case JAVA_LEGACY: {
                UuidCodecHelper.reverseByteArray(binaryData, 0, 8);
                UuidCodecHelper.reverseByteArray(binaryData, 8, 8);
                break;
            }
            case PYTHON_LEGACY:
            case STANDARD: {
                break;
            }
            default: {
                throw new BSONException("Unexpected UUID representation");
            }
        }
        if (this.encoderUuidRepresentation == UuidRepresentation.STANDARD) {
            writer.writeBinaryData(new BsonBinary(BsonBinarySubType.UUID_STANDARD, binaryData));
        }
        else {
            writer.writeBinaryData(new BsonBinary(BsonBinarySubType.UUID_LEGACY, binaryData));
        }
    }
    
    @Override
    public UUID decode(final BsonReader reader, final DecoderContext decoderContext) {
        final byte subType = reader.peekBinarySubType();
        if (subType != BsonBinarySubType.UUID_LEGACY.getValue() && subType != BsonBinarySubType.UUID_STANDARD.getValue()) {
            throw new BSONException("Unexpected BsonBinarySubType");
        }
        final byte[] bytes = reader.readBinaryData().getData();
        if (bytes.length != 16) {
            throw new BsonSerializationException(String.format("Expected length to be 16, not %d.", bytes.length));
        }
        if (subType == BsonBinarySubType.UUID_LEGACY.getValue()) {
            switch (this.decoderUuidRepresentation) {
                case C_SHARP_LEGACY: {
                    UuidCodecHelper.reverseByteArray(bytes, 0, 4);
                    UuidCodecHelper.reverseByteArray(bytes, 4, 2);
                    UuidCodecHelper.reverseByteArray(bytes, 6, 2);
                    break;
                }
                case JAVA_LEGACY: {
                    UuidCodecHelper.reverseByteArray(bytes, 0, 8);
                    UuidCodecHelper.reverseByteArray(bytes, 8, 8);
                    break;
                }
                case PYTHON_LEGACY:
                case STANDARD: {
                    break;
                }
                default: {
                    throw new BSONException("Unexpected UUID representation");
                }
            }
        }
        return new UUID(readLongFromArrayBigEndian(bytes, 0), readLongFromArrayBigEndian(bytes, 8));
    }
    
    @Override
    public Class<UUID> getEncoderClass() {
        return UUID.class;
    }
    
    private static void writeLongToArrayBigEndian(final byte[] bytes, final int offset, final long x) {
        bytes[offset + 7] = (byte)(0xFFL & x);
        bytes[offset + 6] = (byte)(0xFFL & x >> 8);
        bytes[offset + 5] = (byte)(0xFFL & x >> 16);
        bytes[offset + 4] = (byte)(0xFFL & x >> 24);
        bytes[offset + 3] = (byte)(0xFFL & x >> 32);
        bytes[offset + 2] = (byte)(0xFFL & x >> 40);
        bytes[offset + 1] = (byte)(0xFFL & x >> 48);
        bytes[offset] = (byte)(0xFFL & x >> 56);
    }
    
    private static long readLongFromArrayBigEndian(final byte[] bytes, final int offset) {
        long x = 0L;
        x |= (0xFFL & (long)bytes[offset + 7]);
        x |= (0xFFL & (long)bytes[offset + 6]) << 8;
        x |= (0xFFL & (long)bytes[offset + 5]) << 16;
        x |= (0xFFL & (long)bytes[offset + 4]) << 24;
        x |= (0xFFL & (long)bytes[offset + 3]) << 32;
        x |= (0xFFL & (long)bytes[offset + 2]) << 40;
        x |= (0xFFL & (long)bytes[offset + 1]) << 48;
        x |= (0xFFL & (long)bytes[offset]) << 56;
        return x;
    }
}
