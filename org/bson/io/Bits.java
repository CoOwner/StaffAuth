package org.bson.io;

import java.io.*;

public class Bits
{
    public static void readFully(final InputStream inputStream, final byte[] buffer) throws IOException {
        readFully(inputStream, buffer, buffer.length);
    }
    
    public static void readFully(final InputStream inputStream, final byte[] buffer, final int length) throws IOException {
        readFully(inputStream, buffer, 0, length);
    }
    
    public static void readFully(final InputStream inputStream, final byte[] buffer, final int offset, final int length) throws IOException {
        if (buffer.length < length + offset) {
            throw new IllegalArgumentException("Buffer is too small");
        }
        int bytesRead;
        for (int arrayOffset = offset, bytesToRead = length; bytesToRead > 0; bytesToRead -= bytesRead, arrayOffset += bytesRead) {
            bytesRead = inputStream.read(buffer, arrayOffset, bytesToRead);
            if (bytesRead < 0) {
                throw new EOFException();
            }
        }
    }
    
    public static int readInt(final InputStream inputStream) throws IOException {
        return readInt(inputStream, new byte[4]);
    }
    
    public static int readInt(final InputStream inputStream, final byte[] buffer) throws IOException {
        readFully(inputStream, buffer, 4);
        return readInt(buffer);
    }
    
    public static int readInt(final byte[] buffer) {
        return readInt(buffer, 0);
    }
    
    public static int readInt(final byte[] buffer, final int offset) {
        int x = 0;
        x |= (0xFF & buffer[offset + 0]) << 0;
        x |= (0xFF & buffer[offset + 1]) << 8;
        x |= (0xFF & buffer[offset + 2]) << 16;
        x |= (0xFF & buffer[offset + 3]) << 24;
        return x;
    }
    
    public static int readIntBE(final byte[] buffer, final int offset) {
        int x = 0;
        x |= (0xFF & buffer[offset + 0]) << 24;
        x |= (0xFF & buffer[offset + 1]) << 16;
        x |= (0xFF & buffer[offset + 2]) << 8;
        x |= (0xFF & buffer[offset + 3]) << 0;
        return x;
    }
    
    public static long readLong(final InputStream inputStream) throws IOException {
        return readLong(inputStream, new byte[8]);
    }
    
    public static long readLong(final InputStream inputStream, final byte[] buffer) throws IOException {
        readFully(inputStream, buffer, 8);
        return readLong(buffer);
    }
    
    public static long readLong(final byte[] buffer) {
        return readLong(buffer, 0);
    }
    
    public static long readLong(final byte[] buffer, final int offset) {
        long x = 0L;
        x |= (0xFFL & (long)buffer[offset + 0]) << 0;
        x |= (0xFFL & (long)buffer[offset + 1]) << 8;
        x |= (0xFFL & (long)buffer[offset + 2]) << 16;
        x |= (0xFFL & (long)buffer[offset + 3]) << 24;
        x |= (0xFFL & (long)buffer[offset + 4]) << 32;
        x |= (0xFFL & (long)buffer[offset + 5]) << 40;
        x |= (0xFFL & (long)buffer[offset + 6]) << 48;
        x |= (0xFFL & (long)buffer[offset + 7]) << 56;
        return x;
    }
}
