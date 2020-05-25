package org.bson.io;

import org.bson.types.*;
import java.util.*;
import java.io.*;
import org.bson.*;

public abstract class OutputBuffer extends OutputStream implements BsonOutput
{
    @Override
    public void write(final byte[] b) {
        this.write(b, 0, b.length);
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public void write(final byte[] bytes, final int offset, final int length) {
        this.writeBytes(bytes, offset, length);
    }
    
    @Override
    public void writeBytes(final byte[] bytes) {
        this.writeBytes(bytes, 0, bytes.length);
    }
    
    @Override
    public void writeInt32(final int value) {
        this.write(value >> 0);
        this.write(value >> 8);
        this.write(value >> 16);
        this.write(value >> 24);
    }
    
    @Override
    public void writeInt32(final int position, final int value) {
        this.write(position, value >> 0);
        this.write(position + 1, value >> 8);
        this.write(position + 2, value >> 16);
        this.write(position + 3, value >> 24);
    }
    
    @Override
    public void writeInt64(final long value) {
        this.write((byte)(0xFFL & value >> 0));
        this.write((byte)(0xFFL & value >> 8));
        this.write((byte)(0xFFL & value >> 16));
        this.write((byte)(0xFFL & value >> 24));
        this.write((byte)(0xFFL & value >> 32));
        this.write((byte)(0xFFL & value >> 40));
        this.write((byte)(0xFFL & value >> 48));
        this.write((byte)(0xFFL & value >> 56));
    }
    
    @Override
    public void writeDouble(final double x) {
        this.writeLong(Double.doubleToRawLongBits(x));
    }
    
    @Override
    public void writeString(final String str) {
        this.writeInt(0);
        final int strLen = this.writeCharacters(str, false);
        this.writeInt32(this.getPosition() - strLen - 4, strLen);
    }
    
    @Override
    public void writeCString(final String value) {
        this.writeCharacters(value, true);
    }
    
    @Override
    public void writeObjectId(final ObjectId value) {
        this.write(value.toByteArray());
    }
    
    public int size() {
        return this.getSize();
    }
    
    public abstract int pipe(final OutputStream p0) throws IOException;
    
    public abstract List<ByteBuf> getByteBuffers();
    
    @Override
    public abstract void truncateToPosition(final int p0);
    
    public byte[] toByteArray() {
        try {
            final ByteArrayOutputStream bout = new ByteArrayOutputStream(this.size());
            this.pipe(bout);
            return bout.toByteArray();
        }
        catch (IOException ioe) {
            throw new RuntimeException("should be impossible", ioe);
        }
    }
    
    @Override
    public void write(final int value) {
        this.writeByte(value);
    }
    
    public void writeInt(final int value) {
        this.writeInt32(value);
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + " size: " + this.size() + " pos: " + this.getPosition();
    }
    
    protected abstract void write(final int p0, final int p1);
    
    public void writeLong(final long value) {
        this.writeInt64(value);
    }
    
    private int writeCharacters(final String str, final boolean checkForNullCharacters) {
        final int len = str.length();
        int total = 0;
        int c;
        for (int i = 0; i < len; i += Character.charCount(c)) {
            c = Character.codePointAt(str, i);
            if (checkForNullCharacters && c == 0) {
                throw new BsonSerializationException(String.format("BSON cstring '%s' is not valid because it contains a null character at index %d", str, i));
            }
            if (c < 128) {
                this.write((byte)c);
                ++total;
            }
            else if (c < 2048) {
                this.write((byte)(192 + (c >> 6)));
                this.write((byte)(128 + (c & 0x3F)));
                total += 2;
            }
            else if (c < 65536) {
                this.write((byte)(224 + (c >> 12)));
                this.write((byte)(128 + (c >> 6 & 0x3F)));
                this.write((byte)(128 + (c & 0x3F)));
                total += 3;
            }
            else {
                this.write((byte)(240 + (c >> 18)));
                this.write((byte)(128 + (c >> 12 & 0x3F)));
                this.write((byte)(128 + (c >> 6 & 0x3F)));
                this.write((byte)(128 + (c & 0x3F)));
                total += 4;
            }
        }
        this.write(0);
        return ++total;
    }
}
